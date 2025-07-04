package edu.stanford.bdh.engagehf.health.symptoms

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberAxisLabelComponent
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberBottomAxis
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberStartAxis
import com.patrykandpatrick.vico.compose.cartesian.fullWidth
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLine
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberPoint
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.cartesian.rememberVicoScrollState
import com.patrykandpatrick.vico.compose.cartesian.rememberVicoZoomState
import com.patrykandpatrick.vico.compose.common.component.rememberShapeComponent
import com.patrykandpatrick.vico.compose.common.component.rememberTextComponent
import com.patrykandpatrick.vico.compose.common.fill
import com.patrykandpatrick.vico.core.cartesian.HorizontalLayout
import com.patrykandpatrick.vico.core.cartesian.Scroll
import com.patrykandpatrick.vico.core.cartesian.Zoom
import com.patrykandpatrick.vico.core.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.core.cartesian.data.AxisValueOverrider
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.CartesianValueFormatter
import com.patrykandpatrick.vico.core.cartesian.data.lineSeries
import com.patrykandpatrick.vico.core.cartesian.layer.LineCartesianLayer
import com.patrykandpatrick.vico.core.cartesian.layer.LineCartesianLayer.PointProvider.Companion.single
import com.patrykandpatrick.vico.core.cartesian.marker.DefaultCartesianMarker
import com.patrykandpatrick.vico.core.common.component.TextComponent
import com.patrykandpatrick.vico.core.common.shape.Shape
import edu.stanford.bdh.engagehf.R
import edu.stanford.bdh.engagehf.health.HealthTableItem
import edu.stanford.spezi.ui.CenteredBoxContent
import edu.stanford.spezi.ui.VerticalSpacer
import edu.stanford.spezi.ui.theme.Colors.primary
import edu.stanford.spezi.ui.theme.Colors.secondary
import edu.stanford.spezi.ui.theme.Sizes
import edu.stanford.spezi.ui.theme.Spacings
import edu.stanford.spezi.ui.theme.TextStyles
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun SymptomsPage() {
    val viewModel = hiltViewModel<SymptomsViewModel>()
    val uiState by viewModel.uiState.collectAsState()
    SymptomsPage(
        uiState = uiState,
        onAction = viewModel::onAction,
    )
}

@Composable
fun SymptomsPage(
    uiState: SymptomsUiState,
    onAction: (SymptomsViewModel.Action) -> Unit,
) {
    when (uiState) {
        is SymptomsUiState.Error -> {
            CenteredBoxContent {
                Text(
                    text = uiState.message.text(),
                    style = TextStyles.headlineMedium,
                    textAlign = TextAlign.Center,
                )
            }
        }

        SymptomsUiState.Loading -> {
            CenteredBoxContent {
                CircularProgressIndicator(color = primary)
            }
        }

        is SymptomsUiState.Success -> {
            LazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {
                listHeader(uiState = uiState, onAction = onAction)
                itemsIndexed(uiState.data.tableData) { index, tableEntryData ->
                    HealthTableItem(tableEntryData)
                    if (index != uiState.data.tableData.size - 1) HorizontalDivider()
                }
            }
        }

        is SymptomsUiState.NoData -> {
            CenteredBoxContent {
                Text(
                    text = uiState.message.text(),
                    textAlign = TextAlign.Center,
                    style = TextStyles.headlineMedium,
                )
            }
        }
    }
}

