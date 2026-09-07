#version 300 es
precision highp float;

in vec2 vUv;
in vec3 vWorldPosition;
uniform vec2 uDrivePosition;
uniform float uAnimationTime;
uniform float uMotionIntensity;
uniform float uResonance;
uniform int uEnergyPulses;
uniform int uPalette;
out vec4 fragColor;

void main() {
    vec2 centered = vWorldPosition.xz / 8.0;
    float radius = length(centered);
    float stageFade = 1.0 - smoothstep(0.48, 1.28, radius);
    if (stageFade < 0.002) discard;
    vec2 gridUv = abs(fract(vWorldPosition.xz * 0.25 - 0.5) - 0.5) / fwidth(vWorldPosition.xz * 0.25);
    float grid = 1.0 - min(min(gridUv.x, gridUv.y), 1.0);
    float ringCoordinate = length(vWorldPosition.xz) * 0.72;
    float rings = 1.0 - smoothstep(0.025, 0.14, abs(sin(ringCoordinate * 3.14159265)));
    vec2 driveWorld = uDrivePosition * 4.0;
    float driveDistance = distance(vWorldPosition.xz, driveWorld);
    float wave = pow(0.5 + 0.5 * cos(driveDistance * 3.1 - uAnimationTime * 2.35), 22.0);
    wave *= 1.0 - smoothstep(0.4, 8.2, driveDistance);
    float halo = exp(-0.13 * dot(vWorldPosition.xz, vWorldPosition.xz));

    vec3 cool = uPalette == 4 ? vec3(1.0, 0.28, 0.025) : vec3(0.02, 0.58, 1.0);
    vec3 warm = uPalette == 3 ? vec3(0.70, 0.12, 1.0) : vec3(1.0, 0.18, 0.54);
    if (uPalette == 1) warm = vec3(1.0, 0.44, 0.025);
    if (uPalette == 4) warm = vec3(1.0, 0.82, 0.18);
    if (uPalette == 2) { cool = vec3(0.45); warm = vec3(0.95); }

    vec3 base = vec3(0.0025, 0.006, 0.017);
    vec3 color = base + cool * halo * (0.025 + 0.055 * uResonance);
    color += mix(cool, warm, clamp(radius, 0.0, 1.0)) * grid * stageFade * 0.028;
    color += cool * rings * stageFade * 0.022;
    color += mix(cool, warm, 0.35) * wave * float(uEnergyPulses) *
        (0.11 + 0.22 * uResonance) * uMotionIntensity;
    color *= stageFade;
    fragColor = vec4(color, stageFade);
}
