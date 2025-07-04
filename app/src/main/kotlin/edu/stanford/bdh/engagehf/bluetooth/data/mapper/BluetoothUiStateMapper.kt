package edu.stanford.bdh.engagehf.bluetooth.data.mapper

import androidx.health.connect.client.records.BloodPressureRecord
import androidx.health.connect.client.records.HeartRateRecord
import androidx.health.connect.client.records.Record
import androidx.health.connect.client.records.WeightRecord
import edu.stanford.bdh.engagehf.R
import edu.stanford.bdh.engagehf.bluetooth.component.OperationStatus
import edu.stanford.bdh.engagehf.bluetooth.data.models.Action
import edu.stanford.bdh.engagehf.bluetooth.data.models.BluetoothUiState
import edu.stanford.bdh.engagehf.bluetooth.data.models.DeviceUiModel
import edu.stanford.bdh.engagehf.bluetooth.data.models.MeasurementDialogUiState
import edu.stanford.bdh.engagehf.bluetooth.data.models.VitalDisplayData
import edu.stanford.bdh.engagehf.bluetooth.service.EngageBLEServiceState
import edu.stanford.bdh.engagehf.bluetooth.service.Measurement
import edu.stanford.bdh.engagehf.modules.utils.LocaleProvider
import edu.stanford.spezi.ui.StringResource
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

