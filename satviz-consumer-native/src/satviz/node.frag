#version 330 core

in float frag_heat;
in vec2  frag_offset;

out vec4 frag_color;

void main()
{
    float dist = length(frag_offset);
    vec3 cold_color = vec3(0.0, 0.5, 0.55);
    vec3 hot_color  = vec3(0.95, 0.7, 0.3);
    if (dist <= 0.75) {
        frag_color = vec4(mix(cold_color, hot_color, frag_heat), 1.0);
    } else if (dist <= 1.0) {
        frag_color = vec4(0.0, 0.0, 0.0, 1.0);
    } else {
        frag_color = vec4(0.0, 0.0, 0.0, 0.0);
    }
}

