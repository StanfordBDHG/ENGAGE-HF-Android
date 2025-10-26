package edu.stanford.bdh.engagehf.modules.healthconnectonfhir.internal

import androidx.health.connect.client.records.BloodPressureRecord
import androidx.health.connect.client.records.HeartRateRecord
import androidx.health.connect.client.records.Record
import androidx.health.connect.client.records.WeightRecord
import androidx.health.connect.client.units.Mass
import androidx.health.connect.client.units.Pressure
import ca.uhn.fhir.parser.IParser
import com.google.firebase.firestore.DocumentSnapshot
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import edu.stanford.bdh.engagehf.modules.healthconnectonfhir.Loinc
import edu.stanford.bdh.engagehf.modules.healthconnectonfhir.Metadata
import edu.stanford.bdh.engagehf.modules.healthconnectonfhir.ObservationsDocumentMapper
import org.hl7.fhir.r4.model.Observation
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.ZonedDateTime
import javax.inject.Inject

internal class ObservationsDocumentMapperImpl @Inject constructor(
    private val jsonParser: IParser,
    private val gson: Gson,
) : ObservationsDocumentMapper {
    private val mapType by lazy { object : TypeToken<Map<String, Any>>() {}.type }

    override fun map(observation: Observation): Map<String, Any> {
        val json = jsonParser.encodeResourceToString(observation)
        return gson.fromJson(json, mapType)
    }

    override fun <T : Record> map(observationDocument: DocumentSnapshot): T {
        val json = gson.toJson(observationDocument.data)
        val observation = jsonParser.parseResource(Observation::class.java, json)
        val documentId = observationDocument.id
        @Suppress("UNCHECKED_CAST")
        return when (observation.code.codingFirstRep.code) {
            Loinc.BLOOD_PRESSURE.code -> mapToBloodPressureRecord(observation, documentId)
            Loinc.WEIGHT.code -> mapToWeightRecord(observation, documentId)
            Loinc.HEART_RATE.code -> mapToHeartRateRecord(observation, documentId)
            else -> error("Unsupported observation type: ${observation.code.codingFirstRep.code}")
        } as T
    }

    private fun mapToHeartRateRecord(observation: Observation, id: String): Record {
        val time = observation.effectiveDateTimeType.value.toInstant()
        val zoneOffset = getZoneOffset(observation)
        val heartRate = observation.valueQuantity.value.toDouble()

        return HeartRateRecord(
            startTime = time,
            startZoneOffset = zoneOffset,
            endTime = time,
            endZoneOffset = zoneOffset,
            samples = listOf(
                HeartRateRecord.Sample(
                    time = time,
                    beatsPerMinute = heartRate.toLong()
                )
            ),
            metadata = Metadata(id = id)
        )
    }

    private fun mapToBloodPressureRecord(
        observation: Observation,
        id: String,
    ): BloodPressureRecord {
        val systolic =
            observation.component.first { it.code.codingFirstRep.code == SYSTOLIC }
                .valueQuantity.value.toDouble()
        val diastolic =
            observation.component.first { it.code.codingFirstRep.code == DIASTOLIC }
                .valueQuantity.value.toDouble()

        return BloodPressureRecord(
            time = observation.effectiveDateTimeType.value.toInstant(),
            zoneOffset = getZoneOffset(observation),
            systolic = Pressure.millimetersOfMercury(systolic),
            diastolic = Pressure.millimetersOfMercury(diastolic),
            metadata = Metadata(id = id)
        )
    }

    private fun getZoneOffset(observation: Observation) = runCatching {
        ZoneOffset.ofHoursMinutes(
            observation.effectiveDateTimeType.tzHour,
            observation.effectiveDateTimeType.tzMin
        )
    }.getOrDefault(getSystemDefaultZoneOffset())

    private fun getSystemDefaultZoneOffset(): ZoneOffset {
        val zonedDateTime = ZonedDateTime.now(ZoneId.systemDefault())
        return zonedDateTime.offset
    }

    private fun mapToWeightRecord(observation: Observation, id: String): WeightRecord {
        val weight = observation.valueQuantity.value.toDouble()
        val unit =
            observation.valueQuantity.unit

        val metadata = Metadata(id = id)

        return WeightRecord(
            time = observation.effectiveDateTimeType.value.toInstant(),
            zoneOffset = getZoneOffset(observation),
            weight = if (unit.equals(
                    "lbs",
                    ignoreCase = true
                )
            ) {
                Mass.pounds(weight)
            } else {
                Mass.kilograms(weight)
            },
            metadata = metadata
        )
    }

    private companion object {
        const val SYSTOLIC = "8480-6"
        const val DIASTOLIC = "8462-4"
    }
}
