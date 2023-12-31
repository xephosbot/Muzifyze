package com.xbot.musifyze.ui.features.radio

import android.graphics.BitmapShader
import android.graphics.Shader
import androidx.compose.animation.core.withInfiniteAnimationFrameMillis
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.xbot.musifyze.ui.components.drawShader
import org.intellij.lang.annotations.Language

@Composable
fun RadioScreenRoute(
    modifier: Modifier = Modifier,
    //viewModel: RadioViewModel = hiltViewModel()
) {
    /*val state by viewModel.state.collectAsStateWithLifecycle()

    RadioScreenContent(
        modifier = modifier,
        state = state
    )*/
}

@Composable
private fun RadioScreenContent(
    modifier: Modifier = Modifier,
    state: RadioScreenState
) {
    RadioShaderSurface(
        modifier = modifier.fillMaxSize(),
        fft = state.fft,
        waveform = state.waveform
    ) {
        /*Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            TextButton(onClick = { /*TODO*/ }) {
                Text(
                    text = "Моя волна",
                    color = MaterialTheme.colors.onSurface,
                    style = MaterialTheme.typography.h4,
                    fontWeight = FontWeight.Bold
                )
            }
        }*/
    }
}

@Composable
private fun RadioShaderSurface(
    modifier: Modifier = Modifier,
    fft: ByteArray,
    waveform: ByteArray,
    colorSurface: Color = MaterialTheme.colors.surface,
    colorPrimary: Color = MaterialTheme.colors.primaryVariant,
    content: @Composable () -> Unit
) {
    val time by produceState(0f) {
        while (true) {
            withInfiniteAnimationFrameMillis {
                value = it / 1000f
            }
        }
    }

    val radius = with(LocalDensity.current) { DefaultRadius.toPx() }

    val audioVisualizerState = rememberAudioVisualizerState()
    audioVisualizerState.UpdateBitmap(fft = fft, waveform = waveform)

    val shader = remember(audioVisualizerState) {
        BitmapShader(audioVisualizerState.bitmap, Shader.TileMode.DECAL, Shader.TileMode.DECAL)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .drawShader(FRAGMENT_SHADER) {
                setColorUniform("iColorSurface", colorSurface.toArgb())
                setColorUniform("iColorPrimary", colorPrimary.toArgb())
                setFloatUniform("iResolution", size.width, size.height)
                setFloatUniform("iRadius", radius)
                setFloatUniform("iTime", time)
                setInputBuffer("iChannel0", shader)
            },
        content = { content() }
    )
}

@Language("AGSL")
private val FRAGMENT_SHADER = """
    layout(color) uniform half4 iColorSurface;
    layout(color) uniform half4 iColorPrimary;
    uniform float2 iResolution;
    uniform float iRadius;
    uniform float iTime;
    
    uniform shader iChannel0;
    
    // Frequency values for different layers
    float frequencies[16];
    const float2 zeroOne = float2(0.0, 1.0);
    const float PI = 3.141592653589793238;
    
    // Rotate 2D vector by an angle
    float2x2 rotate2d(float angle) {
        return float2x2(cos(angle), -sin(angle), sin(angle), cos(angle));
    }
    
    // 2D Hash function
    float hash2d(float2 uv) {
        float f = uv.x + uv.y * 47.0;
        return fract(cos(f * 3.333) * 100003.9);
    }
    
    // Smoothly interpolate between two values
    float smoothInterpolation(float f0, float f1, float a) {
        return mix(f0, f1, a * a * (3.0 - 2.0 * a));
    }
    
    // 2D Perlin noise function
    float noise2d(float2 uv) {
        float2 fractUV = fract(uv.xy);
        float2 floorUV = floor(uv.xy);
        float h00 = hash2d(floorUV);
        float h10 = hash2d(floorUV + zeroOne.yx);
        float h01 = hash2d(floorUV + zeroOne);
        float h11 = hash2d(floorUV + zeroOne.yy);
        return smoothInterpolation(
            smoothInterpolation(h00, h10, fractUV.x),
            smoothInterpolation(h01, h11, fractUV.x),
            fractUV.y
        );
    }
    
    half4 main(in float2 fragCoord) { 
        float2 uv = fragCoord.xy / iResolution.xy;
        
        int tx = int(uv.x * 512.0);
        
        float fft = iChannel0.eval(vec2(tx, 1)).x; 
        
        float wave = iChannel0.eval(vec2(tx, 0)).x;
        
        // convert frequency to colors
        vec3 col = vec3(fft, 4.0 * fft * (1.0 - fft), 1.0 - fft) * fft;
        
        // add wave form on top	
        col += 1.0 - smoothstep(0.0, 0.15, abs(wave - uv.y));
        
        return vec4(col, 1.0);
        /*float2 uv = (fragCoord * 2.0 - iResolution.xy) / iResolution.y;
        uv /= (iRadius / iResolution.y);
      
        float2x2 rotate = rotate2d(iTime);
        float noise = noise2d(uv + rotate[0].xy);        
        float color = 0.0;
        
        for (int i = 0; i < 16; i++) {
            frequencies[i] = clamp(1.75 * pow(iChannel0.eval(vec2(0.05 + 0.5 * float(i) / 16.0, 1.0)).r, 4.0), 0.0, 1.0);
            //frequencies[i] = sin(iTime * (float(i) / 10000.0) + float(i) * 0.1234) * 0.25;
            
            float wave = sqrt(sin((-(frequencies[i] * noise * PI) + ((uv.x * uv.x) + (uv.y * uv.y)))));
            wave = smoothstep(0.8, 1.0, wave);
            color += wave * frequencies[i] * 0.2;
            wave = smoothstep(0.99999, 1.0, wave);
            color += wave * 0.2;
        }
      
        return mix(iColorSurface, iColorPrimary, color);*/
    }
""".trimIndent()

private val DefaultRadius = 220.dp
