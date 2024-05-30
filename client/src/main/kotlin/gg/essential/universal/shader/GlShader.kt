package gg.essential.universal.shader

//#if MC<11700
//$$ import com.mojang.blaze3d.platform.GlStateManager
//$$ import com.mojang.blaze3d.systems.RenderSystem
//$$ import org.lwjgl.opengl.ARBShaderObjects
//$$ import org.lwjgl.opengl.ARBShaderObjects.*
//$$ import org.lwjgl.opengl.GL11
//$$ import org.lwjgl.opengl.GL11.GL_TEXTURE_BINDING_2D
//$$ import org.lwjgl.opengl.GL13.GL_ACTIVE_TEXTURE
//$$ import org.lwjgl.opengl.GL13.GL_TEXTURE0
//$$ import org.lwjgl.opengl.GL20
//$$ import org.lwjgl.opengl.GL20.*
//$$
//$$ internal class GlShader(
//$$     private val vertSource: String,
//$$     private val fragSource: String,
//$$     private val blendState: BlendState,
//$$ ) : UShader {
//$$     private var program: Int = GlStateManager.glCreateProgram()
//$$     private var vertShader: Int = GlStateManager.glCreateShader(GL20.GL_VERTEX_SHADER)
//$$     private var fragShader: Int = GlStateManager.glCreateShader(GL20.GL_FRAGMENT_SHADER)
//$$     private var samplers = mutableMapOf<String, DirectSamplerUniform>()
//$$
//$$     override var usable = false
//$$     var bound = false
//$$         private set
//$$     private var prevActiveTexture = 0
//$$     private var prevTextureBindings = mutableMapOf<Int, Int>()
//$$     private var prevBlendState: BlendState? = null
//$$
//$$     init {
//$$         createShader()
//$$     }
//$$
//$$     override fun bind() {
//$$         prevActiveTexture = GL11.glGetInteger(GL_ACTIVE_TEXTURE)
//$$         for (sampler in samplers.values) {
//$$             doBindTexture(sampler.textureUnit, sampler.textureId)
//$$         }
//$$         prevBlendState = BlendState.active()
//$$         blendState.activate()
//$$
//$$         GlStateManager._glUseProgram(program)
//$$         bound = true
//$$     }
//$$
//$$     internal fun doBindTexture(textureUnit: Int, textureId: Int) {
//$$         RenderSystem.activeTexture(GL_TEXTURE0 + textureUnit)
//$$         prevTextureBindings.computeIfAbsent(textureUnit) { GL11.glGetInteger(GL_TEXTURE_BINDING_2D) }
//$$         RenderSystem.bindTexture(textureId)
//$$     }
//$$
//$$     override fun unbind() {
//$$         for ((textureUnit, textureId) in prevTextureBindings) {
//$$             RenderSystem.activeTexture(GL_TEXTURE0 + textureUnit)
//$$             RenderSystem.bindTexture(textureId)
//$$         }
//$$         prevTextureBindings.clear()
//$$         RenderSystem.activeTexture(prevActiveTexture)
//$$         prevBlendState?.activate()
//$$
//$$         GlStateManager._glUseProgram(0)
//$$         bound = false
//$$     }
//$$
//$$     internal inline fun withProgramBound(block: () -> Unit) {
//$$         if (bound) {
//$$             block()
//$$         } else {
//$$             val prevProgram = GL11.glGetInteger(GL_CURRENT_PROGRAM)
//$$             try {
//$$                 GlStateManager._glUseProgram(program)
//$$                 block()
//$$             } finally {
//$$                 GlStateManager._glUseProgram(prevProgram)
//$$             }
//$$         }
//$$     }
//$$
//$$     private fun getUniformLocation(uniformName: String): Int? {
//$$         val loc = if (CORE) {
//$$             glGetUniformLocation(program, uniformName)
//$$         } else {
//$$             glGetUniformLocationARB(program, uniformName)
//$$         }
//$$         return if (loc == -1) null else loc
//$$     }
//$$
//$$     override fun getIntUniformOrNull(name: String) = getUniformLocation(name)?.let(::DirectIntUniform)
//$$     override fun getFloatUniformOrNull(name: String) = getUniformLocation(name)?.let(::DirectFloatUniform)
//$$     override fun getFloat2UniformOrNull(name: String) = getUniformLocation(name)?.let(::DirectFloat2Uniform)
//$$     override fun getFloat3UniformOrNull(name: String) = getUniformLocation(name)?.let(::DirectFloat3Uniform)
//$$     override fun getFloat4UniformOrNull(name: String) = getUniformLocation(name)?.let(::DirectFloat4Uniform)
//$$     override fun getFloatMatrixUniformOrNull(name: String) = getUniformLocation(name)?.let(::DirectFloatMatrixUniform)
//$$     override fun getSamplerUniformOrNull(name: String): SamplerUniform? {
//$$         samplers[name]?.let { return it }
//$$         val loc = getUniformLocation(name) ?: return null
//$$         val uniform = DirectSamplerUniform(loc, samplers.size, this)
//$$         samplers[name] = uniform
//$$         return uniform
//$$     }
//$$
//$$     private fun createShader() {
//$$         for ((shader, source) in listOf(vertShader to vertSource, fragShader to fragSource)) {
//$$             if (CORE) glShaderSource(shader, source) else glShaderSourceARB(shader, source)
//$$             GlStateManager.glCompileShader(shader)
//$$
//$$             if (GlStateManager.glGetShaderi(shader, GL20.GL_COMPILE_STATUS) != 1) {
//$$                 println(GlStateManager.glGetShaderInfoLog(shader, 32768))
//$$                 return
//$$             }
//$$
//$$             GlStateManager.glAttachShader(program, shader)
//$$         }
//$$
//$$         GlStateManager.glLinkProgram(program)
//$$
//$$         if (CORE) {
//$$             GL20.glDetachShader(program, vertShader)
//$$             GL20.glDetachShader(program, fragShader)
//$$             GL20.glDeleteShader(vertShader)
//$$             GL20.glDeleteShader(fragShader)
//$$         } else {
//$$             ARBShaderObjects.glDetachObjectARB(program, vertShader)
//$$             ARBShaderObjects.glDetachObjectARB(program, fragShader)
//$$             ARBShaderObjects.glDeleteObjectARB(vertShader)
//$$             ARBShaderObjects.glDeleteObjectARB(fragShader)
//$$         }
//$$
//$$         if (GlStateManager.glGetProgrami(program, GL20.GL_LINK_STATUS) != 1) {
//$$             println(GlStateManager.glGetProgramInfoLog(program, 32768))
//$$             return
//$$         }
//$$
//$$         if (CORE) glValidateProgram(program) else glValidateProgramARB(program)
//$$
//$$         if (GlStateManager.glGetProgrami(program, GL20.GL_VALIDATE_STATUS) != 1) {
//$$             println(GlStateManager.glGetProgramInfoLog(program, 32768))
//$$             return
//$$         }
//$$
//$$         usable = true
//$$     }
//$$
//$$     companion object {
//$$         val CORE = true
//$$     }
//$$ }
//$$
//$$ internal abstract class DirectShaderUniform(override val location: Int) : ShaderUniform
//$$
//$$ internal class DirectIntUniform(location: Int) : DirectShaderUniform(location), IntUniform {
//$$     override fun setValue(value: Int) {
//$$         if (GlShader.CORE) GL20.glUniform1i(location, value)
//$$         else ARBShaderObjects.glUniform1iARB(location, value)
//$$     }
//$$ }
//$$
//$$ internal class DirectFloatUniform(location: Int) : DirectShaderUniform(location), FloatUniform {
//$$     override fun setValue(value: Float) {
//$$         if (GlShader.CORE) GL20.glUniform1f(location, value)
//$$         else ARBShaderObjects.glUniform1fARB(location, value)
//$$     }
//$$ }
//$$
//$$ internal class DirectFloat2Uniform(location: Int) : DirectShaderUniform(location), Float2Uniform {
//$$     override fun setValue(v1: Float, v2: Float) {
//$$         if (GlShader.CORE) GL20.glUniform2f(location, v1, v2)
//$$         else ARBShaderObjects.glUniform2fARB(location, v1, v2)
//$$     }
//$$ }
//$$
//$$ internal class DirectFloat3Uniform(location: Int) : DirectShaderUniform(location), Float3Uniform {
//$$     override fun setValue(v1: Float, v2: Float, v3: Float) {
//$$         if (GlShader.CORE) GL20.glUniform3f(location, v1, v2, v3)
//$$         else ARBShaderObjects.glUniform3fARB(location, v1, v2, v3)
//$$     }
//$$ }
//$$
//$$ internal class DirectFloat4Uniform(location: Int) : DirectShaderUniform(location), Float4Uniform {
//$$     override fun setValue(v1: Float, v2: Float, v3: Float, v4: Float) {
//$$         if (GlShader.CORE) GL20.glUniform4f(location, v1, v2, v3, v4)
//$$         else ARBShaderObjects.glUniform4fARB(location, v1, v2, v3, v4)
//$$     }
//$$ }
//$$
//$$ internal class DirectFloatMatrixUniform(location: Int) : DirectShaderUniform(location), FloatMatrixUniform {
//$$     override fun setValue(array: FloatArray) {
//$$         //#if MC<11400
//$$         //$$ val buffer = ByteBuffer.allocateDirect(array.size * 4).order(ByteOrder.nativeOrder())
//$$         //$$ val floatBuffer = buffer.asFloatBuffer()
//$$         //$$ floatBuffer.put(array)
//$$         //$$ buffer.rewind()
//$$         //#endif
//$$         when (array.size) {
//$$             //#if MC>=11400
//$$             4 -> if (GlShader.CORE) GL20.glUniformMatrix2fv(location, false, array)
//$$             else ARBShaderObjects.glUniformMatrix2fvARB(location, false, array)
//$$             9 -> if (GlShader.CORE) GL20.glUniformMatrix3fv(location, false, array)
//$$             else ARBShaderObjects.glUniformMatrix3fvARB(location, false, array)
//$$             16 -> if (GlShader.CORE) GL20.glUniformMatrix4fv(location, false, array)
//$$             else ARBShaderObjects.glUniformMatrix4fvARB(location, false, array)
//$$             //#else
//$$             //$$ 4 -> if (GlShader.CORE) GL20.glUniformMatrix2(location, false, floatBuffer)
//$$             //$$ else ARBShaderObjects.glUniformMatrix2ARB(location, false, floatBuffer)
//$$             //$$ 9 -> if (GlShader.CORE) GL20.glUniformMatrix3(location, false, floatBuffer)
//$$             //$$ else ARBShaderObjects.glUniformMatrix3ARB(location, false, floatBuffer)
//$$             //$$ 16 -> if (GlShader.CORE) GL20.glUniformMatrix4(location, false, floatBuffer)
//$$             //$$ else ARBShaderObjects.glUniformMatrix4ARB(location, false, floatBuffer)
//$$             //#endif
//$$             else -> throw IllegalArgumentException()
//$$         }
//$$     }
//$$ }
//$$
//$$ internal class DirectSamplerUniform(
//$$     location: Int,
//$$     val textureUnit: Int,
//$$     private val shader: GlShader,
//$$ ) : DirectShaderUniform(location), SamplerUniform {
//$$     var textureId: Int = 0
//$$
//$$     init {
//$$         shader.withProgramBound {
//$$             DirectIntUniform(location).setValue(textureUnit)
//$$         }
//$$     }
//$$
//$$     override fun setValue(textureId: Int) {
//$$         this.textureId = textureId
//$$
//$$         if (shader.bound) {
//$$             shader.doBindTexture(textureUnit, textureId)
//$$         }
//$$     }
//$$ }
//$$
//#endif
