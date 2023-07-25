#version 110

uniform sampler2D TextureSampler;

in vec2 texCoord0;
in vec4 vertexColor;

out vec4 fragColor;

void main() {
    vec4 color = texture(TextureSampler, texCoord0);
    if (color.a < 0.1) {
        discard;
    }
    fragColor = vertexColor;
}
