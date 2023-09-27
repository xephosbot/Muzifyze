package com.xbot.musifyze.ui.components

import android.graphics.BitmapShader
import android.graphics.Color
import android.graphics.RuntimeShader
import android.graphics.Shader
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.node.DrawModifierNode
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.platform.InspectorInfo
import androidx.compose.ui.platform.debugInspectorInfo

fun Modifier.drawShader(
    shader: String,
    params: ShaderScope.() -> Unit,
) = this then DrawShaderElement(
    shader = shader,
    params = params,
    inspectorInfo = debugInspectorInfo {
        name = "drawShader"
        properties["shader"] = shader
    }
)

private data class DrawShaderElement(
    private val shader: String,
    private val params: ShaderScope.() -> Unit,
    private val inspectorInfo: InspectorInfo.() -> Unit
) : ModifierNodeElement<DrawShaderNode>() {

    override fun create(): DrawShaderNode {
        return DrawShaderNode(
            shader = shader,
            params = params
        )
    }

    override fun update(node: DrawShaderNode) {
        node.shader = shader
        node.params = params
    }

    override fun InspectorInfo.inspectableProperties() {
        inspectorInfo()
    }

    override fun hashCode(): Int {
        return shader.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        val otherModifier = other as? DrawShaderElement ?: return false
        return shader == otherModifier.shader && params == otherModifier.params
    }
}

private class DrawShaderNode(
    var shader: String,
    var params: ShaderScope.() -> Unit
) : Modifier.Node(), DrawModifierNode {

    private var lastSize: Size? = null
    private var shaderScope: ShaderScopeImpl? = null
    private var shaderBrush: ShaderBrush? = null

    override fun ContentDrawScope.draw() {
        drawShader()
        drawContent()
    }

    private fun ContentDrawScope.drawShader() {
        if (size != lastSize) {
            shaderScope = ShaderScopeImpl(shader, size)
            shaderBrush = ShaderBrush(shaderScope!!.runtimeShader)
        }
        shaderScope?.apply(params)
        shaderBrush?.let { drawRect(brush = it) }

        lastSize = size
    }
}

private class ShaderScopeImpl(
    private val shader: String,
    override val size: Size
) : ShaderScope {

    val runtimeShader: RuntimeShader by lazy { RuntimeShader(shader) }

    override fun setColorUniform(uniformName: String, color: Int) {
        runtimeShader.setColorUniform(uniformName, color)
    }

    override fun setColorUniform(uniformName: String, color: Long) {
        runtimeShader.setColorUniform(uniformName, color)
    }

    override fun setColorUniform(uniformName: String, color: Color) {
        runtimeShader.setColorUniform(uniformName, color)
    }

    override fun setFloatUniform(uniformName: String, value: Float) {
        runtimeShader.setFloatUniform(uniformName, value)
    }

    override fun setFloatUniform(uniformName: String, value1: Float, value2: Float) {
        runtimeShader.setFloatUniform(uniformName, value1, value2)
    }

    override fun setFloatUniform(uniformName: String, value1: Float, value2: Float, value3: Float) {
        runtimeShader.setFloatUniform(uniformName, value1, value2, value3)
    }

    override fun setFloatUniform(uniformName: String, value1: Float, value2: Float, value3: Float, value4: Float) {
        runtimeShader.setFloatUniform(uniformName, value1, value2, value3, value4)
    }

    override fun setFloatUniform(uniformName: String, values: FloatArray) {
        runtimeShader.setFloatUniform(uniformName, values)
    }

    override fun setIntUniform(uniformName: String, value: Int) {
        runtimeShader.setIntUniform(uniformName, value)
    }

    override fun setIntUniform(uniformName: String, value1: Int, value2: Int) {
        runtimeShader.setIntUniform(uniformName, value1, value2)
    }

    override fun setIntUniform(uniformName: String, value1: Int, value2: Int, value3: Int) {
        runtimeShader.setIntUniform(uniformName, value1, value2, value3)
    }

    override fun setIntUniform(uniformName: String, value1: Int, value2: Int, value3: Int, value4: Int) {
        runtimeShader.setIntUniform(uniformName, value1, value2, value3, value4)
    }

    override fun setIntUniform(uniformName: String, values: IntArray) {
        runtimeShader.setIntUniform(uniformName, values)
    }

    override fun setInputShader(shaderName: String, shader: Shader) {
        runtimeShader.setInputShader(shaderName, shader)
    }

    override fun setInputBuffer(shaderName: String, shader: BitmapShader) {
        runtimeShader.setInputBuffer(shaderName, shader)
    }
}

interface ShaderScope {

    val size: Size

    fun setColorUniform(uniformName: String, color: Int)

    fun setColorUniform(uniformName: String, color: Long)

    fun setColorUniform(uniformName: String, color: Color)

    fun setFloatUniform(uniformName: String, value: Float)

    fun setFloatUniform(uniformName: String, value1: Float, value2: Float)

    fun setFloatUniform(uniformName: String, value1: Float, value2: Float, value3: Float)

    fun setFloatUniform(uniformName: String, value1: Float, value2: Float, value3: Float, value4: Float)

    fun setFloatUniform(uniformName: String, values: FloatArray)

    fun setIntUniform(uniformName: String, value: Int)

    fun setIntUniform(uniformName: String, value1: Int, value2: Int)

    fun setIntUniform(uniformName: String, value1: Int, value2: Int, value3: Int)

    fun setIntUniform(uniformName: String, value1: Int, value2: Int, value3: Int, value4: Int)

    fun setIntUniform(uniformName: String, values: IntArray)

    fun setInputShader(shaderName: String, shader: Shader)

    fun setInputBuffer(shaderName: String, shader: BitmapShader)
}
