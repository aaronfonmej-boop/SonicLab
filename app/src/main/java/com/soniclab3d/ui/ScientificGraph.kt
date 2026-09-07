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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.soniclab3d.ui.theme.SonicPalette
import java.util.Locale
import kotlin.math.abs
import kotlin.math.max

@Composable
fun ScientificGraph(
    title: String,
    values: List<Double>,
    unit: String,
    cursorFraction: Float? = null,
    modifier: Modifier = Modifier,
    lineColor: Color = MaterialTheme.colorScheme.primary
) {
    val maximum = max(values.maxOfOrNull { abs(it) } ?: 0.0, 1e-15)
    val shape = RoundedCornerShape(18.dp)
    Column(
        modifier
            .fillMaxWidth()
            .clip(shape)
            .background(Brush.linearGradient(listOf(Color(0xFF0A1927), Color(0xFF07111D))))
            .border(1.dp, lineColor.copy(alpha = 0.16f), shape)
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(7.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleMedium, fontSize = 12.sp, color = SonicPalette.Ice)
                Text("RANGO ±${scientific(maximum)}", style = MaterialTheme.typography.labelSmall, color = SonicPalette.Muted)
            }
            Text(
                unit.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = lineColor,
                modifier = Modifier
                    .background(lineColor.copy(alpha = 0.10f), RoundedCornerShape(7.dp))
                    .padding(horizontal = 7.dp, vertical = 4.dp)
            )
        }
        Canvas(Modifier.fillMaxWidth().height(108.dp)) {
            val left = 5f
            val right = size.width - 5f
            val top = 6f
            val bottom = size.height - 6f
            for (grid in 0..4) {
                val y = top + (bottom - top) * grid / 4f
                drawLine(SonicPalette.Grid.copy(alpha = 0.52f), Offset(left, y), Offset(right, y), 1f)
                val x = left + (right - left) * grid / 4f
                drawLine(SonicPalette.Grid.copy(alpha = 0.30f), Offset(x, top), Offset(x, bottom), 1f)
            }
            drawLine(
                SonicPalette.Muted.copy(alpha = 0.32f),
                Offset(left, (top + bottom) * 0.5f),
                Offset(right, (top + bottom) * 0.5f),
                1.2f
            )
            if (values.size > 1) {
                val path = Path()
                values.forEachIndexed { index, value ->
                    val x = left + (right - left) * index / (values.size - 1).toFloat()
                    val normalized = (value / maximum).toFloat().coerceIn(-1f, 1f)
                    val y = (top + bottom) * 0.5f - normalized * (bottom - top) * 0.44f
                    if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
                }
                drawPath(path, lineColor.copy(alpha = 0.16f), style = Stroke(width = 8f, cap = StrokeCap.Round))
                drawPath(path, lineColor, style = Stroke(width = 2.4f, cap = StrokeCap.Round))
            }
            cursorFraction?.coerceIn(0f, 1f)?.let { fraction ->
                val x = left + (right - left) * fraction
                drawLine(SonicPalette.Amber.copy(alpha = 0.55f), Offset(x, top), Offset(x, bottom), 1.5f)
                drawCircle(SonicPalette.Amber, radius = 3.5f, center = Offset(x, (top + bottom) * 0.5f))
            }
        }
    }
}

@Composable
fun SpectrumGraph(
    fundamentalHz: Double,
    amplitudes: List<Float>,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(18.dp)
    Column(
        modifier
            .fillMaxWidth()
            .clip(shape)
            .background(Brush.linearGradient(listOf(Color(0xFF0A1927), Color(0xFF07111D))))
            .border(1.dp, SonicPalette.Violet.copy(alpha = 0.18f), shape)
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(7.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("Espectro sintetizado", style = MaterialTheme.typography.titleMedium, fontSize = 12.sp)
                Text("FUNDAMENTAL + ARMÓNICOS", style = MaterialTheme.typography.labelSmall, color = SonicPalette.Violet)
            }
            Text("SYNTH", style = MaterialTheme.typography.labelSmall, color = SonicPalette.Muted)
        }
        Canvas(Modifier.fillMaxWidth().height(102.dp)) {
            val baseline = size.height - 15f
            for (grid in 0..3) {
                val y = 5f + (baseline - 5f) * grid / 3f
                drawLine(SonicPalette.Grid.copy(alpha = 0.42f), Offset(5f, y), Offset(size.width - 5f, y), 1f)
            }
            drawLine(SonicPalette.Muted.copy(alpha = 0.34f), Offset(5f, baseline), Offset(size.width - 5f, baseline), 1.2f)
            val visible = amplitudes.take(16)
            val maxAmplitude = visible.maxOrNull()?.coerceAtLeast(0.001f) ?: 1f
            visible.forEachIndexed { index, amplitude ->
                val x = size.width * (index + 1f) / (visible.size + 1f)
                val barHeight = (amplitude / maxAmplitude).coerceIn(0f, 1f) * (baseline - 10f)
                val color = if (index == 0) SonicPalette.Cyan else SonicPalette.Violet
                drawLine(color.copy(alpha = 0.16f), Offset(x, baseline), Offset(x, baseline - barHeight), 14f, StrokeCap.Round)
                drawLine(color, Offset(x, baseline), Offset(x, baseline - barHeight), 5f, StrokeCap.Round)
                drawCircle(color, radius = 3.5f, center = Offset(x, baseline - barHeight))
            }
        }
        Text(
            amplitudes.take(8).mapIndexed { index, amplitude ->
                "${index + 1}f ${(fundamentalHz * (index + 1)).toInt()} Hz · ${(amplitude * 100).toInt()}%"
            }.joinToString("   "),
            style = MaterialTheme.typography.labelSmall,
            fontSize = 8.sp,
            color = SonicPalette.Muted
        )
    }
}

private fun scientific(value: Double): String = when {
    value == 0.0 -> "0"
    value >= 1_000.0 || value < 0.001 -> String.format(Locale.US, "%.2e", value)
    else -> String.format(Locale.US, "%.4f", value)
}
