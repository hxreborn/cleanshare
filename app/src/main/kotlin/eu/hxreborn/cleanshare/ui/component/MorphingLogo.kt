@file:Suppress("ktlint:standard:function-naming")

package eu.hxreborn.cleanshare.ui.component

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.toPath
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.center
import androidx.compose.ui.graphics.Matrix
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.graphicsLayer
import androidx.graphics.shapes.Morph
import eu.hxreborn.cleanshare.ui.util.rememberReducedMotion
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlin.math.floor

private const val COOKIE_SPIN_MS = 8_000
private const val FULL_TURN = 360f
private const val LOGO_SHAPE_MILLIS = 3_200
private const val LOGO_MORPH_SHARE = 0.55f
private const val LOGO_TAP_PULSE = 0.45f

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
private val LogoPolygons =
    listOf(
        MaterialShapes.Cookie9Sided,
        MaterialShapes.Sunny,
        MaterialShapes.Cookie12Sided,
        MaterialShapes.Clover8Leaf,
    )

@Stable
class LogoPulse(
    private val scope: CoroutineScope,
    private val enabled: Boolean,
) {
    val morph = Animatable(0f)

    fun tap() {
        if (!enabled) return
        scope.launch {
            morph.animateTo(LOGO_TAP_PULSE, spring(dampingRatio = 0.55f, stiffness = 900f))
            morph.animateTo(0f, spring(dampingRatio = 0.75f, stiffness = 260f))
        }
    }
}

@Composable
fun rememberLogoPulse(): LogoPulse {
    val scope = rememberCoroutineScope()
    val enabled = !rememberReducedMotion()
    return remember(scope, enabled) { LogoPulse(scope, enabled) }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun MorphingLogo(
    pulse: LogoPulse,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    val reducedMotion = rememberReducedMotion()
    val morphs =
        remember {
            LogoPolygons.indices.map {
                Morph(LogoPolygons[it].normalized(), LogoPolygons[(it + 1) % LogoPolygons.size].normalized())
            }
        }
    val scaleFactor = remember { rotationSafeScale(LogoPolygons) }
    val path = remember { Path() }
    val scaleMatrix = remember { Matrix() }
    val spinMatrix = remember { Matrix() }
    val easing = remember { CubicBezierEasing(0.4f, 0f, 0.2f, 1f) }
    val rotation: State<Float>
    val cycle: State<Float>
    if (reducedMotion) {
        rotation = remember { mutableFloatStateOf(0f) }
        cycle = remember { mutableFloatStateOf(0f) }
    } else {
        val infinite = rememberInfiniteTransition(label = "cookie")
        rotation =
            infinite.animateFloat(
                initialValue = 0f,
                targetValue = FULL_TURN,
                animationSpec = infiniteRepeatable(tween(COOKIE_SPIN_MS, easing = LinearEasing)),
                label = "rotation",
            )
        cycle =
            infinite.animateFloat(
                initialValue = 0f,
                targetValue = LogoPolygons.size.toFloat(),
                animationSpec = infiniteRepeatable(tween(LOGO_SHAPE_MILLIS * LogoPolygons.size, easing = LinearEasing)),
                label = "morph",
            )
    }
    Box(
        modifier
            .graphicsLayer()
            .drawWithCache {
                val center = size.center
                onDrawWithContent {
                    val progress = cycle.value + pulse.morph.value
                    val whole = floor(progress)
                    val index = Math.floorMod(whole.toInt(), morphs.size)
                    val segment = ((progress - whole) / LOGO_MORPH_SHARE).coerceIn(0f, 1f)
                    val shape = fitted(morphs[index].toPath(easing.transform(segment), path), size, scaleFactor, scaleMatrix)
                    spinMatrix.reset()
                    spinMatrix.translate(center.x, center.y)
                    spinMatrix.rotateZ(rotation.value)
                    spinMatrix.translate(-center.x, -center.y)
                    shape.transform(spinMatrix)
                    clipPath(shape) { this@onDrawWithContent.drawContent() }
                }
            },
        contentAlignment = Alignment.Center,
        content = content,
    )
}
