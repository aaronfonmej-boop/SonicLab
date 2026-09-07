package com.soniclab3d.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.soniclab3d.physics.AcousticPhysics
import com.soniclab3d.physics.AcousticState
import com.soniclab3d.physics.RectangularRoom
import com.soniclab3d.physics.ReferenceParticle
import com.soniclab3d.physics.RoomAcoustics
import com.soniclab3d.physics.ScientificPalette
import com.soniclab3d.physics.SliceAxis
import com.soniclab3d.ui.theme.SonicPalette
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.sqrt

@Composable
fun PressureSliceMap(
    state: AcousticState,
    simulationTimeS: Double,
    modifier: Modifier = Modifier
) {
    val columns = 42
    val rows = 42
    val pressureScale = remember(state) {
        val main = AcousticPhysics.soundPressurePeakPa(state.levelDbSpl.toDouble()) *
            if (state.harmonicMode) state.harmonicAmplitudes.sumOf { abs(it.toDouble()) }.coerceAtLeast(1.0) else 1.0
        val secondary = state.secondarySource.takeIf { it.enabled }
            ?.let { AcousticPhysics.soundPressurePeakPa(it.levelDbSpl.toDouble()) } ?: 0.0
        max(main + secondary, 1e-9)
    }
    val normalized = remember(state, simulationTimeS) {
        FloatArray(columns * rows) { index ->
            val column = index % columns
            val row = index / columns
            val a = -state.maxDistanceM + 2.0 * state.maxDistanceM * column / (columns - 1.0)
            val b = state.maxDistanceM - 2.0 * state.maxDistanceM * row / (rows - 1.0)
            val (x, y, z) = when (state.sliceAxis) {
                SliceAxis.XY -> Triple(a, b, state.sliceOffsetM.toDouble())
                SliceAxis.XZ -> Triple(a, state.sliceOffsetM.toDouble(), b)
                SliceAxis.YZ -> Triple(state.sliceOffsetM.toDouble(), a, b)
            }
            if (sqrt(x * x + y * y + z * z) > state.maxDistanceM) {
                Float.NaN
            } else {
                (AcousticPhysics.sampleAt(state, x, y, z, simulationTimeS).pressurePa / pressureScale)
                    .toFloat()
                    .coerceIn(-1f, 1f)
            }
        }
    }
    val shape = RoundedCornerShape(18.dp)
    Column(
        modifier
            .fillMaxWidth()
            .clip(shape)
            .background(Brush.linearGradient(listOf(Color(0xFF091827), Color(0xFF06101A))))
            .border(1.dp, SonicPalette.Cyan.copy(alpha = 0.18f), shape)
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("Tomografía del campo", style = MaterialTheme.typography.titleMedium)
                Text(
                    "PLANO ${state.sliceAxis.name} · ${formatAdvanced(state.sliceOffsetM.toDouble(), 2)} m",
                    style = MaterialTheme.typography.labelSmall,
                    color = SonicPalette.Cyan
                )
            }
            Text("PRESIÓN", style = MaterialTheme.typography.labelSmall, color = SonicPalette.Muted)
        }
        Canvas(Modifier.fillMaxWidth().height(230.dp)) {
            val cellWidth = size.width / columns
            val cellHeight = size.height / rows
            normalized.forEachIndexed { index, value ->
                if (!value.isNaN()) {
                    val column = index % columns
                    val row = index / columns
                    drawRect(
                        color = scientificFieldColor(value, state.scientificPalette),
                        topLeft = Offset(column * cellWidth, row * cellHeight),
                        size = Size(cellWidth + 1f, cellHeight + 1f)
                    )
                }
            }
            drawRect(SonicPalette.Cyan.copy(alpha = 0.25f), style = Stroke(1.2f))
            drawLine(Color.White.copy(alpha = 0.18f), Offset(size.width / 2f, 0f), Offset(size.width / 2f, size.height), 1f)
            drawLine(Color.White.copy(alpha = 0.18f), Offset(0f, size.height / 2f), Offset(size.width, size.height / 2f), 1f)
        }
        PressureLegend(state.scientificPalette)
    }
}