class BluetoothUiStateMapper @Inject constructor(
    private val localeProvider: LocaleProvider,
) {

    private val dateFormatter by lazy {
        DateTimeFormatter.ofPattern(
            "dd.MM.yyyy, HH:mm", localeProvider.getDefaultLocale()
        )
    }

    private val systemDefaultDateFormatter by lazy {
        dateFormatter.withZone(ZoneId.systemDefault())
    }

    fun mapBleServiceState(state: EngageBLEServiceState): BluetoothUiState {
        return when (state) {
            EngageBLEServiceState.Idle -> {
                BluetoothUiState.Idle(description = StringResource(R.string.bluetooth_initializing_description))
            }

            EngageBLEServiceState.BluetoothNotEnabled -> {
                BluetoothUiState.Idle(
                    description = StringResource(R.string.bluetooth_not_enabled_description),
                    settingsAction = Action.Settings.BluetoothSettings,
                )
            }

            is EngageBLEServiceState.MissingPermissions -> {
                BluetoothUiState.Idle(
                    description = StringResource(R.string.bluetooth_permissions_not_granted_description),
                    settingsAction = Action.Settings.AppSettings,
                )
            }

            is EngageBLEServiceState.Scanning -> {
                val devices = state.sessions.map {
                    val summary = when (val lastMeasurement = it.measurements.lastOrNull()) {
                        is Measurement.BloodPressure -> {
                            val systolic = formatSystolicForLocale(lastMeasurement.systolic)
                            val diastolic = formatDiastolicForLocale(lastMeasurement.diastolic)
                            StringResource(R.string.blood_pressure_value, "$systolic / $diastolic")
                        }
                        is Measurement.Weight -> StringResource(R.string.weight_value, formatWeightForLocale(lastMeasurement.weight))

                        else -> StringResource(R.string.no_measurements_received)
                    }
                    val time = ZonedDateTime.ofInstant(
                        Instant.ofEpochMilli(it.device.lastSeenTimeStamp),
                        ZoneId.systemDefault()
                    )
                    DeviceUiModel(
                        name = it.device.name,
                        summary = summary,
                        connected = it.device.connected,
                        lastSeen = StringResource(R.string.last_seen_on, systemDefaultDateFormatter.format(time))
                    )
                }
                val header = if (devices.isEmpty()) {
                    StringResource(R.string.paired_devices_hint_description)
                } else {
                    null
                }
                BluetoothUiState.Ready(
                    header = header,
                    devices = devices
                )
            }
        }
    }

    fun mapMeasurementDialog(measurement: Measurement): MeasurementDialogUiState {
        return when (measurement) {
            is Measurement.Weight -> MeasurementDialogUiState(
                measurement = measurement,
                isVisible = true,
                formattedWeight = formatWeightForLocale(measurement.weight)
            )

            is Measurement.BloodPressure -> MeasurementDialogUiState(
                measurement = measurement,
                isVisible = true,
                formattedSystolic = formatSystolicForLocale(measurement.systolic),
                formattedDiastolic = formatDiastolicForLocale(measurement.diastolic),
                formattedHeartRate = formatHeartRateForLocale(measurement.pulseRate)
            )
        }
    }

    fun mapBloodPressure(result: Result<BloodPressureRecord?>): VitalDisplayData {
        val title = StringResource(R.string.blood_pressure)
        return mapRecordResult(
            result = result,
            title = title,
            onSuccess = { record ->
                VitalDisplayData(
                    title = title,
                    status = OperationStatus.SUCCESS,
                    date = dateFormatter.format(record.time.atZone(record.zoneOffset)),
                    value = "${record.systolic.inMillimetersOfMercury}/${record.diastolic.inMillimetersOfMercury}",
                    unit = "mmHg"
                )
            }
        )
    }

    fun mapWeight(result: Result<WeightRecord?>): VitalDisplayData {
        val title = StringResource(R.string.weight)
        val locale = getDefaultLocale()
        return mapRecordResult(
            result = result,
            title = title,
            onSuccess = { record ->
                VitalDisplayData(
                    title = title,
                    value = String.format(
                        locale,
                        "%.2f",
                        when (locale.country) {
                            "US", "LR", "MM" -> record.weight.inPounds
                            else -> record.weight.inKilograms
                        }
                    ),
                    unit = when (locale.country) {
                        "US", "LR", "MM" -> "lbs"
                        else -> "kg"
                    },
                    date = dateFormatter.format(record.time.atZone(record.zoneOffset)),
                    status = OperationStatus.SUCCESS,
                )
            }
        )
    }

    fun mapHeartRate(result: Result<HeartRateRecord?>): VitalDisplayData {
        val title = StringResource(R.string.heart_rate)
        return mapRecordResult(
            result = result,
            title = title,
            onSuccess = { record ->
                VitalDisplayData(
                    title = title,
                    value = "${
                        record.samples.stream()
                            .mapToLong(HeartRateRecord.Sample::beatsPerMinute)
                            .average()
                            .orElse(0.0)
                    }",
                    unit = "bpm",
                    date = record.samples.firstOrNull()?.let {
                        dateFormatter.format(it.time.atZone(record.startZoneOffset))
                    },
                    status = OperationStatus.SUCCESS
                )
            }
        )
    }

    private fun <T : Record> mapRecordResult(
        result: Result<T?>,
        title: StringResource,
        onSuccess: (T) -> VitalDisplayData,
    ): VitalDisplayData {
        val successResult = result.getOrNull()
        return when {
            result.isFailure -> VitalDisplayData(
                title = title,
                status = OperationStatus.FAILURE,
                date = null,
                value = null,
                unit = null,
                error = result.exceptionOrNull()?.message
            )

            successResult != null -> onSuccess(successResult)
            else -> VitalDisplayData(
                title = title,
                value = "No data available",
                unit = null,
                date = null,
                status = OperationStatus.NO_DATA
            )
        }
    }

    private fun formatWeightForLocale(weight: Double): String {
        val locale = getDefaultLocale()
        return String.format(
            locale, "%.2f", when (locale.country) {
                "US", "LR", "MM" -> weight * KG_TO_LBS_CONVERSION_FACTOR
                else -> weight
            }
        ) + when (locale.country) {
            "US", "LR", "MM" -> " lbs"
            else -> " kg"
        }
    }

    private fun formatSystolicForLocale(systolic: Float): String {
        return String.format(getDefaultLocale(), "%.0f mmHg", systolic)
    }

    private fun formatDiastolicForLocale(diastolic: Float): String {
        return String.format(getDefaultLocale(), "%.0f mmHg", diastolic)
    }

    private fun formatHeartRateForLocale(heartRate: Float): String {
        return String.format(getDefaultLocale(), "%.0f bpm", heartRate)
    }

    private fun getDefaultLocale() = localeProvider.getDefaultLocale()

    companion object {
        const val KG_TO_LBS_CONVERSION_FACTOR = 2.20462
    }
}
