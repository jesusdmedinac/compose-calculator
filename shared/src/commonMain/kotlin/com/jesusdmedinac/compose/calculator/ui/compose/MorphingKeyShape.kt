package com.jesusdmedinac.compose.calculator.ui.compose

import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin

private const val SAMPLES = 120
private const val COOKIE_DEPTH = 0.14f

/**
 * A superellipse that can grow scalloped "cookie" lobes, so a single shape can
 * morph continuously between a circle ([exponent] 2), a squircle ([exponent] ~4)
 * and the Material 3 Expressive cookie ([cookie] 1).
 */
class MorphingKeyShape(
    private val exponent: Float,
    private val cookie: Float = 0f,
    private val rotationDegrees: Float = 0f,
    private val lobes: Int = 8,
) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density,
    ): Outline {
        val path = Path()
        val radiusX = size.width / 2f
        val radiusY = size.height / 2f
        val rotation = rotationDegrees * PI.toFloat() / 180f
        for (i in 0 until SAMPLES) {
            val angle = 2f * PI.toFloat() * i / SAMPLES
            val cos = cos(angle)
            val sin = sin(angle)
            val radius = (abs(cos).pow(exponent) + abs(sin).pow(exponent)).pow(-1f / exponent)
            val lobe = 1f - COOKIE_DEPTH * cookie * (0.5f - 0.5f * cos(lobes * (angle + rotation)))
            val x = radiusX + radiusX * radius * cos * lobe
            val y = radiusY + radiusY * radius * sin * lobe
            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        path.close()
        return Outline.Generic(path)
    }
}
