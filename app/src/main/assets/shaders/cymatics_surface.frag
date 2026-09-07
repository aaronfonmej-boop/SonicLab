#version 300 es
precision highp float;

in vec2 vUv;
in float vField;
in float vMask;
in float vDisplay;
in vec3 vWorldPosition;
in vec3 vNormal;

uniform int uStyle;
uniform int uPalette;
uniform int uRenderPass;
uniform int uShowNodes;
uniform int uShowAntinodes;
uniform int uShowPhaseLines;
uniform int uShowGrid;
uniform int uShowExciter;
uniform int uShowProbe;
uniform int uOrbitingLight;
uniform int uResonanceAura;
uniform int uEnergyPulses;
uniform vec2 uDrivePosition;
uniform vec2 uProbePosition;
uniform float uLightHue;
uniform float uContourDensity;
uniform float uAnimationTime;
uniform float uMotionIntensity;
uniform float uResonance;
uniform float uModeBlend;
uniform vec3 uCameraPosition;

out vec4 fragColor;

vec3 hsv2rgb(vec3 c) {
    vec3 p = abs(fract(c.xxx + vec3(0.0, 0.6666667, 0.3333333)) * 6.0 - 3.0);
    return c.z * mix(vec3(1.0), clamp(p - 1.0, 0.0, 1.0), c.y);
}

void paletteColors(out vec3 negativeColor, out vec3 positiveColor, out vec3 filamentColor) {
    if (uPalette == 1) {
        negativeColor = vec3(0.03, 0.42, 1.0);
        positiveColor = vec3(1.0, 0.34, 0.025);
        filamentColor = vec3(1.0, 0.88, 0.48);
    } else if (uPalette == 2) {
        negativeColor = vec3(0.10);
        positiveColor = vec3(0.92);
        filamentColor = vec3(1.0);
    } else if (uPalette == 3) {
        negativeColor = vec3(0.015, 0.72, 1.0);
        positiveColor = vec3(0.66, 0.10, 1.0);
        filamentColor = vec3(0.68, 0.96, 1.0);
    } else if (uPalette == 4) {
        negativeColor = vec3(0.46, 0.08, 0.015);
        positiveColor = vec3(1.0, 0.56, 0.025);
        filamentColor = vec3(1.0, 0.89, 0.42);
    } else {
        negativeColor = vec3(0.02, 0.70, 1.0);
        positiveColor = vec3(1.0, 0.08, 0.54);
        filamentColor = vec3(1.0, 0.86, 0.36);
    }
}

vec3 diverging(float value) {
    vec3 negativeColor;
    vec3 positiveColor;
    vec3 filamentColor;
    paletteColors(negativeColor, positiveColor, filamentColor);
    float magnitude = clamp(abs(value), 0.0, 1.0);
    vec3 side = value >= 0.0 ? positiveColor : negativeColor;
    vec3 neutral = uPalette == 4 ? vec3(0.055, 0.025, 0.018) : vec3(0.025, 0.045, 0.095);
    return mix(neutral, side, 0.16 + 0.84 * pow(magnitude, 0.72));
}

