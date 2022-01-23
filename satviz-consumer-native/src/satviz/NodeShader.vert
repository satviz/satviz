#version 330 core
#extension GL_ARB_explicit_uniform_location : enable

layout(location = 0) uniform mat4 world_to_view;
uniform sampler1D heat_palette;

layout(location = 0) in vec2  template_position;
layout(location = 1) in vec2  node_position;
layout(location = 2) in float node_heat;

out vec2 frag_offset;
out vec4 node_color;

void main() {
  frag_offset = template_position;
  gl_Position = world_to_view * vec4(template_position + node_position, 0.0, 1.0);
  node_color  = texture(heat_palette, node_heat);
}