@Composable
private fun PressureLegend(palette: ScientificPalette) {
    val negative = scientificFieldColor(-1f, palette)
    val positive = scientificFieldColor(1f, palette)
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text("RAREFACCIÓN", style = MaterialTheme.typography.labelSmall, color = negative)
        Spacer(Modifier.width(8.dp))
        Canvas(Modifier.weight(1f).height(8.dp)) {
            drawRoundRect(
                brush = Brush.horizontalGradient(listOf(negative, Color(0xFFB7CAD4), positive)),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(8f, 8f)
            )
        }
        Spacer(Modifier.width(8.dp))
        Text("COMPRESIÓN", style = MaterialTheme.typography.labelSmall, color = positive)
    }
}

@Composable
fun ParticleMicroscope(
    state: AcousticState,
    reference: ReferenceParticle,
    modifier: Modifier = Modifier
) {
    val sample = reference.sample
    val radius = sqrt(
        reference.equilibriumX * reference.equilibriumX +
            reference.equilibriumY * reference.equilibriumY +
            reference.equilibriumZ * reference.equilibriumZ
    ).coerceAtLeast(1e-12)
    val signedPhysical = (
        sample.displacementXM * reference.equilibriumX +
            sample.displacementYM * reference.equilibriumY +
            sample.displacementZM * reference.equilibriumZ
        ) / radius
    val localPeak = max(sample.displacementMagnitudeM, AcousticPhysics.metrics(state).displacementPeakM * 1e-4)
    val physicalNormalized = (signedPhysical / localPeak).toFloat().coerceIn(-1f, 1f)
    val visualMeters = (signedPhysical * state.displacementVisualScale)
        .coerceIn(-state.maxVisualDisplacementM.toDouble(), state.maxVisualDisplacementM.toDouble())
    val visualNormalized = (visualMeters / state.maxVisualDisplacementM.coerceAtLeast(1e-9f)).toFloat()
    val shape = RoundedCornerShape(18.dp)

    Column(
        modifier
            .fillMaxWidth()
            .clip(shape)
            .background(Color(0xFF091725))
            .border(1.dp, SonicPalette.Violet.copy(alpha = 0.20f), shape)
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(7.dp)
    ) {
        Text("Microscopio de partícula", style = MaterialTheme.typography.titleMedium)
        Text(
            if (reference.isSelected) "PARTÍCULA GPU #${reference.particleIndex}" else "SONDA DE REFERENCIA x = 2 m",
            style = MaterialTheme.typography.labelSmall,
            color = SonicPalette.Amber
        )
        Canvas(Modifier.fillMaxWidth().height(154.dp)) {
            fun track(y: Float, normalizedPosition: Float, color: Color) {
                val left = 18f
                val right = size.width - 18f
                val center = size.width / 2f
                drawLine(SonicPalette.Grid, Offset(left, y), Offset(right, y), 3f, StrokeCap.Round)
                drawLine(Color.White.copy(alpha = 0.42f), Offset(center, y - 18f), Offset(center, y + 18f), 2f)
                val position = center + normalizedPosition * (right - left) * 0.42f
                drawCircle(color.copy(alpha = 0.18f), 17f, Offset(position, y))
                drawCircle(color, 7f, Offset(position, y))
            }
            track(48f, physicalNormalized, SonicPalette.Cyan)
            track(118f, visualNormalized, SonicPalette.Magenta)
        }
        Row(Modifier.fillMaxWidth()) {
            Column(Modifier.weight(1f)) {
                Text("FÍSICA · AUTOZOOM", style = MaterialTheme.typography.labelSmall, color = SonicPalette.Cyan)
                Text("${scientificAdvanced(signedPhysical)} m", fontSize = 11.sp)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("DIBUJADA · ×${compactAdvanced(state.displacementVisualScale)}", style = MaterialTheme.typography.labelSmall, color = SonicPalette.Magenta)
                Text("${scientificAdvanced(visualMeters)} m", fontSize = 11.sp)
            }
        }
        Text(
            "La línea central es el equilibrio. La vista física usa autozoom; la vista dibujada representa exactamente la amplificación configurada.",
            style = MaterialTheme.typography.bodyMedium,
            fontSize = 10.sp,
            color = SonicPalette.Muted
        )
    }
}

