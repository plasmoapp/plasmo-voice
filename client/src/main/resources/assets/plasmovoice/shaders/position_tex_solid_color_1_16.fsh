#version 110

uniform sampler2D Sampler0;

varying vec2 texCoord0;
varying vec4 vertexColor;

void main() {
    vec4 color = texture2D(Sampler0, texCoord0.st);
    if (color.a < 0.1) {
        discard;
    }

    gl_FragColor = vertexColor;
}
