#version 330 core

uniform mat4 world_to_view;

layout(location = 0) in vec2 node_position;

void main()
{
    gl_Position = world_to_view * vec4(node_position, 0.0, 1.0);
}