void main() {
    if (vMask < 0.035) discard;
    float plateAlpha = smoothstep(0.035, 0.72, vMask);
    float fieldAbs = abs(vField);
    float nodeCore = 1.0 - smoothstep(0.008, 0.026, fieldAbs);
    float nodeHalo = 1.0 - smoothstep(0.025, 0.145, fieldAbs);
    float antinode = smoothstep(0.60, 0.95, fieldAbs);
    vec3 negativeColor;
    vec3 positiveColor;
    vec3 filamentColor;
    paletteColors(negativeColor, positiveColor, filamentColor);

    if (uRenderPass == 0) {
        float edge = 1.0 - smoothstep(0.10, 0.42, vMask);
        vec3 underside = mix(vec3(0.004, 0.008, 0.018), vec3(0.018, 0.048, 0.085), edge);
        underside += filamentColor * edge * 0.075;
        fragColor = vec4(underside, plateAlpha * 0.98);
        return;
    }

    vec2 driveUv = uDrivePosition * 0.5 + 0.5;
    float distanceToDrive = distance(vUv, driveUv);
    float pulsePhase = distanceToDrive * 72.0 - uAnimationTime * (2.2 + 1.8 * uResonance);
    float pulseBand = pow(0.5 + 0.5 * cos(pulsePhase), 18.0);
    pulseBand *= 1.0 - smoothstep(0.08, 0.80, distanceToDrive);

    if (uRenderPass == 2) {
        float tracer = 0.40 + 0.60 * pow(
            0.5 + 0.5 * sin((vUv.x * 1.17 + vUv.y) * 48.0 - uAnimationTime * 1.65),
            4.0
        );
        float nodesEnabled = float(uShowNodes);
        float filament = nodesEnabled * (nodeCore * (1.15 + 0.85 * tracer) + nodeHalo * 0.20);
        float pulse = float(uEnergyPulses) * pulseBand * (0.28 + 0.72 * uMotionIntensity);
        float driveCore = 1.0 - smoothstep(0.0, 0.018, distanceToDrive);
        float emission = filament + pulse + driveCore * float(uShowExciter) * 1.8;
        if (emission < 0.018) discard;
        vec3 energyColor = mix(filamentColor, positiveColor, pulse * 0.32);
        vec3 emissionColor = energyColor * emission * (1.05 + 0.90 * uResonance);
        emissionColor += vec3(0.75, 0.96, 1.0) * nodeCore * tracer * nodesEnabled * 0.72;
        fragColor = vec4(emissionColor, clamp(emission * 0.62, 0.0, 1.0));
        return;
    }

    vec3 base = diverging(vDisplay);
    vec3 normal = normalize(vNormal);
    float orbitAngle = uAnimationTime * (0.15 + 0.16 * uMotionIntensity);
    vec3 staticLight = normalize(vec3(-0.38, 0.84, 0.36));
    vec3 orbitLight = normalize(vec3(cos(orbitAngle) * 0.62, 0.78, sin(orbitAngle) * 0.62));
    vec3 lightDirection = mix(staticLight, orbitLight, float(uOrbitingLight) * uMotionIntensity);
    vec3 secondaryDirection = normalize(vec3(-lightDirection.z, 0.45, lightDirection.x));
    vec3 viewDirection = normalize(uCameraPosition - vWorldPosition);
    float diffuse = max(dot(normal, lightDirection), 0.0);
    float secondaryDiffuse = max(dot(normal, secondaryDirection), 0.0);
    float specular = pow(max(dot(reflect(-lightDirection, normal), viewDirection), 0.0), 68.0);
    float broadSpecular = pow(max(dot(reflect(-secondaryDirection, normal), viewDirection), 0.0), 18.0);
    float fresnel = pow(1.0 - max(dot(normal, viewDirection), 0.0), 3.2);
    vec3 lightColor = hsv2rgb(vec3(fract(uLightHue / 360.0), 0.62, 1.0));
    vec3 secondaryColor = hsv2rgb(vec3(fract((uLightHue + 126.0) / 360.0), 0.70, 1.0));

    vec3 color;
    if (uStyle == 0) {
        color = base * (0.43 + 0.57 * diffuse) + vec3(0.24) * specular;
        if (uShowNodes == 1) color = mix(color, filamentColor, nodeCore * 0.72);
    } else {
        vec3 metalBase = uPalette == 4 ? vec3(0.045, 0.021, 0.016) : vec3(0.012, 0.030, 0.072);
        vec3 metal = mix(metalBase, base, 0.70);
        color = metal * (0.20 + 0.90 * diffuse + 0.20 * secondaryDiffuse);
        color += lightColor * (0.48 * specular + 0.34 * fresnel);
        color += secondaryColor * broadSpecular * 0.18 * uMotionIntensity;
        color += base * pow(abs(vDisplay), 1.35) * 0.44;

        float slowPulse = 0.5 + 0.5 * sin(uAnimationTime * 1.38);
        float modalRibbon = pow(
            0.5 + 0.5 * cos(fieldAbs * 34.0 - uAnimationTime * (0.72 + uResonance)),
            12.0
        );
        float auraEnabled = float(uResonanceAura);
        color += lightColor * nodeHalo * uResonance * auraEnabled *
            (0.055 + 0.085 * slowPulse) * uMotionIntensity;
        color += secondaryColor * modalRibbon * uResonance * auraEnabled * 0.075 * uMotionIntensity;
        color += positiveColor * pulseBand * float(uEnergyPulses) * 0.055 * uMotionIntensity;

        float transitionFlash = sin(3.14159265 * clamp(uModeBlend, 0.0, 1.0));
        color += mix(lightColor, secondaryColor, vUv.y) * transitionFlash * 0.10 * uMotionIntensity;
        float edgeGlow = 1.0 - smoothstep(0.025, 0.19, abs(vMask - 0.52));
        color += lightColor * edgeGlow * (0.11 + 0.14 * uResonance) * uMotionIntensity;
    }

    if (uShowAntinodes == 1) color += antinode * diverging(vField) * 0.22;
    if (uShowPhaseLines == 1) {
        float contourCoordinate = fieldAbs * uContourDensity;
        float contourWave = abs(sin(3.14159265 * contourCoordinate));
        float contour = 1.0 - smoothstep(0.025, 0.14, contourWave);
        color = mix(color, vec3(0.86, 0.95, 1.0), contour * 0.18);
    }
    if (uShowGrid == 1) {
        vec2 grid = abs(fract(vUv * 16.0 - 0.5) - 0.5) / fwidth(vUv * 16.0);
        float line = 1.0 - min(min(grid.x, grid.y), 1.0);
        color = mix(color, vec3(0.24, 0.68, 0.82), line * 0.16);
    }
    if (uShowExciter == 1) {
        float breathe = 1.0 + 0.12 * sin(uAnimationTime * 2.1) * uMotionIntensity;
        float ring = 1.0 - smoothstep(0.006, 0.017, abs(distanceToDrive - 0.030 * breathe));
        float core = 1.0 - smoothstep(0.0, 0.010, distanceToDrive);
        color = mix(color, vec3(0.94, 1.0, 1.0), max(ring, core) * 0.95);
    }
    if (uShowProbe == 1) {
        vec2 probeUv = uProbePosition * 0.5 + 0.5;
        float distanceToProbe = distance(vUv, probeUv);
        float crossX = 1.0 - smoothstep(0.002, 0.007, abs(vUv.x - probeUv.x));
        float crossY = 1.0 - smoothstep(0.002, 0.007, abs(vUv.y - probeUv.y));
        float extent = 1.0 - smoothstep(0.025, 0.040, distanceToProbe);
        color = mix(color, vec3(0.20, 1.0, 0.72), max(crossX, crossY) * extent * 0.92);
    }
    color = pow(max(color, vec3(0.0)), vec3(0.94));
    fragColor = vec4(color, plateAlpha);
}
