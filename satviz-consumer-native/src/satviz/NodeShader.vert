#version 330 core
#extension GL_ARB_explicit_uniform_location : enable

layout(location = 0) uniform mat4 world_to_view;
layout(location = 1) uniform vec2 node_size;
uniform sampler1D heat_palette;

layout(location = 0) in vec2  template_position;
layout(location = 1) in float node_heat;
layout(location = 2) in vec2  node_position;

out vec2 frag_offset;
out vec3 node_color;

void main() {
  frag_offset = template_position;
  gl_Position = vec4(node_size * template_position, 0.0, 0.0) + world_to_view * vec4(node_position, 0.0, 1.0);
  node_color  = texture(heat_palette, node_heat).rgb;
}
