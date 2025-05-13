#version 110

varying vec2 texCoord0;
varying vec4 vertexColor;

void main() {
    gl_Position = gl_ProjectionMatrix * gl_ModelViewMatrix * gl_Vertex;

    texCoord0 = gl_MultiTexCoord0.xy;
    vertexColor = gl_Color;
}
