#version 300 es
precision highp float;

layout(location = 0) in vec4 aStartTarget;
layout(location = 1) in vec2 aNoiseDelay;

uniform mat4 uMvp;
uniform sampler2D uModeTexture;
uniform sampler2D uPreviousModeTexture;
uniform float uModeBlend;
uniform float uProgress;
uniform float uPhase;
uniform float uAmplitude;
uniform float uVisualScale;
uniform float uPointSize;
uniform float uAnimationTime;
uniform float uMotionIntensity;
uniform float uResonance;
uniform float uFlowIntensity;
uniform float uLayerAlpha;
uniform int uLayer;

out float vGlow;
out float vAlpha;
out float vLayer;
out float vTwinkle;

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
    if (uLayer == 1) {
        float life = fract(aNoiseDelay.y + uAnimationTime * (0.024 + 0.032 * aNoiseDelay.x));
        float lifeFade = smoothstep(0.0, 0.12, life) * (1.0 - smoothstep(0.75, 1.0, life));
        float angle = aNoiseDelay.x * 6.2831853 + uAnimationTime * (0.11 + 0.09 * aNoiseDelay.y);
        float drift = uMotionIntensity;
        vec3 world = vec3(
            (aStartTarget.x - 0.5) * 10.4 + sin(angle) * (0.22 + aNoiseDelay.y * 0.40) * drift,
            0.10 + life * 3.95 + sin(angle * 1.7) * 0.15 * drift,
            (aStartTarget.y - 0.5) * 9.2 + cos(angle) * (0.22 + aNoiseDelay.x * 0.40) * drift
        );
        gl_Position = uMvp * vec4(world, 1.0);
        gl_PointSize = uPointSize * (0.52 + 0.74 * aNoiseDelay.x);
        vGlow = 0.32 + 0.68 * aNoiseDelay.x;
        vAlpha = lifeFade * uLayerAlpha * (0.14 + 0.22 * uMotionIntensity);
        vLayer = 1.0;
        vTwinkle = 0.5 + 0.5 * sin(uAnimationTime * (1.6 + aNoiseDelay.y * 2.2) + angle * 3.0);
        return;
    }

    if (uLayer == 2) {
        vec2 anchor = aStartTarget.zw;
        vec2 texel = 1.0 / vec2(textureSize(uModeTexture, 0));
        float left = modal(anchor - vec2(texel.x, 0.0)).r;
        float right = modal(anchor + vec2(texel.x, 0.0)).r;
        float down = modal(anchor - vec2(0.0, texel.y)).r;
        float up = modal(anchor + vec2(0.0, texel.y)).r;
        vec2 gradient = vec2(right - left, up - down);
        vec2 tangent = length(gradient) > 0.0001 ? normalize(vec2(-gradient.y, gradient.x)) : vec2(1.0, 0.0);
        vec2 normalDirection = vec2(-tangent.y, tangent.x);
        float streamTime = uAnimationTime * (0.34 + 0.25 * aNoiseDelay.x) + aNoiseDelay.y * 6.2831853;
        float lane = (aNoiseDelay.x - 0.5) * 0.046;
        float travel = sin(streamTime) * (0.026 + 0.055 * aNoiseDelay.y);
        vec2 candidate = clamp(anchor + tangent * travel + normalDirection * lane, 0.0, 1.0);
        vec2 data = modal(candidate);
        vec2 uv = data.g > 0.42 ? candidate : anchor;
        data = modal(uv);
        float height = data.r * sin(uPhase) * uAmplitude * uVisualScale;
        float lift = 0.052 + abs(sin(streamTime * 1.9)) * 0.035 * uMotionIntensity;
        vec3 world = vec3((uv.x - 0.5) * 8.0, height + lift, (uv.y - 0.5) * 8.0);
        gl_Position = uMvp * vec4(world, 1.0);
        float twinkle = 0.5 + 0.5 * sin(streamTime * 2.4 + aNoiseDelay.x * 19.0);
        gl_PointSize = uPointSize * (0.72 + 0.62 * aNoiseDelay.x) * (1.0 + 0.25 * twinkle);
        vGlow = 0.70 + 0.30 * twinkle;
        vAlpha = data.g * uLayerAlpha * (0.30 + 0.45 * uMotionIntensity);
        vLayer = 2.0;
        vTwinkle = twinkle;
        return;
    }

    float delay = aNoiseDelay.y * 0.24;
    float localProgress = smoothstep(delay, 1.0, uProgress);
    float easedProgress = localProgress * localProgress * (3.0 - 2.0 * localProgress);
    vec2 start = aStartTarget.xy;
    vec2 target = aStartTarget.zw;
    vec2 direct = mix(start, target, easedProgress);
    vec2 direction = target - start;
    float directionLength = max(length(direction), 0.0001);
    vec2 perpendicular = vec2(-direction.y, direction.x) / directionLength;
    float travelEnvelope = sin(3.14159265 * easedProgress);
    float flow = uFlowIntensity * uMotionIntensity;
    float arc = (aNoiseDelay.x - 0.5) * travelEnvelope * (0.14 + 0.12 * aNoiseDelay.y) * flow;
    float eddy = sin(easedProgress * 18.0 + aNoiseDelay.x * 23.0 + uAnimationTime * 0.65) *
        travelEnvelope * 0.025 * flow;
    float settled = smoothstep(0.82, 1.0, localProgress);
    float current = sin(uAnimationTime * (0.42 + 0.18 * aNoiseDelay.y) + aNoiseDelay.x * 21.0) *
        settled * (0.006 + 0.013 * aNoiseDelay.y) * flow;
    vec2 candidate = clamp(direct + perpendicular * (arc + eddy + current), 0.0, 1.0);
    vec2 candidateData = modal(candidate);
    bool candidateInside = candidateData.g >= 0.48;
    vec2 uv = candidateInside ? candidate : direct;
    vec2 data = candidateInside ? candidateData : modal(direct);
    float field = data.r;
    float plateHeight = field * sin(uPhase) * uAmplitude * uVisualScale;
    float granularHop = travelEnvelope * (0.035 + 0.22 * abs(field)) *
        (0.35 + 0.65 * aNoiseDelay.x) * flow;
    float vibrationHop = (1.0 - localProgress) * abs(field) *
        abs(sin(localProgress * 38.0 + aNoiseDelay.x * 31.4159 + uPhase)) * 0.20;
    float livingHop = settled * abs(sin(uAnimationTime * (1.3 + aNoiseDelay.x) + aNoiseDelay.y * 17.0)) *
        (0.005 + 0.014 * abs(field)) * flow;
    vec3 world = vec3(
        (uv.x - 0.5) * 8.0,
        plateHeight + 0.045 + granularHop + vibrationHop + livingHop,
        (uv.y - 0.5) * 8.0
    );
    gl_Position = uMvp * vec4(world, 1.0);
    float pulse = 0.5 + 0.5 * sin(uAnimationTime * 1.85 + aNoiseDelay.x * 9.0);
    gl_PointSize = uPointSize * (0.74 + 0.48 * aNoiseDelay.x) *
        (1.0 + 0.14 * uResonance * pulse * uMotionIntensity);
    vGlow = 0.38 + 0.42 * localProgress + 0.20 * uResonance * pulse;
    vAlpha = data.g * uLayerAlpha;
    vLayer = 0.0;
    vTwinkle = pulse;
}
