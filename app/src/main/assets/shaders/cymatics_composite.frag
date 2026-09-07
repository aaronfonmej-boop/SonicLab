#version 300 es
precision highp float;

in vec2 vUv;
uniform sampler2D uScene;
uniform sampler2D uBloom;
uniform float uBloomIntensity;
uniform float uMotionIntensity;
uniform float uResonance;
uniform float uAnimationTime;
uniform vec2 uResolution;
out vec4 fragColor;

float hash(vec2 value) {
    return fract(sin(dot(value, vec2(12.9898, 78.233))) * 43758.5453);
}

void main() {
    vec2 centered = vUv - 0.5;
    float radial = dot(centered, centered);
    vec2 aberration = centered * (0.0012 + 0.0010 * uResonance) * uMotionIntensity;
    vec3 scene;
    scene.r = texture(uScene, vUv + aberration).r;
    scene.g = texture(uScene, vUv).g;
    scene.b = texture(uScene, vUv - aberration).b;
    vec3 bloom = texture(uBloom, vUv).rgb;
    vec3 color = scene + bloom * uBloomIntensity * (0.72 + 0.48 * uResonance);
    color = vec3(1.0) - exp(-color * 1.22);
    float vignette = 1.0 - smoothstep(0.18, 0.72, radial);
    color *= 0.78 + 0.22 * vignette;
    float grain = hash(gl_FragCoord.xy + fract(uAnimationTime) * uResolution) - 0.5;
    color += grain * (0.008 + 0.004 * uMotionIntensity);
    color = pow(max(color, vec3(0.0)), vec3(0.94));
    fragColor = vec4(color, 1.0);
}
