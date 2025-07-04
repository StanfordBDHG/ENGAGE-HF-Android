package edu.stanford.bdh.engagehf.health.symptoms

import edu.stanford.bdh.engagehf.R
import edu.stanford.bdh.engagehf.health.AggregatedHealthData
import edu.stanford.bdh.engagehf.health.NewestHealthData
import edu.stanford.bdh.engagehf.health.TableEntryData
import edu.stanford.bdh.engagehf.modules.utils.LocaleProvider
import edu.stanford.bdh.engagehf.modules.utils.extensions.roundToDecimalPlaces
import edu.stanford.spezi.ui.StringResource
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import javax.inject.Inject

@Suppress("MagicNumber")
class SymptomsUiStateMapper @Inject constructor(
    private val localeProvider: LocaleProvider,
) {

    private val dateTimeFormatter = DateTimeFormatter.ofPattern(MONTH_DAY_TIME_PATTERN)
    private val monthYearFormatter = DateTimeFormatter.ofPattern(MONTH_YEAR_PATTERN)

    fun mapSymptomsUiState(
        selectedSymptomType: SymptomType,
        symptomScores: List<SymptomScore>,
    ): SymptomsUiState {
        if (symptomScores.isEmpty()) {
            return SymptomsUiState.NoData(StringResource(R.string.no_symptom_scores_available))
        }
        val oldestDate = symptomScores.minBy { it.zonedDateTime }.zonedDateTime
        val chartData = calculateChartData(
            symptomScores = symptomScores,
            oldestDateTime = oldestDate,
            selectedSymptomType = selectedSymptomType,
        )
        val tableData = mapTableData(symptomScores, selectedSymptomType)
        val newestHealthData = mapNewestHealthData(symptomScores, selectedSymptomType)

        return SymptomsUiState.Success(
            data = SymptomsUiData(
                symptomScores = symptomScores,
                chartData = listOf(chartData),
                tableData = tableData,
                headerData = HeaderData(
                    formattedValue = newestHealthData.formattedValue,
                    formattedDate = newestHealthData.formattedDate,
                    selectedSymptomType = selectedSymptomType,
                    isSelectedSymptomTypeDropdownExpanded = false,
                ),
                xValueFormatter = { value ->
                    oldestDate
                        .plusDays(value.toLong())
                        .format(DateTimeFormatter.ofPattern("MMM dd"))
                }
            )
        )
    }

    private fun mapNewestHealthData(
        symptomScores: List<SymptomScore>,
        selectedSymptomType: SymptomType,
    ): NewestHealthData {
        val newestData = symptomScores.maxBy { it.zonedDateTime }
        return NewestHealthData(
            formattedValue = formatValue(newestData, selectedSymptomType),
            formattedDate = newestData.zonedDateTime.format(monthYearFormatter)
        )
    }

    private fun formatValue(newestData: SymptomScore, selectedSymptomType: SymptomType): String {
        return when (selectedSymptomType) {
            SymptomType.OVERALL -> newestData.overallScore.asPercent()
            SymptomType.PHYSICAL_LIMITS -> newestData.physicalLimitsScore.asPercent()
            SymptomType.SOCIAL_LIMITS -> newestData.socialLimitsScore.asPercent()
            SymptomType.QUALITY_OF_LIFE -> newestData.qualityOfLifeScore.asPercent()
            SymptomType.SPECIFIC -> newestData.symptomFrequencyScore.asPercent()
            SymptomType.DIZZINESS -> newestData.dizzinessScore?.toString() ?: NOT_AVAILABLE
        }
    }

    private fun Double?.asPercent(): String = this?.let {
        "${it.roundToDecimalPlaces(places = 2)}%"
    } ?: NOT_AVAILABLE

    private fun mapTableData(
        symptomScores: List<SymptomScore>,
        selectedSymptomType: SymptomType,
    ): List<TableEntryData> {
        val sortedScores = symptomScores.sortedBy { it.date }
        return sortedScores
            .mapIndexed { index, score ->
                val previousScore =
                    sortedScores.getOrNull(index - 1)
                val currentValue =
                    getScoreForSelectedSymptomType(selectedSymptomType, score)

                val previousValue = previousScore?.let {
                    getScoreForSelectedSymptomType(selectedSymptomType, it)
                }

                val formattedValue = currentValue?.let {
                    if (selectedSymptomType == SymptomType.DIZZINESS) {
                        it.toString()
                    } else {
                        "$it%"
                    }
                } ?: NOT_AVAILABLE

                val trend =
                    if (previousValue != null && currentValue != null && previousValue != 0.0) {
                        ((currentValue - previousValue) / previousValue) * 100
                    } else {
                        null
                    }

                val formattedTrend = trend?.let {
                    String.format(localeProvider.getDefaultLocale(), PERCENT_FORMAT, it)
                } ?: NOT_AVAILABLE

                TableEntryData(
                    id = null,
                    value = currentValue,
                    secondValue = null,
                    formattedValues = formattedValue,
                    date = score.zonedDateTime,
                    formattedDate = score.zonedDateTime.format(dateTimeFormatter),
                    trend = trend,
                    formattedTrend = formattedTrend
                )
            }.reversed()
    }

    private fun calculateChartData(
        symptomScores: List<SymptomScore>,
        selectedSymptomType: SymptomType,
        oldestDateTime: ZonedDateTime,
    ): AggregatedHealthData {
        val yValues = mutableListOf<Double>()
        val xValues = mutableListOf<Double>()
        val groupedByDay = symptomScores.groupBy { it.zonedDateTime.toLocalDate() }
        val oldestLocalDate = oldestDateTime.toLocalDate()

        groupedByDay.forEach { (date, scores) ->
            val filteredScores = scores.mapNotNull { score ->
                getScoreForSelectedSymptomType(selectedSymptomType, score)
            }

            if (filteredScores.isNotEmpty()) {
                val averageScore = filteredScores.average()
                yValues.add(averageScore)
                xValues.add(ChronoUnit.DAYS.between(oldestLocalDate, date).toDouble())
            }
        }

        return AggregatedHealthData(
            yValues = yValues,
            xValues = xValues,
            seriesName = when (selectedSymptomType) {
                SymptomType.OVERALL -> R.string.symptom_type_overall
                SymptomType.PHYSICAL_LIMITS -> R.string.symptom_type_physical
                SymptomType.SOCIAL_LIMITS -> R.string.symptom_type_social
                SymptomType.QUALITY_OF_LIFE -> R.string.symptom_type_quality
                SymptomType.SPECIFIC -> R.string.symptom_type_specific
                SymptomType.DIZZINESS -> R.string.symptom_type_dizziness
            }.let { StringResource(it) }
        )
    }

    private fun getScoreForSelectedSymptomType(
        selectedSymptomType: SymptomType,
        score: SymptomScore,
    ) = when (selectedSymptomType) {
        SymptomType.OVERALL -> score.overallScore
        SymptomType.PHYSICAL_LIMITS -> score.physicalLimitsScore
        SymptomType.SOCIAL_LIMITS -> score.socialLimitsScore
        SymptomType.QUALITY_OF_LIFE -> score.qualityOfLifeScore
        SymptomType.SPECIFIC -> score.symptomFrequencyScore
        SymptomType.DIZZINESS -> score.dizzinessScore
    }?.roundToDecimalPlaces(places = 2)

    companion object {
        private const val PERCENT_FORMAT = "%+.1f%%"
        private const val NOT_AVAILABLE = "N/A"
        private const val MONTH_DAY_TIME_PATTERN = "MMM dd HH:mm"
        private const val MONTH_YEAR_PATTERN = "MMM yy"
    }
}
