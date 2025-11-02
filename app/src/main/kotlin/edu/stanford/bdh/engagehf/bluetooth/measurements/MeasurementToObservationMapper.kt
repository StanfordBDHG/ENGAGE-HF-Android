package edu.stanford.bdh.engagehf.bluetooth.measurements

import androidx.health.connect.client.records.BloodPressureRecord
import androidx.health.connect.client.records.HeartRateRecord
import androidx.health.connect.client.records.Record
import androidx.health.connect.client.records.WeightRecord
import androidx.health.connect.client.units.Mass
import androidx.health.connect.client.units.Pressure
import edu.stanford.bdh.engagehf.bluetooth.service.Measurement
import edu.stanford.bdh.engagehf.modules.healthconnectonfhir.Metadata
import edu.stanford.bdh.engagehf.modules.healthconnectonfhir.RecordToObservationMapper
import edu.stanford.bdh.engagehf.modules.utils.TimeProvider
import org.hl7.fhir.r4.model.Observation
import java.time.Instant
import java.time.LocalDateTime
import javax.inject.Inject

internal class MeasurementToObservationMapper @Inject constructor(
    private val recordToObservationMapper: RecordToObservationMapper,
    private val timeProvider: TimeProvider,
) {

    private val currentZoneOffset get() = timeProvider.currentOffset()

    fun map(measurement: Measurement): List<Observation> {
        return mapToRecords(measurement)
            .flatMap { recordToObservationMapper.map(it) }
    }

    private fun mapToRecords(measurement: Measurement): List<Record> {
        return when (measurement) {
            is Measurement.BloodPressure -> {
                listOf(createHeartRateRecord(measurement), createBloodPressureRecord(measurement))
            }

            is Measurement.Weight -> {
                listOf(createWeightRecord(measurement))
            }
        }
    }

    private fun createBloodPressureRecord(measurement: Measurement.BloodPressure): BloodPressureRecord {
        return BloodPressureRecord(
            systolic = Pressure.millimetersOfMercury(measurement.systolic.toDouble()),
            diastolic = Pressure.millimetersOfMercury(measurement.diastolic.toDouble()),
            time = createInstant(measurement),
            zoneOffset = currentZoneOffset,
            metadata = Metadata()
        )
    }

    private fun createHeartRateRecord(measurement: Measurement.BloodPressure): HeartRateRecord {
        val time = createInstant(measurement)
        return HeartRateRecord(
            startTime = time,
            endTime = time,
            startZoneOffset = currentZoneOffset,
            endZoneOffset = currentZoneOffset,
            samples = listOf(
                HeartRateRecord.Sample(
                    time = time,
                    beatsPerMinute = measurement.pulseRate.toLong()
                )
            ),
            metadata = Metadata()
        )
    }

    private fun createWeightRecord(measurement: Measurement.Weight): WeightRecord {
        return WeightRecord(
            weight = Mass.kilograms(measurement.weight),
            time = measurement.zonedDateTime?.toInstant() ?: timeProvider.nowInstant(),
            zoneOffset = currentZoneOffset,
            metadata = Metadata(),
        )
    }

    private fun createInstant(measurement: Measurement.BloodPressure): Instant {
        val now = timeProvider.nowInstant()
        return runCatching {
            val localTime = LocalDateTime.of(
                measurement.timestampYear,
                measurement.timestampMonth,
                measurement.timestampDay,
                measurement.timeStampHour,
                measurement.timeStampMinute,
                measurement.timeStampSecond
            )
            val measurementInstant = localTime.toInstant(currentZoneOffset)
            if (measurementInstant.isAfter(now)) now else measurementInstant
            // using now instant in case device sent an invalid timestamp
        }.getOrDefault(now)
    }
}
