#version 330 core

in vec2 frag_offset;
in vec4 node_color;

out vec4 frag_color;

void main() {
    float dist = length(frag_offset);
    if (dist <= 0.75) {
        frag_color = node_color;
    } else if (dist <= 1.0) {
        frag_color = vec4(0.0, 0.0, 0.0, 1.0);
    } else {
        frag_color = vec4(0.0, 0.0, 0.0, 0.0);
    }
}
