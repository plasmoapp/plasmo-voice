// MC 1.17+
package gg.essential.universal.shader
//#if MC>11700

import com.google.common.collect.ImmutableMap
import com.mojang.blaze3d.shaders.Uniform
import com.mojang.blaze3d.systems.RenderSystem
import com.mojang.blaze3d.vertex.DefaultVertexFormat
import com.mojang.blaze3d.vertex.VertexFormat
import net.minecraft.client.renderer.ShaderInstance
import net.minecraft.resources.ResourceLocation
import org.apache.commons.codec.digest.DigestUtils
import java.io.FileNotFoundException
import kotlin.NoSuchElementException

//#if MC>=11903
import gg.essential.universal.DummyPack
//#endif

//#if MC>=11900
import net.minecraft.server.packs.resources.Resource
import java.util.Optional
//#else
//$$ import net.minecraft.server.packs.resources.SimpleResource
//#endif

//#if MC>=12100
//$$ import com.mojang.blaze3d.vertex.VertexFormatElement
//#endif

internal class MCShader(
    private val mc: ShaderInstance,
    private val blendState: BlendState
) : UShader {
    override var usable = true

    override fun bind() {
        RenderSystem.setShader(::mc)

        // MC's GlBlendState is fundamentally broken because it is lazy in that it does not update anything
        // if the previously active blend state matches this one. But that assumes that it is the only method which
        // can modify the global GL state, which is just a horrible assumption and MC itself immediately violates
        // it in RenderLayer.
        // So, to actually get our state applied, we gotta do it ourselves.
        blendState.activate()
    }

    override fun unbind() {
        RenderSystem.setShader { null }
    }

    private fun getUniformOrNull(name: String) = mc.getUniform(name)?.let(::MCShaderUniform)
    override fun getIntUniformOrNull(name: String) = getUniformOrNull(name)
    override fun getFloatUniformOrNull(name: String) = getUniformOrNull(name)
    override fun getFloat2UniformOrNull(name: String) = getUniformOrNull(name)
    override fun getFloat3UniformOrNull(name: String) = getUniformOrNull(name)
    override fun getFloat4UniformOrNull(name: String) = getUniformOrNull(name)
    override fun getFloatMatrixUniformOrNull(name: String) = getUniformOrNull(name)
    override fun getSamplerUniformOrNull(name: String) = MCSamplerUniform(mc, name)

    companion object {
        private val DEBUG_LEGACY = System.getProperty("universalcraft.shader.legacy.debug", "") == "true"

        fun fromLegacyShader(vertSource: String, fragSource: String, blendState: BlendState, vertexFormat: VertexFormat?): MCShader {
            val transformer = ShaderTransformer(vertexFormat)

            val transformedVertSource = transformer.transform(vertSource)
            val transformedFragSource = transformer.transform(fragSource)

            val json = """
                {
                    "blend": {
                        "func": "${blendState.equation.mcStr}",
                        "srcrgb": "${blendState.srcRgb.mcStr}",
                        "dstrgb": "${blendState.dstRgb.mcStr}",
                        "srcalpha": "${blendState.srcAlpha.mcStr}",
                        "dstalpha": "${blendState.dstAlpha.mcStr}"
                    },
                    "vertex": "${DigestUtils.sha1Hex(transformedVertSource).lowercase()}",
                    "fragment": "${DigestUtils.sha1Hex(transformedFragSource).lowercase()}",
                    "attributes": [ ${transformer.attributes.joinToString { "\"$it\"" }} ],
                    "samplers": [
                        ${transformer.samplers.joinToString(",\n") { "{ \"name\": \"$it\" }" }}
                    ],
                    "uniforms": [
                        ${transformer.uniforms.map { (name, type) -> """
                            { "name": "$name", "type": "${type.typeName}", "count": ${type.default.size}, "values": [ ${type.default.joinToString()} ] }
                        """.trimIndent() }.joinToString(",\n")}
                    ]
                }
            """.trimIndent()

            if (DEBUG_LEGACY) {
                println(transformedVertSource)
                println(transformedFragSource)
                println(json)
            }

            val factory = { id: ResourceLocation ->
                val content = when {
                    id.path.endsWith(".json") -> json
                    id.path.endsWith(".vsh") -> transformedVertSource
                    id.path.endsWith(".fsh") -> transformedFragSource
                    else -> throw FileNotFoundException(id.toString())
                }
                //#if MC>=11903
                Optional.of(Resource(DummyPack, content::byteInputStream))
                //#elseif MC>=11900
                //$$ Optional.of(Resource("__generated__", content::byteInputStream))
                //#else
                //$$ SimpleResource("__generated__", id, content.byteInputStream(), null)
                //#endif
            }

            val shaderVertexFormat = if (vertexFormat != null) {
                // Shader calls glBindAttribLocation using the names in the VertexFormat, not the shader json...
                // Easiest way to work around this is to construct a custom VertexFormat with our prefixed names.
                //#if MC>=12100
                //$$ VertexFormat.builder()
                //$$     .apply {
                //$$         transformer.attributes.forEachIndexed { index, name ->
                //$$             add(name, vertexFormat.elements[index])
                //$$         }
                //$$     }
                //$$     .build()
                //#else
                VertexFormat(ImmutableMap.copyOf(
                    transformer.attributes.withIndex()
                        .associate { it.value to vertexFormat.elements[it.index] }))
                //#endif
            } else {
                // Legacy fallback: The actual element doesn't matter here, Shader only cares about the names
                //#if MC>=12100
                //$$ VertexFormat.builder()
                //$$     .apply {
                //$$         transformer.attributes.forEachIndexed { index, name ->
                //$$             add(name, VertexFormatElement.POSITION)
                //$$         }
                //$$     }
                //$$     .build()
                //#else
                VertexFormat(ImmutableMap.copyOf(transformer.attributes.associateWith { DefaultVertexFormat.ELEMENT_POSITION }))
                //#endif
            }


            val name = DigestUtils.sha1Hex(json).lowercase()
            return MCShader(ShaderInstance(factory, name, shaderVertexFormat), blendState)
        }
    }
}

