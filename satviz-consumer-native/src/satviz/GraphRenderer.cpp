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

GraphRenderer::Resources GraphRenderer::resources;

void GraphRenderer::initializeResources() {
  // Load shaders
#include <ShaderSources.inl>
  GLuint node_vert = compileGlShader(NodeShader_vert, NodeShader_vert_size, GL_VERTEX_SHADER);
  GLuint node_frag = compileGlShader(NodeShader_frag, NodeShader_frag_size, GL_FRAGMENT_SHADER);
  GLuint edge_vert = compileGlShader(EdgeShader_vert, EdgeShader_vert_size, GL_VERTEX_SHADER);
  GLuint edge_frag = compileGlShader(EdgeShader_frag, EdgeShader_frag_size, GL_FRAGMENT_SHADER);
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
  simpleGlVertexAttrib(ATTR_NODE_POSITION, resources.template_vbo,         2, GL_FLOAT,         0);
  simpleGlVertexAttrib(ATTR_NODE_HEAT,     buffer_objects[BO_NODE_HEAT],   1, GL_UNSIGNED_BYTE, 1);
  simpleGlVertexAttrib(ATTR_NODE_OFFSET,   buffer_objects[BO_NODE_OFFSET], 2, GL_FLOAT,         1);

  // Set up edge render state
  glBindVertexArray(edge_state);
  simpleGlVertexAttrib(ATTR_EDGE_POSITION, buffer_objects[BO_NODE_OFFSET], 2, GL_FLOAT, 0);
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
  // TODO update & use this attribute.
}

void GraphRenderer::onHeatUpdate(graph::HeatUpdate &update) {
  (void) update;
  // TODO update this attribute.
}

void GraphRenderer::onLayoutChange() {
  // TODO resize buffer if neccessary!
  glBindBuffer(GL_ARRAY_BUFFER, buffer_objects[BO_NODE_OFFSET]);
  float (*area)[2] = (float (*)[2]) glMapBuffer(GL_ARRAY_BUFFER, GL_WRITE_ONLY);
  auto N = my_graph->nodes();
  // TODO proper mapping to indices!
  int idx = 0;
  for (auto it = N->begin(); it != N->end(); ++it) {
    area[idx][0] = my_graph->nodeX[*it];
    area[idx][1] = my_graph->nodeY[*it];
    idx++;
  }
  glUnmapBuffer(GL_ARRAY_BUFFER);
}

void GraphRenderer::onLayoutChange(std::vector<int> changed) {
  (void) changed;
  // TODO only update things that changed!
  onLayoutChange();
}

void GraphRenderer::onReload() {
  // TODO also update the other attributes!
  onLayoutChange();
}

} // namespace video
} // namespace satviz
