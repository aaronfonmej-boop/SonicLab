#version 300 es
precision highp float;

layout(location = 0) in vec2 aUv;

uniform mat4 uMvp;
uniform sampler2D uModeTexture;
uniform sampler2D uPreviousModeTexture;
uniform float uModeBlend;
uniform float uPhase;
uniform float uAmplitude;
uniform float uVisualScale;
uniform float uGridSize;
uniform float uVerticalOffset;
uniform int uVariable;

out vec2 vUv;
out float vField;
out float vMask;
out float vDisplay;
out vec3 vWorldPosition;
out vec3 vNormal;

vec2 modalCurrent(vec2 uv) {
    ivec2 size = textureSize(uModeTexture, 0);
    vec2 position = clamp(uv, 0.0, 1.0) * vec2(size - 1);
    ivec2 low = ivec2(floor(position));
    ivec2 high = min(low + ivec2(1), size - ivec2(1));
    vec2 blendValue = fract(position);
    vec2 a = mix(texelFetch(uModeTexture, ivec2(low.x, low.y), 0).rg,
                 texelFetch(uModeTexture, ivec2(high.x, low.y), 0).rg, blendValue.x);
    vec2 b = mix(texelFetch(uModeTexture, ivec2(low.x, high.y), 0).rg,
                 texelFetch(uModeTexture, ivec2(high.x, high.y), 0).rg, blendValue.x);
    return mix(a, b, blendValue.y);
}

vec2 modalPrevious(vec2 uv) {
    ivec2 size = textureSize(uPreviousModeTexture, 0);
    vec2 position = clamp(uv, 0.0, 1.0) * vec2(size - 1);
    ivec2 low = ivec2(floor(position));
    ivec2 high = min(low + ivec2(1), size - ivec2(1));
    vec2 blendValue = fract(position);
    vec2 a = mix(texelFetch(uPreviousModeTexture, ivec2(low.x, low.y), 0).rg,
                 texelFetch(uPreviousModeTexture, ivec2(high.x, low.y), 0).rg, blendValue.x);
    vec2 b = mix(texelFetch(uPreviousModeTexture, ivec2(low.x, high.y), 0).rg,
                 texelFetch(uPreviousModeTexture, ivec2(high.x, high.y), 0).rg, blendValue.x);
    return mix(a, b, blendValue.y);
}

vec2 modal(vec2 uv) {
    if (uModeBlend >= 0.9995) return modalCurrent(uv);
    if (uModeBlend <= 0.0005) return modalPrevious(uv);
    return mix(modalPrevious(uv), modalCurrent(uv), uModeBlend);
}

void main() {
    vec2 data = modal(aUv);
    float field = data.r;
    float phaseWave = sin(uPhase);
    float displacement = field * phaseWave * uAmplitude * uVisualScale;
    float texel = 1.0 / max(uGridSize, 2.0);
    float left = modal(aUv - vec2(texel, 0.0)).r;
    float right = modal(aUv + vec2(texel, 0.0)).r;
    float down = modal(aUv - vec2(0.0, texel)).r;
    float up = modal(aUv + vec2(0.0, texel)).r;
    float heightScale = phaseWave * uAmplitude * uVisualScale;
    vec3 normal = normalize(vec3(
        -(right - left) * heightScale,
        2.0 * texel * 8.0,
        -(up - down) * heightScale
    ));
    vec3 world = vec3(
        (aUv.x - 0.5) * 8.0,
        displacement + uVerticalOffset,
        (aUv.y - 0.5) * 8.0
    );
    float displayValue = displacement;
    if (uVariable == 1) {
        displayValue = field * cos(uPhase) * uAmplitude;
    } else if (uVariable == 2) {
        displayValue = sin(uPhase + (field < 0.0 ? 3.14159265 : 0.0)) * abs(field);
    } else if (uVariable == 3) {
        displayValue = field * field * uAmplitude;
    }
    vUv = aUv;
    vField = field;
    vMask = data.g;
    vDisplay = displayValue;
    vWorldPosition = world;
    vNormal = normal;
    gl_Position = uMvp * vec4(world, 1.0);
}