private fun LazyListScope.listHeader(
    uiState: SymptomsUiState.Success,
    onAction: (SymptomsViewModel.Action) -> Unit,
) {
    item {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(vertical = Spacings.medium)
        ) {
            Column {
                Text(
                    text = uiState.data.headerData.formattedValue,
                    style = TextStyles.headlineLarge.copy(color = primary),
                    modifier = Modifier.padding(vertical = Spacings.small)
                )
                Text(
                    text = uiState.data.headerData.formattedDate,
                    style = TextStyles.bodyMedium.copy(color = secondary)
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            SymptomsDropdown(uiState.data.headerData, onAction)
            IconButton(
                modifier = Modifier.size(Sizes.Icon.large),
                onClick = { onAction(SymptomsViewModel.Action.Info) }
            ) {
                Icon(
                    imageVector = Icons.Filled.Info,
                    contentDescription = stringResource(R.string.info_icon_content_description),
                    tint = primary
                )
            }
        }
        SymptomsChart(uiState.data)
        VerticalSpacer()
        Row(
            modifier = Modifier.padding(horizontal = Spacings.medium),
            verticalAlignment = Alignment.Bottom,
        ) {
            Text(
                text = stringResource(R.string.health_history),
                style = TextStyles.headlineMedium,
            )
        }
    }
}

@Composable
fun SymptomsChart(
    uiState: SymptomsUiData,
) {
    val modelProducer = remember { CartesianChartModelProducer() }
    LaunchedEffect(uiState.chartData.hashCode()) {
        withContext(Dispatchers.Default) {
            if (uiState.chartData.isEmpty()) return@withContext
            modelProducer.runTransaction {
                lineSeries {
                    uiState.chartData.forEach {
                        series(x = it.xValues, y = it.yValues)
                    }
                }
            }
        }
    }
    val shapeComponent = rememberShapeComponent(
        shape = Shape.Pill,
        color = primary
    )

    val xValueFormatter = CartesianValueFormatter { value, _, _ -> uiState.xValueFormatter(value) }

    val marker = remember {
        DefaultCartesianMarker(
            label = TextComponent(),
            labelPosition = DefaultCartesianMarker.LabelPosition.AroundPoint,
            indicatorSizeDp = 5f,
        )
    }
    CartesianChartHost(
        chart =
        rememberCartesianChart(
            rememberLineCartesianLayer(
                LineCartesianLayer.LineProvider.series(
                    rememberLine(
                        fill = LineCartesianLayer.LineFill.single(fill(primary)),
                        areaFill = LineCartesianLayer.AreaFill.single(fill(Color.Transparent)),
                        pointProvider = single(
                            rememberPoint(
                                shapeComponent,
                                5.dp,
                            )
                        ),
                    )
                ),
                axisValueOverrider = AxisValueOverrider.fixed(
                    maxY = if (uiState.headerData.selectedSymptomType == SymptomType.DIZZINESS) 5.0 else 100.0,
                    minY = 0.0,
                    minX = uiState.chartData.first().xValues.minOrNull() ?: 0.0,
                    maxX = uiState.chartData.first().xValues.maxOrNull() ?: 0.0,
                )
            ),
            startAxis = rememberStartAxis(
                titleComponent = rememberTextComponent(),
                label = rememberAxisLabelComponent(),
                guideline = null,
                valueFormatter = rememberYAxisValueFormatter(uiState.headerData.selectedSymptomType),
            ),
            bottomAxis = rememberBottomAxis(
                guideline = null,
                valueFormatter = xValueFormatter,
                itemPlacer = remember {
                    HorizontalAxis.ItemPlacer.default(
                        spacing = 1,
                        offset = 0,
                        addExtremeLabelPadding = true
                    )
                },
            ),
            marker = marker,
            decorations = emptyList(),
            horizontalLayout = HorizontalLayout.fullWidth(),
        ),
        modelProducer = modelProducer,
        zoomState = rememberVicoZoomState(
            zoomEnabled = false,
            initialZoom = remember { Zoom.max(Zoom.static(), Zoom.Content) },
        ),
        scrollState = rememberVicoScrollState(
            initialScroll = Scroll.Absolute.End,
        ),
    )
}

@Suppress("MagicNumber")
@Composable
private fun rememberYAxisValueFormatter(
    selectedSymptomType: SymptomType,
): CartesianValueFormatter {
    val context = LocalContext.current
    return remember(selectedSymptomType) {
        if (selectedSymptomType == SymptomType.DIZZINESS) {
            CartesianValueFormatter { value, _, _ ->
                val resId = when (value) {
                    5.0 -> R.string.dizziness_score_very_severe
                    4.0 -> R.string.dizziness_score_severe
                    3.0 -> R.string.dizziness_score_moderate
                    2.0 -> R.string.dizziness_score_mild
                    1.0 -> R.string.dizziness_score_minimal
                    else -> R.string.dizziness_score_none
                }
                context.getString(resId)
            }
        } else {
            CartesianValueFormatter.yPercent()
        }
    }
}

@Composable
private fun SymptomsDropdown(headerData: HeaderData, onAction: (SymptomsViewModel.Action) -> Unit) {
    Box(modifier = Modifier.wrapContentSize(Alignment.TopStart)) {
        TextButton(onClick = {
            onAction(SymptomsViewModel.Action.ToggleSymptomTypeDropdown(true))
        }) {
            SymptomTypeHeaderText(headerData.selectedSymptomType)
            Icon(Icons.Default.ArrowDropDown, contentDescription = "ArrowDropDown")
        }
        DropdownMenu(
            expanded = headerData.isSelectedSymptomTypeDropdownExpanded,
            onDismissRequest = {
                onAction(SymptomsViewModel.Action.ToggleSymptomTypeDropdown(false))
            }) {
            SymptomType.entries.forEach { symptomType ->
                val isSelected = headerData.selectedSymptomType == symptomType
                DropdownMenuItem(
                    text = { SymptomTypeDropdownText(symptomType) },
                    onClick = {
                        onAction(SymptomsViewModel.Action.ToggleSymptomTypeDropdown(false))
                        onAction(SymptomsViewModel.Action.SelectSymptomType(symptomType))
                    },
                    leadingIcon = {
                        Icon(
                            Icons.Outlined.Check,
                            contentDescription = null,
                            tint = if (isSelected) Color.Black else Color.Transparent
                        )
                    }
                )
            }
        }
    }
}

@Composable
private fun SymptomTypeHeaderText(symptomType: SymptomType) {
    Text(
        text = when (symptomType) {
            SymptomType.OVERALL -> stringResource(R.string.overall_score_title)
            SymptomType.PHYSICAL_LIMITS -> stringResource(R.string.physical_limits_score_title)
            SymptomType.SOCIAL_LIMITS -> stringResource(R.string.social_limits_score_title)
            SymptomType.QUALITY_OF_LIFE -> stringResource(R.string.quality_of_life_score_title)
            SymptomType.SPECIFIC -> stringResource(R.string.symptoms_frequency_score_title)
            SymptomType.DIZZINESS -> stringResource(R.string.dizziness_score_title)
        }
    )
}

@Composable
private fun SymptomTypeDropdownText(symptomType: SymptomType) {
    Text(
        text = when (symptomType) {
            SymptomType.OVERALL -> stringResource(R.string.symptom_type_overall)
            SymptomType.PHYSICAL_LIMITS -> stringResource(R.string.symptom_type_physical)
            SymptomType.SOCIAL_LIMITS -> stringResource(R.string.symptom_type_social)
            SymptomType.QUALITY_OF_LIFE -> stringResource(R.string.symptom_type_quality)
            SymptomType.SPECIFIC -> stringResource(R.string.symptom_type_specific)
            SymptomType.DIZZINESS -> stringResource(R.string.symptom_type_dizziness)
        }
    )
}
