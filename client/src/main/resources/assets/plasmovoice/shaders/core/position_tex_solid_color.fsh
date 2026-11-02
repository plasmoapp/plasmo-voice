//#if MC>=26.3
//$$ #version 330
//$$ #extension GL_ARB_separate_shader_objects : require
//$$
//$$ #include <minecraft:dynamictransforms.glsl>
//$$
//$$ uniform sampler2D Sampler0;
//$$
//$$ layout(location = 0) in vec2 texCoord0;
//$$ layout(location = 1) in vec4 vertexColor;
//$$
//$$ layout(location = 0) out vec4 fragColor;
//#else
#version 150

//#if MC>=1.21.6
//$$ #moj_import <minecraft:dynamictransforms.glsl>
//$$
//#endif
uniform sampler2D Sampler0;

in vec2 texCoord0;
in vec4 vertexColor;

out vec4 fragColor;
//#endif

void main() {
    vec4 color = texture(Sampler0, texCoord0);
    if (color.a < 0.1) {
        discard;
    }

    fragColor = vertexColor;
}
