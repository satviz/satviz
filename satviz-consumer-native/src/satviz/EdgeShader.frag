#version 330 core
#extension GL_ARB_explicit_uniform_location : enable

layout(location = 1) uniform vec3 edge_color;

in float weight;

out vec4 frag_color;

void main() {
    frag_color = vec4(edge_color, weight);
}
