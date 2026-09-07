#version 300 es
precision mediump float;

in vec3 vColor;
in float vIntensity;
in float vKind;
out vec4 fragColor;

void main() {
    vec2 centered = gl_PointCoord * 2.0 - 1.0;
    float radius = length(centered);
    if (radius > 1.0) discard;
    float core = 1.0 - smoothstep(0.10, 0.92, radius);
    float halo = 1.0 - smoothstep(0.0, 1.0, radius);
    float alpha = (0.20 * halo + 0.80 * core) * vIntensity;

    if (vKind > 0.5 && vKind < 2.5) {
        alpha = max(alpha, 1.0 - smoothstep(0.55, 1.0, radius));
    } else if (vKind > 2.5) {
        alpha = (0.18 + 0.48 * core) * vIntensity;
    }
    fragColor = vec4(vColor, alpha);
}
