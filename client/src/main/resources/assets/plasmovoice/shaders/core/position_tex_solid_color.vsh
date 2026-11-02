//#if MC>=26.3
//$$ #version 330
//$$ #extension GL_ARB_separate_shader_objects : require
//$$
//$$ #include <minecraft:dynamictransforms.glsl>
//$$ #include <minecraft:projection.glsl>
//$$
//$$ layout(location = 0) in vec3 Position;
//$$ layout(location = 1) in vec2 UV0;
//$$ layout(location = 2) in vec4 Color;
//$$
//$$ layout(location = 0) out vec2 texCoord0;
//$$ layout(location = 1) out vec4 vertexColor;
//#else
#version 150

//#if MC>=1.21.6
//$$ #moj_import <minecraft:dynamictransforms.glsl>
//$$ #moj_import <minecraft:projection.glsl>
//#else
uniform mat4 ModelViewMat;
uniform mat4 ProjMat;
//#endif

in vec3 Position;
in vec2 UV0;
in vec4 Color;

out vec2 texCoord0;
out vec4 vertexColor;
//#endif

void main() {
    gl_Position = ProjMat * ModelViewMat * vec4(Position, 1.0);

    texCoord0 = UV0;
    vertexColor = Color;
}
