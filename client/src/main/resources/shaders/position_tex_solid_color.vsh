#version 110

in vec3 Position;
in vec2 UV0;
in vec4 Color;

//uniform mat4 ModelViewMat;
//uniform mat4 ProjMat;

out vec2 texCoord0;
out vec4 vertexColor;

void main() {
    gl_Position = gl_ProjectionMatrix * gl_ModelViewMatrix * vec4(Position, 1.0);

    texCoord0 = UV0;
    vertexColor = Color;
}
