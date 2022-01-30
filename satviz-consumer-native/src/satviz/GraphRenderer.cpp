#include <satviz/GraphRenderer.hpp>
#include <satviz/GlUtils.hpp>

namespace satviz {
namespace video {

#define UNIFORM_WORLD_TO_VIEW 0

#define ATTR_NODE_POSITION 0
#define ATTR_NODE_HEAT     1
#define ATTR_NODE_OFFSET   2

#define ATTR_EDGE_INDICES  0
#define ATTR_EDGE_WEIGHT   1

#define SENTINEL_INDEX 0xFFFFFFFF

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

GraphRenderer::GraphRenderer(graph::Graph &gr)
  : GraphObserver(gr), edge_capacity(10), edge_mapping(gr.getOgdfGraph(), -1) {
  node_count = my_graph.getOgdfGraph().numberOfNodes();

  // Generate OpenGL handles
  glGenVertexArrays(1, &node_state);
  glGenVertexArrays(1, &edge_state);
  glGenBuffers(NUM_BUFFER_OBJECTS, buffer_objects);
  glGenTextures(1, &heat_palette);
  glGenTextures(1, &offset_texview);

  // Allocate buffers
  glBindBuffer(GL_ARRAY_BUFFER, buffer_objects[BO_NODE_OFFSET]);
  glBufferData(GL_ARRAY_BUFFER, 2 * sizeof (float) * node_count, NULL, GL_DYNAMIC_DRAW);
  glBindBuffer(GL_ARRAY_BUFFER, buffer_objects[BO_NODE_HEAT]);
  glBufferData(GL_ARRAY_BUFFER, 1 * sizeof (char) * node_count, NULL, GL_DYNAMIC_DRAW);
  glBindBuffer(GL_ARRAY_BUFFER, buffer_objects[BO_EDGE_INDICES]);
  glBufferData(GL_ARRAY_BUFFER, sizeof (unsigned[2]) * edge_capacity, NULL, GL_DYNAMIC_DRAW);
  glBindBuffer(GL_ARRAY_BUFFER, buffer_objects[BO_EDGE_WEIGHT]);
  glBufferData(GL_ARRAY_BUFFER, 1 * sizeof (char) * edge_capacity, NULL, GL_DYNAMIC_DRAW);

  glBindTexture(GL_TEXTURE_BUFFER, offset_texview);
  glTexBuffer(GL_TEXTURE_BUFFER, GL_RG32F, buffer_objects[BO_NODE_OFFSET]);
  glUseProgram(resources.edge_prog);
  glUniform1i(glGetUniformLocation(resources.edge_prog, "offset_texview"), 0);

  // Set up node render state
  glBindVertexArray(node_state);
  simpleGlVertexAttrib(ATTR_NODE_POSITION, resources.template_vbo,         2, GL_FLOAT,         0);
  simpleGlVertexAttrib(ATTR_NODE_HEAT,     buffer_objects[BO_NODE_HEAT],   1, GL_UNSIGNED_BYTE, 1);
  simpleGlVertexAttrib(ATTR_NODE_OFFSET,   buffer_objects[BO_NODE_OFFSET], 2, GL_FLOAT,         1);

  // Set up edge render state
  glBindVertexArray(edge_state);
  glBindBuffer(GL_ARRAY_BUFFER, buffer_objects[BO_EDGE_INDICES]);
  glEnableVertexAttribArray(ATTR_EDGE_INDICES);
  glVertexAttribIPointer(ATTR_EDGE_INDICES, 2, GL_UNSIGNED_INT, 0, (void *) 0);
  glVertexAttribDivisor(ATTR_EDGE_INDICES, 1);
  simpleGlVertexAttrib(ATTR_EDGE_WEIGHT, buffer_objects[BO_EDGE_WEIGHT], 1, GL_UNSIGNED_BYTE, 1);

  glBindBuffer(GL_ARRAY_BUFFER, buffer_objects[BO_EDGE_INDICES]);
  for (int i = 0; i < edge_capacity; i++) {
    free_edges.push_back(i);
    unsigned data[2] = { SENTINEL_INDEX, SENTINEL_INDEX };
    glBufferSubData(GL_ARRAY_BUFFER, i * sizeof (unsigned[2]), sizeof (unsigned[2]), data);
  }

  // Set up heatmap color palette
  const int heat_palette_width = 4;
  const GLuint palette_colors[] = {
      0xFFA0A0A0,
      0xFF8C8000,
      0xFF4DB3F2,
      0xFF00A0FF,
  };
  glBindTexture(GL_TEXTURE_1D, heat_palette);
  glTexParameteri(GL_TEXTURE_1D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
  glTexParameteri(GL_TEXTURE_1D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
  glTexParameteri(GL_TEXTURE_1D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
  glTexImage1D(GL_TEXTURE_1D, 0, GL_RGBA, heat_palette_width, 0, GL_RGBA, GL_UNSIGNED_BYTE, palette_colors);
  glUseProgram(resources.node_prog);
  glUniform1i(glGetUniformLocation(resources.node_prog, "heat_palette"), 0);
}

GraphRenderer::~GraphRenderer() {
  glDeleteVertexArrays(1, &node_state);
  glDeleteVertexArrays(1, &edge_state);
  glDeleteBuffers(NUM_BUFFER_OBJECTS, buffer_objects);
  glDeleteTextures(1, &heat_palette);
  glDeleteTextures(1, &offset_texview);
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
  glBindTexture(GL_TEXTURE_BUFFER, offset_texview);
  glDrawArraysInstanced(GL_LINES, 0, 2, edge_capacity);

  // Draw nodes
  glUseProgram(resources.node_prog);
  glUniformMatrix4fv(UNIFORM_WORLD_TO_VIEW, 1, GL_FALSE, view_matrix);
  glBindVertexArray(node_state);
  glBindTexture(GL_TEXTURE_1D, heat_palette);
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

void GraphRenderer::onLayoutChange(ogdf::Array<ogdf::node> &changed) {
  glBindBuffer(GL_ARRAY_BUFFER, buffer_objects[BO_NODE_OFFSET]);
  float (*area)[2] = (float (*)[2]) glMapBuffer(GL_ARRAY_BUFFER, GL_READ_WRITE);
  // TODO proper mapping of nodes to indices!
  printf("onLayoutChange():\n");
  for (ogdf::node node : changed) {
    int idx = node->index();
    area[idx][0] = (float) my_graph.getX(node);
    area[idx][1] = (float) my_graph.getY(node);
    printf("\t[%03d] = %f, %f\n", idx, area[idx][0], area[idx][1]);
  }
  glUnmapBuffer(GL_ARRAY_BUFFER);
}

void GraphRenderer::onEdgeAdded(ogdf::edge e) {
  int index = edge_mapping[e];
  if (index < 0) {
    if (free_edges.empty()) {
      int new_capacity = 2 * edge_capacity;
      resizeGlBuffer(&buffer_objects[BO_EDGE_INDICES], edge_capacity * sizeof(unsigned[2]), new_capacity * sizeof (unsigned[2]));
      glBindBuffer(GL_ARRAY_BUFFER, buffer_objects[BO_EDGE_INDICES]);
      for (int i = edge_capacity; i < new_capacity; i++) {
        free_edges.push_back(i);
        unsigned data[2] = { SENTINEL_INDEX, SENTINEL_INDEX };
        glBufferSubData(GL_ARRAY_BUFFER, i * sizeof (unsigned[2]), sizeof (unsigned[2]), data);
      }
      edge_capacity = new_capacity;
    }
    index = free_edges.back();
    free_edges.pop_back();
    edge_mapping[e] = index;
  }
  int offset = index * sizeof(unsigned[2]);

  // TODO proper mapping of nodes to indices!
  std::array<ogdf::node, 2> nodes = e->nodes();
  int data[2] = { nodes[0]->index(), nodes[1]->index() };

  glBindBuffer(GL_ARRAY_BUFFER, buffer_objects[BO_EDGE_INDICES]);
  glBufferSubData(GL_ARRAY_BUFFER, offset, sizeof (unsigned[2]), data);
}

void GraphRenderer::onEdgeDeleted(ogdf::edge e) {
  int index = edge_mapping[e];
  if (index >= 0) {
    free_edges.push_back(index);
    glBindBuffer(GL_ARRAY_BUFFER, buffer_objects[BO_EDGE_INDICES]);
    unsigned data[2] = { SENTINEL_INDEX, SENTINEL_INDEX };
    glBufferSubData(GL_ARRAY_BUFFER, index * sizeof (unsigned[2]), sizeof (unsigned[2]), data);
    edge_mapping[e] = -1;
  }
}

void GraphRenderer::onReload() {
  // TODO also update the other attributes!

  ogdf::Array<ogdf::node> nodes;
  my_graph.getOgdfGraph().allNodes(nodes);
  onLayoutChange(nodes);

  ogdf::Array<ogdf::edge> edges;
  my_graph.getOgdfGraph().allEdges(edges);
  for (auto edge : edges) {
    onEdgeDeleted(edge);
  }
  for (auto edge : edges) {
    onEdgeAdded(edge);
  }
}

} // namespace video
} // namespace satviz
