#version 330 core
#extension GL_ARB_explicit_uniform_location : enable

layout(location = 0) uniform mat4 world_to_view;
uniform sampler1D offset_texview;

layout(location = 0) in ivec2 edge_indices;
layout(location = 1) in float edge_weight;

out float weight;

void main() {
    int index = gl_VertexID == 0 ? edge_indices.x : edge_indices.y;
    vec2 offset = texelFetch(offset_texview, index, 0).rg;
    gl_Position = world_to_view * vec4(offset, 0.0, 1.0);
    weight = edge_weight;
}
