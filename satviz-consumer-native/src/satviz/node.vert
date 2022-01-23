#version 330 core

layout(location = 0) uniform mat4 world_to_view;

layout(location = 0) in vec2  template_position;
layout(location = 1) in vec2  node_position;
layout(location = 2) in float node_heat;

out float frag_heat;
out vec2  frag_offset;

void main()
{
    frag_heat = node_heat;
    frag_offset = template_position;
    gl_Position = world_to_view * vec4(template_position + node_position, 0.0, 1.0);
}

