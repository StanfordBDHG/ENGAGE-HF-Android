package edu.stanford.bdh.engagehf.medication.ui

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import edu.stanford.bdh.engagehf.R
import edu.stanford.bdh.engagehf.medication.data.DosageInformation
import edu.stanford.bdh.engagehf.medication.data.DoseSchedule
import edu.stanford.bdh.engagehf.medication.data.MedicationRecommendation
import edu.stanford.bdh.engagehf.medication.data.MedicationRecommendationType
import edu.stanford.spezi.ui.StringResource
import javax.inject.Inject
import edu.stanford.bdh.engagehf.modules.design.R.drawable as DrawableR

class MedicationUiStateMapper @Inject constructor(
    @ApplicationContext private val context: Context,
) {

    fun mapMedicationUiState(recommendations: List<MedicationRecommendation>): MedicationUiState {
        return if (recommendations.isEmpty()) {
            MedicationUiState.NoData(
                message = StringResource(R.string.medications_no_recommendations)
            )
        } else {
            val medicationsTaking = mutableListOf<MedicationCardUiModel>()
            val medicationsThatMayHelp = mutableListOf<MedicationCardUiModel>()
            recommendations
                .sortedByDescending { it.type.priority }
                .forEach { recommendation ->
                    val cardUiModel = map(recommendation)
                    val currentDailyIntake = recommendation.dosageInformation.currentDailyIntake
                    if (currentDailyIntake > 0.0) {
                        medicationsTaking.add(cardUiModel)
                    } else {
                        medicationsThatMayHelp.add(cardUiModel)
                    }
                }
            MedicationUiState.Success(
                medicationsTaking = Medications(
                    medications = medicationsTaking.toList(),
                    expanded = true,
                ),
                medicationsThatMayHelp = Medications(
                    medications = medicationsThatMayHelp.toList(),
                    expanded = true,
                ),
                colorKeyExpanded = true
            )
        }
    }

    private fun map(recommendation: MedicationRecommendation): MedicationCardUiModel {
        return MedicationCardUiModel(
            id = recommendation.id,
            title = recommendation.title,
            subtitle = recommendation.subtitle,
            description = recommendation.description,
            statusIconResId = iconId(recommendation.type),
            statusColor = statusColor(recommendation.type),
            isExpanded = false,
            videoPath = recommendation.videoPath,
            dosageInformation = mapDosageInformation(recommendation.dosageInformation),
        )
    }

    fun toggleItemExpand(
        section: MedicationViewModel.Section,
        uiState: MedicationUiState.Success,
    ): MedicationUiState {
        return when (section) {
            MedicationViewModel.Section.MEDICATIONS_TAKING -> {
                uiState.copy(
                    medicationsTaking = uiState.medicationsTaking.copy(
                        expanded = !uiState.medicationsTaking.expanded
                    )
                )
            }

            MedicationViewModel.Section.MEDICATIONS_THAT_MAY_HELP -> {
                uiState.copy(
                    medicationsThatMayHelp = uiState.medicationsThatMayHelp.copy(
                        expanded = !uiState.medicationsThatMayHelp.expanded
                    )
                )
            }

            MedicationViewModel.Section.COLOR_KEY -> {
                uiState.copy(colorKeyExpanded = !uiState.colorKeyExpanded)
            }
        }
    }

    fun expandMedication(
        medicationId: String,
        uiState: MedicationUiState,
    ): MedicationUiState {
        return if (uiState is MedicationUiState.Success) {
            val newValueTaking = uiState.medicationsTaking.medications.map { model ->
                if (model.id == medicationId) {
                    model.copy(isExpanded = model.isExpanded.not())
                } else {
                    model
                }
            }
            val newValueMayHelp = uiState.medicationsThatMayHelp.medications.map { model ->
                if (model.id == medicationId) {
                    model.copy(isExpanded = model.isExpanded.not())
                } else {
                    model
                }
            }
            MedicationUiState.Success(
                medicationsTaking = Medications(
                    medications = newValueTaking,
                    expanded = uiState.medicationsTaking.expanded
                ),
                medicationsThatMayHelp = Medications(
                    medications = newValueMayHelp,
                    expanded = uiState.medicationsThatMayHelp.expanded
                ),
                colorKeyExpanded = uiState.colorKeyExpanded,
            )
        } else {
            uiState
        }
    }

    private fun dosageValues(schedules: List<DoseSchedule>, unit: String): List<String> {
        return if (schedules.isEmpty()) {
            listOf(context.getString(R.string.dosage_information_not_available))
        } else {
            schedules.map { schedule ->
                val values = schedule.quantity.map { it.toString() }.joinToString("/") { it }
                val frequency = when (val value = schedule.frequency) {
                    1.0 -> ""
                    2.0 -> "twice "
                    else -> {
                        val sanitized = value.toString().removeSuffix(".0").removeSuffix(",0")
                        "${sanitized}x "
                    }
                }
                "$values $unit ${frequency}daily"
            }
        }
    }

    private fun mapDosageInformation(dosageInformation: DosageInformation): DosageInformationUiModel {
        val currentDailyIntake = dosageInformation.currentDailyIntake
        val targetDailyIntake = dosageInformation.targetDailyIntake
        val progress = if (targetDailyIntake == 0.0) {
            0.0
        } else {
            currentDailyIntake / targetDailyIntake
        }
        val unit = dosageInformation.unit
        return DosageInformationUiModel(
            currentDose = DosageRowInfoData(
                label = context.getString(R.string.dosage_information_dose_row_current_dose),
                dosageValues = dosageValues(dosageInformation.currentSchedule, unit),
            ), targetDose = DosageRowInfoData(
                label = context.getString(R.string.dosage_information_dose_row_target_dose),
                dosageValues = dosageValues(dosageInformation.targetSchedule, unit),
            ), progress = progress.toFloat().coerceIn(0f, 1.0f)
        )
    }

    private fun iconId(type: MedicationRecommendationType): Int? {
        return when (type) {
            MedicationRecommendationType.TARGET_DOSE_REACHED,
            MedicationRecommendationType.PERSONAL_TARGET_DOSE_REACHED,
            -> DrawableR.ic_check

            MedicationRecommendationType.IMPROVEMENT_AVAILABLE,
            MedicationRecommendationType.NOT_STARTED,
            -> DrawableR.ic_arrow_up

            MedicationRecommendationType.MORE_PATIENT_OBSERVATIONS_REQUIRED,
            MedicationRecommendationType.MORE_LAB_OBSERVATIONS_REQUIRED,
            MedicationRecommendationType.NO_ACTION_REQUIRED,
            -> null
        }
    }

    private fun statusColor(type: MedicationRecommendationType): MedicationColor {
        return when (type) {
            MedicationRecommendationType.TARGET_DOSE_REACHED,
            MedicationRecommendationType.PERSONAL_TARGET_DOSE_REACHED,
            -> MedicationColor.GREEN_SUCCESS

            MedicationRecommendationType.IMPROVEMENT_AVAILABLE -> MedicationColor.YELLOW

            MedicationRecommendationType.MORE_PATIENT_OBSERVATIONS_REQUIRED,
            MedicationRecommendationType.MORE_LAB_OBSERVATIONS_REQUIRED,
            -> MedicationColor.BLUE

            MedicationRecommendationType.NOT_STARTED,
            MedicationRecommendationType.NO_ACTION_REQUIRED,
            -> MedicationColor.GRAY
        }
    }
}
