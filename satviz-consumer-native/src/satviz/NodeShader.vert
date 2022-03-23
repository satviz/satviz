#version 330 core
#extension GL_ARB_explicit_uniform_location : enable
#extension GL_ARB_gpu_shader_fp64 : enable

#ifdef GL_ARB_gpu_shader_fp64
layout(location = 0) uniform dmat4 world_to_view;
#else
layout(location = 0) uniform mat4 world_to_view;
#endif

layout(location = 1) uniform vec2 node_size;
uniform sampler1D heat_palette;

layout(location = 0) in vec2  template_position;
layout(location = 1) in float node_heat;
layout(location = 2) in vec2  node_position;

out vec2 frag_offset;
out vec3 node_color;

void main() {
  frag_offset = template_position;
#ifdef GL_ARB_gpu_shader_fp64
  gl_Position = vec4(world_to_view * dvec4(node_position, 0.0, 1.0));
#else
  gl_Position = vec4(world_to_view * vec4(node_position, 0.0, 1.0));
#endif
  gl_Position += vec4(node_size * template_position, 0.0, 0.0);
  node_color  = texture(heat_palette, node_heat).rgb;
}
