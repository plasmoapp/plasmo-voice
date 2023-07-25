#version 110

uniform sampler2D TextureSampler;

varying vec2 texCoord0;
varying vec4 vertexColor;

void main() {
    vec4 color = texture2D(TextureSampler, texCoord0.st);
    if (color.a < 0.1) {
        discard;
    }

    gl_FragColor = vertexColor;
}
