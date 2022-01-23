#version 330 core
#extension GL_ARB_explicit_uniform_location : enable

layout(location = 0) uniform mat4 world_to_view;

layout(location = 0) in vec2 node_position;

void main() {
    gl_Position = world_to_view * vec4(node_position, 0.0, 1.0);
}
