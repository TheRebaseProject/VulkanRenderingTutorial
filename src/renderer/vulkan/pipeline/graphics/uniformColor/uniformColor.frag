#version 450

/*
 * uniforms
 */
layout(set = 0, binding = 0) uniform UniformBufferObject {
    vec4 color;
} ubo;

/*
 * outputs
 */
layout(location = 0) out vec4 outputFragColor;

/*
 * functions
 */
void main() {
    outputFragColor = ubo.color;
}