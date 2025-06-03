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
//$$         // blendState.activate()
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
//$$         // prevBlendState?.activate()
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
//$$ internal abstract class DirectShaderUniform(val location: Int)
//$$
//$$ internal class DirectIntUniform(location: Int) : DirectShaderUniform(location) {
//$$     fun setValue(value: Int) {
//$$         if (GlShader.CORE) GL20.glUniform1i(location, value)
//$$         else ARBShaderObjects.glUniform1iARB(location, value)
//$$     }
//$$ }
//$$
//$$ internal class DirectSamplerUniform(
//$$     location: Int,
//$$     val textureUnit: Int,
//$$     private val shader: GlShader,
//$$ ) : DirectShaderUniform(location) {
//$$     var textureId: Int = 0
//$$
//$$     init {
//$$         shader.withProgramBound {
//$$             DirectIntUniform(location).setValue(textureUnit)
//$$         }
//$$     }
//$$
//$$     fun setValue(textureId: Int) {
//$$         this.textureId = textureId
//$$
//$$         if (shader.bound) {
//$$             shader.doBindTexture(textureUnit, textureId)
//$$         }
//$$     }
//$$ }
//$$
//#endif