internal class MCShaderUniform(val mc: Uniform) : ShaderUniform, IntUniform, FloatUniform, Float2Uniform, Float3Uniform, Float4Uniform, FloatMatrixUniform {
    override val location: Int
        get() = mc.location

    override fun setValue(value: Int) = mc.set(value)

    override fun setValue(value: Float) = mc.set(value)

    override fun setValue(v1: Float, v2: Float) = mc.set(v1, v2)

    override fun setValue(v1: Float, v2: Float, v3: Float) = mc.set(v1, v2, v3)

    override fun setValue(v1: Float, v2: Float, v3: Float, v4: Float) = mc.set(v1, v2, v3, v4)

    override fun setValue(array: FloatArray) = mc.set(array)
}

internal class MCSamplerUniform(val mc: ShaderInstance, val name: String) : SamplerUniform {
    override val location: Int = 0

    override fun setValue(textureId: Int) {
        mc.setSampler(name, textureId)
    }
}

internal class ShaderTransformer(private val vertexFormat: VertexFormat?) {
    val attributes = mutableListOf<String>()
    val samplers = mutableSetOf<String>()
    val uniforms = mutableMapOf<String, UniformType>()

    fun transform(originalSource: String): String {
        var source = originalSource

        source = source.replace("gl_ModelViewProjectionMatrix", "gl_ProjectionMatrix * gl_ModelViewMatrix")
        source = source.replace("texture2D", "texture")

        val replacements = mutableMapOf<String, String>()
        val transformed = mutableListOf<String>()
        transformed.add("#version 150")

        val frag = "gl_FragColor" in source
        val vert = !frag

        if (frag) {
            transformed.add("out vec4 uc_FragColor;")
            replacements["gl_FragColor"] = "uc_FragColor"
        }

        if (vert && "gl_FrontColor" in source) {
            transformed.add("out vec4 uc_FrontColor;")
            replacements["gl_FrontColor"] = "uc_FrontColor"
        }
        if (frag && "gl_Color" in source) {
            transformed.add("in vec4 uc_FrontColor;")
            replacements["gl_Color"] = "uc_FrontColor"
        }

        fun replaceAttribute(newAttributes: MutableList<Pair<String, String>>, needle: String, type: String, replacementName: String = "uc_" + needle.substringAfter("_"), replacement: String = replacementName) {
            if (needle in source) {
                replacements[needle] = replacement
                newAttributes.add(replacementName to "in $type $replacementName;")
            }
        }
        if (vert) {
            val newAttributes = mutableListOf<Pair<String, String>>()
            replaceAttribute(newAttributes, "gl_Vertex", "vec3", "uc_Position", replacement = "vec4(uc_Position, 1.0)")
            replaceAttribute(newAttributes, "gl_Color", "vec4")
            replaceAttribute(newAttributes, "gl_MultiTexCoord0.st", "vec2", "uc_UV0")
            replaceAttribute(newAttributes, "gl_MultiTexCoord1.st", "vec2", "uc_UV1")
            replaceAttribute(newAttributes, "gl_MultiTexCoord2.st", "vec2", "uc_UV2")

            if (vertexFormat != null) {
                newAttributes.sortedBy { vertexFormat.elementAttributeNames.indexOf(it.first.removePrefix("uc_")) }
                    .forEach {
                        attributes.add(it.first)
                        transformed.add(it.second)
                    }
            } else {
                newAttributes.forEach {
                    attributes.add(it.first)
                    transformed.add(it.second)
                }
            }
        }

        fun replaceUniform(needle: String, type: UniformType, replacementName: String, replacement: String = replacementName) {
            if (needle in source) {
                replacements[needle] = replacement
                if (replacementName !in uniforms) {
                    uniforms[replacementName] = type
                    transformed.add("uniform ${type.glslName} $replacementName;")
                }
            }
        }
        replaceUniform("gl_ModelViewMatrix", UniformType.Mat4, "ModelViewMat")
        replaceUniform("gl_ProjectionMatrix", UniformType.Mat4, "ProjMat")


        for (line in source.lines()) {
            transformed.add(when {
                line.startsWith("#version") -> continue
                line.startsWith("varying ") -> (if (frag) "in " else "out ") + line.substringAfter("varying ")
                line.startsWith("uniform ") -> {
                    val (_, glslType, name) = line.trimEnd(';').split(" ")
                    if (glslType == "sampler2D") {
                        samplers.add(name)
                    } else {
                        uniforms[name] = UniformType.fromGlsl(glslType)
                    }
                    line
                }
                else -> replacements.entries.fold(line) { acc, (needle, replacement) -> acc.replace(needle, replacement) }
            })
        }

        return transformed.joinToString("\n")
    }
}

internal enum class UniformType(val typeName: String, val glslName: String, val default: IntArray) {
    Int1("int", "int", intArrayOf(0)),
    Float1("float", "float", intArrayOf(0)),
    Float2("float", "vec2", intArrayOf(0, 0)),
    Float3("float", "vec3", intArrayOf(0, 0, 0)),
    Float4("float", "vec4", intArrayOf(0, 0, 0, 0)),
    Mat2("matrix2x2", "mat2", intArrayOf(1, 0, 0, 1)),
    Mat3("matrix3x3", "mat3", intArrayOf(1, 0, 0, 0, 1, 0, 0, 0, 1)),
    Mat4("matrix4x4", "mat4", intArrayOf(1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1)),
    ;

    companion object {
        fun fromGlsl(glslName: String): UniformType =
            values().find { it.glslName == glslName } ?: throw NoSuchElementException(glslName)
    }
}
//#endif
