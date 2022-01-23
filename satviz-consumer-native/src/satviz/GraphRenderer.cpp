#include <satviz/GraphRenderer.hpp>
#include <satviz/GlUtils.hpp>

namespace satviz {
namespace video {

#define UNIFORM_WORLD_TO_VIEW 0

#define ATTR_NODE_POSITION 0
#define ATTR_NODE_HEAT     1
#define ATTR_NODE_OFFSET   2

#define ATTR_EDGE_POSITION 0
#define ATTR_EDGE_WEIGHT   1

static const float template_coordinates[] = {
     1.0f, -1.0f,
     1.0f,  1.0f,
    -1.0f, -1.0f,
    -1.0f,  1.0f,
};

void GraphRenderer::initializeResources() {
  // Load shaders
#include <ShaderSources.inl>
  GLuint node_vert_id = compileGlShader(node_vert, node_vert_size, GL_VERTEX_SHADER);
  GLuint node_frag_id = compileGlShader(node_frag, node_frag_size, GL_FRAGMENT_SHADER);
  GLuint edge_vert_id = compileGlShader(edge_vert, edge_vert_size, GL_VERTEX_SHADER);
  GLuint edge_frag_id = compileGlShader(edge_frag, edge_frag_size, GL_FRAGMENT_SHADER);
  resources.node_prog = linkGlProgram(node_vert_id, node_frag_id);
  resources.edge_prog = linkGlProgram(edge_vert_id, edge_frag_id);
  glDeleteShader(node_vert_id);
  glDeleteShader(node_frag_id);
  glDeleteShader(edge_vert_id);
  glDeleteShader(edge_frag_id);
  // Upload template geometry to VRAM
  glGenBuffers(1, &resources.template_vbo);
  glBindBuffer(GL_ARRAY_BUFFER, resources.template_vbo);
  glBufferData(GL_ARRAY_BUFFER, sizeof template_coordinates, template_coordinates, GL_STATIC_DRAW);
}

void GraphRenderer::terminateResources() {
  glDeleteBuffers(1, &resources.template_vbo);
  glDeleteProgram(resources.node_prog);
  glDeleteProgram(resources.edge_prog);
}

GraphRenderer::GraphRenderer(graph::Graph *gr)
  : GraphObserver(gr), node_count(0), edge_count(0), node_capacity(1000), edge_capacity(1000) {

  // Generate OpenGL handles
  glGenVertexArrays(1, &node_state);
  glGenVertexArrays(1, &edge_state);
  glGenBuffers(NUM_BUFFER_OBJECTS, buffer_objects);

  // Allocate buffers
  glBindBuffer(GL_ARRAY_BUFFER, buffer_objects[BO_NODE_OFFSET]);
  glBufferData(GL_ARRAY_BUFFER, 2 * sizeof (float) * node_capacity, NULL, GL_DYNAMIC_DRAW);
  glBindBuffer(GL_ARRAY_BUFFER, buffer_objects[BO_NODE_HEAT]);
  glBufferData(GL_ARRAY_BUFFER, 1 * sizeof (char) * node_capacity, NULL, GL_DYNAMIC_DRAW);
  glBindBuffer(GL_ARRAY_BUFFER, buffer_objects[BO_EDGE_WEIGHT]);
  glBufferData(GL_ARRAY_BUFFER, 1 * sizeof (char) * edge_capacity, NULL, GL_DYNAMIC_DRAW);
  glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, buffer_objects[BO_EDGE_INDICES]);
  glBufferData(GL_ELEMENT_ARRAY_BUFFER, 2 * sizeof (unsigned) * edge_capacity, NULL, GL_DYNAMIC_DRAW);

  // Set up node render state
  glBindVertexArray(node_state);

  glBindBuffer(GL_ARRAY_BUFFER, resources.template_vbo);
  glEnableVertexAttribArray(ATTR_NODE_POSITION);
  glVertexAttribPointer(ATTR_NODE_POSITION, 2, GL_FLOAT, GL_TRUE, 0, (void *) 0);

  glBindBuffer(GL_ARRAY_BUFFER, buffer_objects[BO_NODE_HEAT]);
  glEnableVertexAttribArray(ATTR_NODE_HEAT);
  glVertexAttribPointer(ATTR_NODE_HEAT, 1, GL_UNSIGNED_BYTE, GL_TRUE, 0, (void *) 0);
  glVertexAttribDivisor(ATTR_NODE_HEAT, 1);

  glBindBuffer(GL_ARRAY_BUFFER, buffer_objects[BO_NODE_OFFSET]);
  glEnableVertexAttribArray(ATTR_NODE_OFFSET);
  glVertexAttribPointer(ATTR_NODE_OFFSET, 2, GL_FLOAT, GL_TRUE, 0, (void *) 0);
  glVertexAttribDivisor(ATTR_NODE_OFFSET, 1);

  // Set up edge render state
  glBindVertexArray(edge_state);

  glBindBuffer(GL_ARRAY_BUFFER, buffer_objects[BO_NODE_OFFSET]);
  glEnableVertexAttribArray(ATTR_EDGE_POSITION);
  glVertexAttribPointer(ATTR_EDGE_POSITION, 2, GL_FLOAT, GL_TRUE, 0, (void *) 0);

  glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, buffer_objects[BO_EDGE_INDICES]);
}

GraphRenderer::~GraphRenderer() {
  glDeleteVertexArrays(1, &node_state);
  glDeleteVertexArrays(1, &edge_state);
  glDeleteBuffers(NUM_BUFFER_OBJECTS, buffer_objects);
}

void GraphRenderer::draw(Camera &camera, int width, int height) {
  float view_matrix[16];
  camera.toMatrix(view_matrix, width, height);

  glEnable(GL_BLEND);
  glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

  // Draw edges
  glUseProgram(resources.edge_prog);
  glUniformMatrix4fv(UNIFORM_WORLD_TO_VIEW, 1, GL_FALSE, view_matrix);
  glBindVertexArray(edge_state);
  glDrawElements(GL_LINES, 2 * edge_count, GL_UNSIGNED_INT, 0);

  // Draw nodes
  glUseProgram(resources.node_prog);
  glUniformMatrix4fv(UNIFORM_WORLD_TO_VIEW, 1, GL_FALSE, view_matrix);
  glBindVertexArray(node_state);
  glDrawArraysInstanced(GL_TRIANGLE_STRIP, 0, 4, node_count);
}

void GraphRenderer::onWeightUpdate(graph::WeightUpdate &update) {
  (void) update;
}

void GraphRenderer::onHeatUpdate(graph::HeatUpdate &update) {
  (void) update;
}

void GraphRenderer::onLayoutChange() {

}

void GraphRenderer::onLayoutChange(std::vector<int> changed) {
  (void) changed;
}

void GraphRenderer::onReload() {

}

} // namespace video
} // namespace satviz
