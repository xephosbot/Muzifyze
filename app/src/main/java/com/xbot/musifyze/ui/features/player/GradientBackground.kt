package com.xbot.musifyze.ui.features.player

import androidx.compose.animation.core.withInfiniteAnimationFrameMillis
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.xbot.musifyze.ui.components.drawShader
import org.intellij.lang.annotations.Language

@Composable
fun GradientBackground(
    colorVibrant: Color = Color.Cyan,
    colorMuted: Color = Color.Magenta,
    colorSurface: Color = MaterialTheme.colors.surface,
    content: @Composable () -> Unit
) {
    val time by produceState(0f) {
        while (true) {
            withInfiniteAnimationFrameMillis {
                value = it / 1000f
            }
        }
    }

    val scale = with(LocalDensity.current) { DefaultScale.toPx() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .drawShader(GRADIENT_SHADER) {
                setColorUniform("iColorVibrant", colorVibrant.toArgb())
                setColorUniform("iColorMuted", colorMuted.toArgb())
                setColorUniform("iColorSurface", colorSurface.toArgb())
                setFloatUniform("iResolution", size.width, size.height)
                setFloatUniform("iOffset", 0f, -size.height)
                setFloatUniform("iTime", time)
                setFloatUniform("iScale", scale)
            },
        content = { content() }
    )
}

@Language("AGSL")
private val GRADIENT_SHADER = """
    layout(color) uniform half4 iColorVibrant;   
    layout(color) uniform half4 iColorMuted;
    layout(color) uniform half4 iColorSurface;
    uniform float2 iResolution;
    uniform float2 iOffset;
    uniform float iTime;
    uniform float iScale;
    
    const float radius = 2.0;
    const float PI = 3.141592653589793238;
    
    float smootherstep(float a, float b, float x) {
        x = clamp((x - a)/(b - a), 0.0, 1.0);
        return x * x * x * (x * (x * 6.0 - 15.0) + 10.0);
    }
    
    half4 main(in float2 fragCoord) {
        float2 uv = (fragCoord * 2.0 - iResolution.xy) / iResolution.y;
        uv /= (iScale / iResolution.y);
        uv += iOffset.xy / iResolution.xy;
        
        float2 rotate1 = float2(cos(iTime) * 1.5, sin(2.0 * iTime)) * 0.5;
        float2 rotate2 = float2(cos(iTime + PI) * 1.5, sin(2.0 * iTime + PI)) * 0.5;
        
        float gradient1 = smootherstep(radius, -radius, length(uv - rotate1));
        float gradient2 = smootherstep(radius, -radius, length(uv - rotate2));
        
        half4 color = gradient1 * iColorVibrant + gradient2 * iColorMuted;
        half4 background = (1.0 - (gradient1 + gradient2)) * iColorSurface;
        half4 finalColor = color + background;
        
        return finalColor;
    }
""".trimIndent()

private val DefaultScale = 400.dp