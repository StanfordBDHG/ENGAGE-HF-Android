package edu.stanford.bdh.engagehf.bluetooth.measurements

import androidx.health.connect.client.records.BloodPressureRecord
import androidx.health.connect.client.records.HeartRateRecord
import androidx.health.connect.client.records.Record
import androidx.health.connect.client.records.WeightRecord
import androidx.health.connect.client.units.Mass
import androidx.health.connect.client.units.Pressure
import com.google.common.truth.Truth.assertThat
import edu.stanford.bdh.engagehf.bluetooth.service.Measurement
import edu.stanford.bdh.engagehf.bluetooth.service.Measurement.BloodPressure.Flags
import edu.stanford.bdh.engagehf.bluetooth.service.Measurement.BloodPressure.Status
import edu.stanford.bdh.engagehf.modules.healthconnectonfhir.RecordToObservationMapper
import edu.stanford.bdh.engagehf.modules.utils.TimeProvider
import io.mockk.every
import io.mockk.mockk
import org.hl7.fhir.r4.model.Observation
import org.junit.Before
import org.junit.Test
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.ZonedDateTime

class MeasurementToObservationMapperTest {
    private val recordToObservationMapper: RecordToObservationMapper = mockk()
    private val timeProvider: TimeProvider = mockk()
    private val now = Instant.now()
    private val offset = ZoneOffset.UTC

    private val mapper = MeasurementToObservationMapper(
        recordToObservationMapper = recordToObservationMapper,
        timeProvider = timeProvider,
    )

    @Before
    fun setup() {
        every { timeProvider.nowInstant() } returns now
        every { timeProvider.currentOffset() } returns offset
    }

    @Test
    fun `it should map weight correctly`() {
        // given
        val date = ZonedDateTime.now()
        val weightMeasurement = createWeight(weight = 70.5, zonedDateTime = date)
        val expectedObservation = Observation()
        val slot = mutableListOf<WeightRecord>()
        every { recordToObservationMapper.map(capture(slot)) } returns listOf(expectedObservation)

        // when
        val result = mapper.map(weightMeasurement)
        val capturedValue = slot.first()

        // then
        assertThat(capturedValue.weight).isEqualTo(Mass.kilograms(70.5))
        assertThat(capturedValue.time).isEqualTo(date.toInstant())
        assertThat(capturedValue.zoneOffset).isEqualTo(offset)
        assertThat(result.size).isEqualTo(1)
        assertThat(expectedObservation).isEqualTo(result.first())
    }

    @Test
    fun `it should map blood pressure correctly with valid timestamp`() {
        // given
        val measurement = createBloodPressure(
            timestampYear = 2025,
            timestampMonth = 6,
            timestampDay = 15,
            timeStampHour = 10,
            timeStampMinute = 30,
        )
        val expectedTime = LocalDateTime.of(
            measurement.timestampYear,
            measurement.timestampMonth,
            measurement.timestampDay,
            measurement.timeStampHour,
            measurement.timeStampMinute,
            measurement.timeStampSecond
        ).toInstant(offset)
        val expectedSamples = HeartRateRecord.Sample(
            time = expectedTime,
            beatsPerMinute = measurement.pulseRate.toLong()
        )
        val observation = Observation()
        val slots = mutableListOf<Record>()

        every {
            recordToObservationMapper.map(capture(slots))
        } returns listOf(observation)

        // when
        val result = mapper.map(measurement)
        val capturedHeartRateRecord = slots.first() as HeartRateRecord
        val capturedBloodPressureRecord = slots.last() as BloodPressureRecord

        // then
        assertThat(capturedHeartRateRecord.samples).isEqualTo(listOf(expectedSamples))
        assertThat(capturedHeartRateRecord.startTime).isEqualTo(expectedTime)
        assertThat(capturedHeartRateRecord.endTime).isEqualTo(expectedTime)
        assertThat(capturedHeartRateRecord.startZoneOffset).isEqualTo(offset)
        assertThat(capturedHeartRateRecord.endZoneOffset).isEqualTo(offset)
        assertThat(capturedBloodPressureRecord.time).isEqualTo(expectedTime)
        assertThat(capturedBloodPressureRecord.systolic).isEqualTo(Pressure.millimetersOfMercury(120.0))
        assertThat(capturedBloodPressureRecord.diastolic).isEqualTo(Pressure.millimetersOfMercury(80.0))
        assertThat(capturedBloodPressureRecord.zoneOffset).isEqualTo(offset)
        assertThat(result.size).isEqualTo(2)
        assertThat(result).containsExactlyElementsIn(listOf(observation, observation))
    }

    @Test
    fun `it should map blood pressure with fallback time when timestamp is invalid`() {
        // given
        val measurement = createBloodPressure(
            timestampYear = 0,
            timestampMonth = 0,
            timestampDay = 0,
            timeStampHour = 0,
            timeStampMinute = 0,
        )
        val expectedTime = now
        val observation = Observation()
        val slots = mutableListOf<Record>()

        every {
            recordToObservationMapper.map(capture(slots))
        } returns listOf(observation)

        // when
        val result = mapper.map(measurement)
        val capturedHeartRateRecord = slots.first() as HeartRateRecord
        val capturedBloodPressureRecord = slots.last() as BloodPressureRecord

        // then
        assertThat(capturedHeartRateRecord.startTime).isEqualTo(expectedTime)
        assertThat(capturedHeartRateRecord.endTime).isEqualTo(expectedTime)
        assertThat(capturedBloodPressureRecord.time).isEqualTo(expectedTime)
        assertThat(capturedBloodPressureRecord.zoneOffset).isEqualTo(offset)
        assertThat(result.size).isEqualTo(2)
        assertThat(result).containsExactlyElementsIn(listOf(observation, observation))
    }

    private fun createBloodPressure(
        timestampYear: Int,
        timestampMonth: Int,
        timestampDay: Int,
        timeStampHour: Int,
        timeStampMinute: Int,
    ) = Measurement.BloodPressure(
        flags = Flags(
            bloodPressureUnitsFlag = true,
            timeStampFlag = true,
            pulseRateFlag = true,
            userIdFlag = true,
            measurementStatusFlag = true,
        ),
        systolic = 120f,
        diastolic = 80f,
        meanArterialPressure = 1f,
        timestampYear = timestampYear,
        timestampMonth = timestampMonth,
        timestampDay = timestampDay,
        timeStampHour = timeStampHour,
        timeStampMinute = timeStampMinute,
        timeStampSecond = 0,
        pulseRate = 100f,
        userId = 1,
        measurementStatus = Status(
            bodyMovementDetectionFlag = true,
            cuffFitDetectionFlag = true,
            irregularPulseDetectionFlag = true,
            pulseRateRangeDetectionFlags = 0,
            measurementPositionDetectionFlag = true,
        ),
    )

    private fun createWeight(
        weight: Double = 60.0,
        zonedDateTime: ZonedDateTime? = null,
        userId: Int? = null,
        bmi: Double? = null,
        height: Double? = null,
    ) = Measurement.Weight(
        weight = weight,
        zonedDateTime = zonedDateTime,
        userId = userId,
        bmi = bmi,
        height = height,
    )
}
