#version 300 es
precision highp float;

in vec2 vUv;
uniform sampler2D uScene;
out vec4 fragColor;

void main() {
    vec3 color = texture(uScene, vUv).rgb;
    float brightness = max(max(color.r, color.g), color.b);
    float contribution = smoothstep(0.48, 1.18, brightness);
    fragColor = vec4(color * contribution, 1.0);
}
