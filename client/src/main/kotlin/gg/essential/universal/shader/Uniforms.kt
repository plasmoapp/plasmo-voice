package gg.essential.universal.shader

interface ShaderUniform {
    val location: Int
}

interface IntUniform : ShaderUniform {
    fun setValue(value: Int)
}

interface FloatUniform : ShaderUniform {
    fun setValue(value: Float)
}

interface Float2Uniform : ShaderUniform {
    fun setValue(v1: Float, v2: Float)
}

interface Float3Uniform : ShaderUniform {
    fun setValue(v1: Float, v2: Float, v3: Float)
}

interface Float4Uniform : ShaderUniform {
    fun setValue(v1: Float, v2: Float, v3: Float, v4: Float)
}

interface FloatMatrixUniform : ShaderUniform {
    fun setValue(array: FloatArray)
}

interface SamplerUniform : ShaderUniform {
    fun setValue(textureId: Int)
}
