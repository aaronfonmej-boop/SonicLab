#version 300 es
precision highp float;

uniform mat4 uMvp;
out vec2 vUv;
out vec3 vWorldPosition;

void main() {
    const vec2 positions[6] = vec2[6](
        vec2(-1.0, -1.0), vec2(1.0, -1.0), vec2(-1.0, 1.0),
        vec2(-1.0, 1.0), vec2(1.0, -1.0), vec2(1.0, 1.0)
    );
    vec2 position = positions[gl_VertexID];
    vUv = position * 0.5 + 0.5;
    vWorldPosition = vec3(position.x * 10.5, -0.62, position.y * 10.5);
    gl_Position = uMvp * vec4(vWorldPosition, 1.0);
}
