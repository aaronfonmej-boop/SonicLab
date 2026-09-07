#version 300 es
precision highp float;
precision highp int;

layout(location = 0) in vec3 aEquilibrium;

uniform mat4 uMvp;
uniform float uTime;
uniform float uSoundSpeed;
uniform float uAirDensity;
uniform float uSourceRadius;
uniform float uVisualScale;
uniform float uMaxVisualDisplacement;
uniform float uPointSize;
uniform float uArtisticHue;
uniform int uColorMode;
uniform int uVisualVariable;
uniform int uWaveform;
uniform int uScientificPalette;
uniform int uShowWavefronts;
uniform float uMaxDistance;
uniform int uSliceAxis;
uniform float uSliceOffset;
uniform int uSelectedIndex;
uniform int uMarkerMode;

uniform float uFrequencyA;
uniform float uPressurePeakA;
uniform float uPhaseA;
uniform float uAbsorptionA;
uniform int uHarmonicMode;
uniform float uHarmonics[16];

uniform int uSourceBEnabled;
uniform vec3 uSourceBPosition;
uniform float uFrequencyB;
uniform float uPressurePeakB;
uniform float uPhaseB;
uniform float uAbsorptionB;

uniform float uPressureColorScale;
uniform float uVelocityColorScale;
uniform float uDisplacementColorScale;

out vec3 vColor;
out float vIntensity;
out float vKind;

const float PI = 3.14159265359;

vec2 waveformComponents(float phase) {
    if (uWaveform == 0) return vec2(sin(phase), cos(phase));

    float pressure = 0.0;
    float displacement = 0.0;
    if (uWaveform == 1 || uWaveform == 2) {
        for (int i = 0; i < 4; ++i) {
            float n = float(2 * i + 1);
            float coefficient;
            if (uWaveform == 1) {
                float signValue = (i % 2 == 0) ? 1.0 : -1.0;
                coefficient = signValue * 8.0 / (PI * PI * n * n);
            } else {
                coefficient = 4.0 / (PI * n);
            }
            pressure += coefficient * sin(n * phase);
            displacement += coefficient / n * cos(n * phase);
        }
        return vec2(pressure, displacement);
    }

    for (int i = 1; i <= 8; ++i) {
        float n = float(i);
        pressure += 0.24 * sin(n * phase);
        displacement += 0.24 / n * cos(n * phase);
    }
    return vec2(pressure, displacement);
}

float envelopeAt(float radius, float absorptionNepers) {
    float regularizedRadius = sqrt(radius * radius + uSourceRadius * uSourceRadius);
    float referenceRadius = sqrt(1.0 + uSourceRadius * uSourceRadius);
    return referenceRadius / regularizedRadius *
        exp(-absorptionNepers * max(radius - 1.0, 0.0));
}

vec3 safeDirection(vec3 offset, float radius) {
    return radius > 0.00001 ? offset / radius : vec3(0.0);
}

vec3 hsv2rgb(vec3 c) {
    vec3 p = abs(fract(c.xxx + vec3(0.0, 0.6666667, 0.3333333)) * 6.0 - 3.0);
    return c.z * mix(vec3(1.0), clamp(p - 1.0, 0.0, 1.0), c.y);
}

vec3 slicePosition(vec2 normalizedPosition) {
    vec2 plane = normalizedPosition * uMaxDistance;
    if (uSliceAxis == 0) return vec3(plane.x, plane.y, uSliceOffset);
    if (uSliceAxis == 1) return vec3(plane.x, uSliceOffset, plane.y);
    return vec3(uSliceOffset, plane.x, plane.y);
}

