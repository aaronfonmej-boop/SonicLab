#version 300 es
precision highp float;

in float vGlow;
in float vAlpha;
in float vLayer;
in float vTwinkle;
uniform int uStyle;
uniform int uPalette;
out vec4 fragColor;

void paletteColors(out vec3 cool, out vec3 warm) {
    if (uPalette == 1) {
        cool = vec3(0.04, 0.48, 1.0);
        warm = vec3(1.0, 0.42, 0.04);
    } else if (uPalette == 2) {
        cool = vec3(0.58);
        warm = vec3(1.0);
    } else if (uPalette == 3) {
        cool = vec3(0.08, 0.78, 1.0);
        warm = vec3(0.76, 0.20, 1.0);
    } else if (uPalette == 4) {
        cool = vec3(0.88, 0.23, 0.035);
        warm = vec3(1.0, 0.86, 0.25);
    } else {
        cool = vec3(0.18, 0.72, 1.0);
        warm = vec3(1.0, 0.18, 0.64);
    }
}

void main() {
    vec2 point = gl_PointCoord * 2.0 - 1.0;
    float radius = dot(point, point);
    if (radius > 1.0) discard;
    float softDisc = 1.0 - smoothstep(0.30, 1.0, radius);
    float core = 1.0 - smoothstep(0.0, 0.24, radius);
    vec3 cool;
    vec3 warm;
    paletteColors(cool, warm);

    if (vLayer > 1.5) {
        vec3 streamColor = mix(cool, vec3(0.92, 1.0, 1.0), 0.48 + 0.35 * vTwinkle);
        streamColor += warm * core * 0.36;
        fragColor = vec4(streamColor * (0.82 + 0.82 * vTwinkle), softDisc * vAlpha);
    } else if (vLayer > 0.5) {
        vec3 dustColor = mix(cool, warm, vTwinkle * 0.38);
        fragColor = vec4(dustColor * (0.62 + 0.82 * vTwinkle), softDisc * vAlpha);
    } else {
        if (vAlpha < 0.035) discard;
        vec3 scientific = vec3(0.96, 0.84, 0.48);
        vec3 spectacular = mix(cool, warm, clamp(vGlow, 0.0, 1.0));
        vec3 color = uStyle == 0 ? scientific : spectacular;
        color += vec3(1.0, 0.97, 0.84) * core * vTwinkle * (uStyle == 0 ? 0.10 : 0.38);
        fragColor = vec4(color * (0.70 + 0.50 * softDisc), softDisc * vAlpha * 0.96);
    }
}
