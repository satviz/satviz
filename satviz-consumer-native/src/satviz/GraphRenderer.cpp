#include <satviz/GraphRenderer.hpp>
#include <satviz/GlUtils.hpp>

#define NODE_ATTR_POSITION 0
#define NODE_ATTR_HEAT     1
#define NODE_ATTR_OFFSET   2

#define EDGE_ATTR_POSITION 0
#define EDGE_ATTR_WEIGHT   1

namespace satviz {
namespace video {

static const float template_coordinates[] = {
     1.0f, -1.0f,
     1.0f,  1.0f,
    -1.0f, -1.0f,
    -1.0f,  1.0f,
};

static void GraphRenderer::initializeResources() {
  // Load shaders
  GLuint node_vert    = compileGlShader(node_vert_shader_source, GL_VERTEX_SHADER);
  GLuint node_frag    = compileGlShader(node_frag_shader_source, GL_FRAGMENT_SHADER);
  GLuint node_vert    = compileGlShader(edge_vert_shader_source, GL_VERTEX_SHADER);
  GLuint edge_frag    = compileGlShader(edge_frag_shader_source, GL_FRAGMENT_SHADER);
  resources.node_prog = linkGlProgram(node_vert, node_frag);
  resources.edge_prog = linkGlProgram(edge_vert, edge_frag);
  glDeleteShader(node_vert);
  glDeleteShader(node_frag);
  glDeleteShader(edge_vert);
  glDeleteShader(edge_frag);
  // Upload template geometry to VRAM
  glGenBuffers(1, &resources.template_vbo);
  glBindBuffer(GL_ARRAY_BUFFER, resources.template_vbo);
  glBufferData(GL_ARRAY_BUFFER, sizeof template_coordinates, template_coordinates, GL_STATIC_DRAW);
}

static void GraphRenderer::terminateResources() {
  glDeleteBuffers(1, &resources.template_vbo);
  glDeleteProgram(resources.node_prog);
  glDeleteProgram(resources.edge_prog);
}

GraphRenderer::GraphRenderer(graph::Graph *gr)
  : GraphObserver(gr) {
  this->node_count = 0;
  glGenBuffers(1, &this->node_vbo);
  glBindBuffer(GL_ARRAY_BUFFER, this->node_vbo);
  glBufferData(GL_ARRAY_BUFFER, (8 + 1) * NODES_PER_VBO, NULL, GL_DYNAMIC_DRAW);

  this->edge_count = 0;
  glGenBuffers(1, &this->edge_ibo);
  glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, this->edge_ibo);
  glBufferData(GL_ELEMENT_ARRAY_BUFFER, 8 * EDGES_PER_IBO, NULL, GL_DYNAMIC_DRAW);

  glGenVertexArrays(1, &resources.node_vao);
  glBindVertexArray(resources.node_vao);

  glEnableVertexAttribArray(NODE_ATTR_POSITION);
  glVertexAttribFormat(NODE_ATTR_POSITION, 2, GL_FLOAT, GL_FALSE, 0);
  glVertexAttribBinding(NODE_ATTR_POSITION, NODE_ATTR_POSITION);
  glBindVertexBuffer(NODE_ATTR_POSITION, resources.template_vbo, 0, 2 * sizeof (float));

  glEnableVertexAttribArray(NODE_ATTR_HEAT);
  glVertexAttribFormat(NODE_ATTR_HEAT, 1, GL_UNSIGNED_BYTE, GL_TRUE, 0);
  glVertexAttribBinding(NODE_ATTR_HEAT, NODE_ATTR_HEAT);
  glVertexBindingDivisor(NODE_ATTR_HEAT, 1);

  glEnableVertexAttribArray(NODE_ATTR_OFFSET);
  glVertexAttribFormat(NODE_ATTR_OFFSET, 2, GL_FLOAT, GL_FALSE, 0);
  glVertexAttribBinding(NODE_ATTR_OFFSET, NODE_ATTR_OFFSET);
  glVertexBindingDivisor(NODE_ATTR_OFFSET, 1);

  glGenVertexArrays(1, &resources.edge_vao);
  glBindVertexArray(resources.edge_vao);

  glEnableVertexAttribArray(EDGE_ATTR_POSITION);
  glVertexAttribFormat(EDGE_ATTR_POSITION, 2, GL_FLOAT, GL_FALSE, 0);
  glVertexAttribBinding(EDGE_ATTR_POSITION, EDGE_ATTR_POSITION);

  glEnableVertexAttribArray(EDGE_ATTR_WEIGHT);
  glVertexAttribFormat(EDGE_ATTR_WEIGHT, 1, GL_UNSIGNED_BYTE, GL_TRUE, 0);
  glVertexAttribBinding(EDGE_ATTR_WEIGHT, EDGE_ATTR_WEIGHT);
}

GraphRenderer::~GraphRenderer() {
  glDeleteVertexArrays(1, &resources.node_vao);
  glDeleteVertexArrays(1, &resources.edge_vao);
  glDeleteBuffers(1, &this->node_vbo);
  glDeleteBuffers(1, &this->edge_ibo);
}

void GraphRenderer::draw(Camera &camera, int width, int height) {
  float view_matrix[16];
  camera.toMatrix(view_matrix, width, height);

  glEnable(GL_BLEND);
  glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

  glUseProgram(resources.edge_prog);
  glUniformMatrix4fv(glGetUniformLocation(resources.edge_prog, "world_to_view"), 1, GL_FALSE, view_matrix);
  glBindVertexArray(resources.edge_vao);
  glBindVertexBuffer(EDGE_ATTR_POSITION, this->node_vbo, 0, 2 * sizeof (float));
  glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, this->edge_ibo);
  glDrawElements(GL_LINES, 2 * this->edge_count, GL_UNSIGNED_INT, 0);

  glUseProgram(resources.node_prog);
  glUniformMatrix4fv(glGetUniformLocation(resources.node_prog, "world_to_view"), 1, GL_FALSE, view_matrix);
  glBindVertexArray(resources.node_vao);
  glBindVertexBuffer(NODE_ATTR_POSITION, this->node_vbo, 0, 2 * sizeof (float));
  glBindVertexBuffer(NODE_ATTR_HEAT,     this->node_vbo, 8 * NODES_PER_VBO, 1);
  glDrawArraysInstanced(GL_TRIANGLE_STRIP, 0, 4, this->node_count);
}

void GraphRenderer::onWeightUpdate(WeightUpdate &update) {

}

void GraphRenderer::onHeatUpdate(HeatUpdate &update) {

}

void GraphRenderer::onLayoutChange() {

}

void GraphRenderer::onLayoutChange(std::vector<int> changed) {

}

void GraphRenderer::onReload() {

}

} // namespace video
} // namespace satviz