void main() {
    bool isSlice = uMarkerMode == 2;
    if (uMarkerMode == 1) {
        gl_Position = uMvp * vec4(uSourceBPosition, 1.0);
        gl_PointSize = 15.0;
        vColor = vec3(0.18, 1.0, 0.58);
        vIntensity = 1.0;
        vKind = 2.0;
        return;
    }

    vec3 equilibrium = isSlice ? slicePosition(aEquilibrium.xy) : aEquilibrium;
    if (!isSlice && gl_VertexID == 0) {
        gl_Position = uMvp * vec4(0.0, 0.0, 0.0, 1.0);
        gl_PointSize = 15.0;
        vColor = vec3(1.0, 0.78, 0.12);
        vIntensity = 1.0;
        vKind = 2.0;
        return;
    }
    if (isSlice && length(equilibrium) > uMaxDistance) {
        gl_Position = vec4(2.0, 2.0, 2.0, 1.0);
        gl_PointSize = 1.0;
        vColor = vec3(0.0);
        vIntensity = 0.0;
        vKind = 3.0;
        return;
    }

    float omegaA = 2.0 * PI * uFrequencyA;
    vec3 offsetA = equilibrium;
    float radiusA = length(offsetA);
    vec3 directionA = safeDirection(offsetA, radiusA);
    float envelopeA = envelopeAt(radiusA, uAbsorptionA);
    float basePhaseA = omegaA / max(uSoundSpeed, 1.0) * radiusA - omegaA * uTime + uPhaseA;

    float pressureNormA = 0.0;
    float displacementNormA = 0.0;
    if (uHarmonicMode == 1) {
        for (int i = 0; i < 16; ++i) {
            float order = float(i + 1);
            float harmonicPhase = order * (basePhaseA - uPhaseA) + uPhaseA;
            pressureNormA += uHarmonics[i] * sin(harmonicPhase);
            displacementNormA += uHarmonics[i] / order * cos(harmonicPhase);
        }
    } else {
        vec2 components = waveformComponents(basePhaseA);
        pressureNormA = components.x;
        displacementNormA = components.y;
    }

    float pressure = uPressurePeakA * envelopeA * pressureNormA;
    float velocitySigned = uPressurePeakA /
        max(uAirDensity * uSoundSpeed, 0.0001) * envelopeA * pressureNormA;
    float displacementA = uPressurePeakA /
        max(uAirDensity * uSoundSpeed * omegaA, 0.0001) * envelopeA * displacementNormA;
    vec3 displacementVector = directionA * displacementA;
    vec3 velocityVector = directionA * velocitySigned;

    if (uSourceBEnabled == 1) {
        vec3 offsetB = equilibrium - uSourceBPosition;
        float radiusB = length(offsetB);
        vec3 directionB = safeDirection(offsetB, radiusB);
        float envelopeB = envelopeAt(radiusB, uAbsorptionB);
        float omegaB = 2.0 * PI * uFrequencyB;
        float phaseB = omegaB / max(uSoundSpeed, 1.0) * radiusB - omegaB * uTime + uPhaseB;
        float pressureNormB = sin(phaseB);
        pressure += uPressurePeakB * envelopeB * pressureNormB;
        velocityVector += directionB * uPressurePeakB /
            max(uAirDensity * uSoundSpeed, 0.0001) * envelopeB * pressureNormB;
        displacementVector += directionB * uPressurePeakB /
            max(uAirDensity * uSoundSpeed * omegaB, 0.0001) * envelopeB * cos(phaseB);
    }

    vec3 visualDisplacement = displacementVector * uVisualScale;
    float visualLength = length(visualDisplacement);
    if (visualLength > uMaxVisualDisplacement) {
        visualDisplacement *= uMaxVisualDisplacement / visualLength;
    }
    vec3 currentPosition = isSlice ? equilibrium : equilibrium + visualDisplacement;

    float signedValue;
    if (uVisualVariable == 1) {
        signedValue = dot(displacementVector, directionA) / max(uDisplacementColorScale, 1e-12);
    } else if (uVisualVariable == 2) {
        signedValue = dot(velocityVector, directionA) / max(uVelocityColorScale, 1e-9);
    } else if (uVisualVariable == 3) {
        signedValue = sin(basePhaseA);
    } else {
        signedValue = pressure / max(uPressureColorScale, 1e-6);
    }
    signedValue = clamp(signedValue, -1.0, 1.0);

    if (uColorMode == 0) {
        vec3 neutralColor = vec3(0.72, 0.86, 0.92);
        vec3 positiveColor = vec3(1.0, 0.035, 0.12);
        vec3 negativeColor = vec3(0.02, 0.25, 1.0);
        if (uScientificPalette == 1) {
            neutralColor = vec3(0.82, 0.86, 0.82);
            positiveColor = vec3(1.0, 0.52, 0.0);
            negativeColor = vec3(0.0, 0.72, 0.92);
        } else if (uScientificPalette == 2) {
            neutralColor = vec3(0.50);
            positiveColor = vec3(1.0);
            negativeColor = vec3(0.08);
        }
        vColor = signedValue >= 0.0
            ? mix(neutralColor, positiveColor, signedValue)
            : mix(neutralColor, negativeColor, -signedValue);
    } else {
        float hue = fract(uArtisticHue / 360.0 + 0.20 * signedValue);
        vColor = hsv2rgb(vec3(hue, 0.82, 1.0));
    }

    vIntensity = 0.28 + 0.72 * abs(signedValue);
    vKind = isSlice ? 3.0 : (gl_VertexID == uSelectedIndex ? 1.0 : 0.0);
    float frontStrength = pow(abs(signedValue), 6.0);
    if (!isSlice && uShowWavefronts == 1 && vKind < 0.5) {
        vIntensity = 0.10 + 0.90 * frontStrength;
    }
    if (vKind > 0.5 && vKind < 1.5) {
        vColor = vec3(1.0, 1.0, 0.22);
        vIntensity = 1.0;
    }

    gl_Position = uMvp * vec4(currentPosition, 1.0);
    if (isSlice) {
        gl_PointSize = 4.0;
    } else if (vKind > 0.5) {
        gl_PointSize = 14.0;
    } else {
        float wavefrontSize = uShowWavefronts == 1 ? 1.0 + 1.5 * frontStrength : 1.0;
        gl_PointSize = clamp(uPointSize * (1.0 + 0.8 * abs(signedValue)) * wavefrontSize, 1.0, 18.0);
    }
}