@Composable
fun RoomModeMap(
    room: RectangularRoom,
    soundSpeedMps: Double,
    modifier: Modifier = Modifier
) {
    val columns = 48
    val rows = 34
    val values = remember(room) {
        FloatArray(columns * rows) { index ->
            val x = room.lengthM * (index % columns) / (columns - 1.0)
            val y = room.widthM * (index / columns) / (rows - 1.0)
            RoomAcoustics.normalizedPressure(room, x, y, room.heightM * 0.5).toFloat()
        }
    }
    val frequency = RoomAcoustics.modeFrequencyHz(room, soundSpeedMps)
    val shape = RoundedCornerShape(18.dp)
    Column(
        modifier
            .fillMaxWidth()
            .clip(shape)
            .background(Color(0xFF091725))
            .border(1.dp, SonicPalette.Violet.copy(alpha = 0.20f), shape)
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("Modo propio del recinto", style = MaterialTheme.typography.titleMedium)
                Text("MODO (${room.modeX}, ${room.modeY}, ${room.modeZ}) · ${RoomAcoustics.modeType(room).uppercase()}", style = MaterialTheme.typography.labelSmall, color = SonicPalette.Violet)
            }
            Text("${formatAdvanced(frequency, 2)} Hz", color = SonicPalette.Cyan, style = MaterialTheme.typography.titleMedium)
        }
        Canvas(Modifier.fillMaxWidth().height(210.dp)) {
            val cellWidth = size.width / columns
            val cellHeight = size.height / rows
            values.forEachIndexed { index, value ->
                drawRect(
                    scientificFieldColor(value, ScientificPalette.RED_BLUE),
                    Offset((index % columns) * cellWidth, (index / columns) * cellHeight),
                    Size(cellWidth + 1f, cellHeight + 1f)
                )
            }
            drawRect(SonicPalette.Violet.copy(alpha = 0.65f), style = Stroke(2f))
        }
        Text(
            "Corte horizontal a media altura. Los cambios de signo separan regiones de fase opuesta; las zonas cercanas a cero corresponden a nodos del modo ideal.",
            style = MaterialTheme.typography.bodyMedium,
            fontSize = 10.sp,
            color = SonicPalette.Muted
        )
    }
}

private fun scientificFieldColor(value: Float, palette: ScientificPalette): Color {
    val magnitude = abs(value).coerceIn(0f, 1f)
    val neutral = Color(0xFF9AAEB8)
    val positive: Color
    val negative: Color
    when (palette) {
        ScientificPalette.RED_BLUE -> {
            positive = Color(0xFFFF174F)
            negative = Color(0xFF1264FF)
        }
        ScientificPalette.COLORBLIND -> {
            positive = Color(0xFFFF8A00)
            negative = Color(0xFF00B8D9)
        }
        ScientificPalette.MONOCHROME -> {
            positive = Color.White
            negative = Color(0xFF111820)
        }
    }
    return if (value >= 0f) lerpAdvanced(neutral, positive, magnitude) else lerpAdvanced(neutral, negative, magnitude)
}

private fun lerpAdvanced(a: Color, b: Color, amount: Float): Color = Color(
    red = a.red + (b.red - a.red) * amount,
    green = a.green + (b.green - a.green) * amount,
    blue = a.blue + (b.blue - a.blue) * amount,
    alpha = 1f
)

private fun scientificAdvanced(value: Double): String = java.lang.String.format(java.util.Locale.US, "%.3e", value)
private fun formatAdvanced(value: Double, decimals: Int): String = java.lang.String.format(java.util.Locale.US, "%.${decimals}f", value)
private fun compactAdvanced(value: Float): String = when {
    value >= 1_000_000f -> "${formatAdvanced((value / 1_000_000f).toDouble(), 1)}M"
    value >= 1_000f -> "${formatAdvanced((value / 1_000f).toDouble(), 0)}k"
    else -> value.toInt().toString()
}
