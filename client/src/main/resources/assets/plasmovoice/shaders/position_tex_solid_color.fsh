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
//#elseif MC>=1.21.6
//$$ #version 150
//$$
//$$ #moj_import <minecraft:dynamictransforms.glsl>
//$$
//$$ uniform sampler2D Sampler0;
//$$
//$$ in vec2 texCoord0;
//$$ in vec4 vertexColor;
//$$
//$$ out vec4 fragColor;
//#else
#version 110

uniform sampler2D Sampler0;

varying vec2 texCoord0;
varying vec4 vertexColor;
//#endif

void main() {
    //#if MC>=1.21.6
    //$$ vec4 color = texture(Sampler0, texCoord0);
    //$$ if (color.a < 0.1) {
    //$$     discard;
    //$$ }
    //$$
    //$$ fragColor = vertexColor;
    //#else
    vec4 color = texture2D(Sampler0, texCoord0.st);
    if (color.a < 0.1) {
        discard;
    }

    gl_FragColor = vertexColor;
    //#endif
}
