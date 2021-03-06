#include <satviz/GraphRenderer.hpp>
#include <satviz/GlUtils.hpp>

namespace satviz {
namespace video {

#define UNIFORM_WORLD_TO_VIEW 0
#define UNIFORM_EDGE_COLOR    1
#define UNIFORM_NODE_SIZE     1

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
    : GraphObserver(gr) {
  init();
}

GraphRenderer::~GraphRenderer() {
  deinit();
}

void GraphRenderer::init() {
  node_count = my_graph.getOgdfGraph().numberOfNodes();
  edge_capacity = 10;
  edge_mapping.init(my_graph.getOgdfGraph(), -1);

  // Generate OpenGL handles
  glGenVertexArrays(1, &node_state);
  glGenVertexArrays(1, &edge_state);
  glGenBuffers(NUM_BUFFER_OBJECTS, buffer_objects);
  glGenTextures(1, &heat_palette);
  glGenTextures(1, &offset_texview);

  // Allocate buffers
  allocateGlBuffer(buffer_objects[BO_NODE_OFFSET],  "node:offset",  node_count * sizeof (float[2]));
  allocateGlBuffer(buffer_objects[BO_NODE_HEAT],    "node:heat",    node_count * sizeof (char));
  allocateGlBuffer(buffer_objects[BO_EDGE_INDICES], "edge:indices", edge_capacity * sizeof (unsigned[2]));
  allocateGlBuffer(buffer_objects[BO_EDGE_WEIGHT],  "edge:weight",  edge_capacity * sizeof (char));

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

  // Allocate the heatmap color palette
  glBindTexture(GL_TEXTURE_1D, heat_palette);
  glTexParameteri(GL_TEXTURE_1D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
  glTexParameteri(GL_TEXTURE_1D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
  glTexParameteri(GL_TEXTURE_1D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
  glTexImage1D(GL_TEXTURE_1D, 0, GL_RGB, 2, 0, GL_RGB, GL_FLOAT, NULL);
  //glUseProgram(resources.node_prog);
  //glUniform1i(glGetUniformLocation(resources.node_prog, "heat_palette"), 0);
}

void GraphRenderer::deinit() {
  glDeleteVertexArrays(1, &node_state);
  glDeleteVertexArrays(1, &edge_state);
  glDeleteBuffers(NUM_BUFFER_OBJECTS, buffer_objects);
  glDeleteTextures(1, &heat_palette);
  glDeleteTextures(1, &offset_texview);
}

void GraphRenderer::applyTheme(const Theme &theme) {
  glBindTexture(GL_TEXTURE_1D, heat_palette);
  glTexSubImage1D(GL_TEXTURE_1D, 0, 0, 1, GL_RGB, GL_FLOAT, theme.coldColor);
  glTexSubImage1D(GL_TEXTURE_1D, 0, 1, 1, GL_RGB, GL_FLOAT, theme.hotColor);

  glUseProgram(resources.edge_prog);
  glUniform3fv(UNIFORM_EDGE_COLOR, 1, theme.edgeColor);

  bg_color[0] = theme.bgColor[0];
  bg_color[1] = theme.bgColor[1];
  bg_color[2] = theme.bgColor[2];
  node_size = theme.nodeSize;
}

void GraphRenderer::clearScreen() {
  glClearColor(bg_color[0], bg_color[1], bg_color[2], 0.0f);
  glClear(GL_COLOR_BUFFER_BIT);
}

void GraphRenderer::uniformViewMatrix(double *matrix) {
  if (GLAD_GL_ARB_gpu_shader_fp64) {
    glUniformMatrix4dv(UNIFORM_WORLD_TO_VIEW, 1, GL_FALSE, matrix);
  } else {
    float reduced[16];
    for (int i = 0; i < 16; i++) {
      reduced[i] = (float) matrix[i];
    }
    glUniformMatrix4fv(UNIFORM_WORLD_TO_VIEW, 1, GL_FALSE, reduced);
  }
}

void GraphRenderer::draw(Camera &camera, int width, int height) {
  double view_matrix[16];
  camera.toMatrix(view_matrix);

  glEnable(GL_BLEND);
  glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

  // Draw edges
  glUseProgram(resources.edge_prog);
  uniformViewMatrix(view_matrix);
  glBindVertexArray(edge_state);
  glBindTexture(GL_TEXTURE_BUFFER, offset_texview);
  glDrawArraysInstanced(GL_LINES, 0, 2, edge_capacity);

  // Draw nodes
  glUseProgram(resources.node_prog);
  uniformViewMatrix(view_matrix);
  glUniform2f(UNIFORM_NODE_SIZE, node_size / (float) width, node_size / (float) height);
  glBindVertexArray(node_state);
  glBindTexture(GL_TEXTURE_1D, heat_palette);
  glDrawArraysInstanced(GL_TRIANGLE_STRIP, 0, 4, node_count);
}

void GraphRenderer::onWeightChange(ogdf::Array<ogdf::edge> &changed) {
  ogdf::GraphAttributes &attrs = my_graph.getOgdfAttrs();
  glBindBuffer(GL_ARRAY_BUFFER, buffer_objects[BO_EDGE_WEIGHT]);
  unsigned char *area = (unsigned char *) glMapBuffer(GL_ARRAY_BUFFER, GL_READ_WRITE);
  for (auto e : changed) {
    int idx = edge_mapping[e];
    double w = attrs.doubleWeight(e);
    double I = w / (10.0 + w);
    I = 0.1 + 0.9 * I;
    area[idx] = (unsigned char) (I * 256.0);
  }
  glUnmapBuffer(GL_ARRAY_BUFFER);
}

void GraphRenderer::onHeatChange(ogdf::Array<ogdf::node> &changed) {
  ogdf::GraphAttributes &attrs = my_graph.getOgdfAttrs();
  glBindBuffer(GL_ARRAY_BUFFER, buffer_objects[BO_NODE_HEAT]);
  unsigned char *area = (unsigned char *) glMapBuffer(GL_ARRAY_BUFFER, GL_READ_WRITE);
  for (auto v : changed) {
    int idx = v->index();
    area[idx] = (unsigned char) attrs.weight(v);
  }
  glUnmapBuffer(GL_ARRAY_BUFFER);
}

void GraphRenderer::onLayoutChange(ogdf::Array<ogdf::node> &changed) {
  glBindBuffer(GL_ARRAY_BUFFER, buffer_objects[BO_NODE_OFFSET]);
  float (*area)[2] = (float (*)[2]) glMapBuffer(GL_ARRAY_BUFFER, GL_READ_WRITE);
  //printf("onLayoutChange():\n");
  for (ogdf::node node : changed) {
    int idx = node->index();
    area[idx][0] = (float) my_graph.getX(node);
    area[idx][1] = (float) my_graph.getY(node);
    //printf("\t[%03d] = %f, %f\n", idx, area[idx][0], area[idx][1]);
  }
  glUnmapBuffer(GL_ARRAY_BUFFER);
}

int GraphRenderer::allocateEdgeIndex() {
  // Resize everything if we ran out of free indices
  if (free_edges.empty()) {
    // Resize OpenGL buffers
    int new_capacity = edge_capacity + edge_capacity / 2;
    resizeGlBuffer(&buffer_objects[BO_EDGE_INDICES], edge_capacity * sizeof(unsigned[2]), new_capacity * sizeof (unsigned[2]));
    resizeGlBuffer(&buffer_objects[BO_EDGE_WEIGHT], edge_capacity * sizeof(char), new_capacity * sizeof (char));

    // The buffer ids have changed, we have to re-register the VAO vertex attributes
    glBindVertexArray(edge_state);
    glBindBuffer(GL_ARRAY_BUFFER, buffer_objects[BO_EDGE_INDICES]);
    glEnableVertexAttribArray(ATTR_EDGE_INDICES);
    glVertexAttribIPointer(ATTR_EDGE_INDICES, 2, GL_UNSIGNED_INT, 0, (void *) 0);
    glVertexAttribDivisor(ATTR_EDGE_INDICES, 1);
    simpleGlVertexAttrib(ATTR_EDGE_WEIGHT, buffer_objects[BO_EDGE_WEIGHT], 1, GL_UNSIGNED_BYTE, 1);

    // Properly keep track of the new free indices
    glBindBuffer(GL_ARRAY_BUFFER, buffer_objects[BO_EDGE_INDICES]);
    for (int i = edge_capacity; i < new_capacity; i++) {
      free_edges.push_back(i);
      unsigned data[2] = { SENTINEL_INDEX, SENTINEL_INDEX };
      glBufferSubData(GL_ARRAY_BUFFER, i * sizeof (unsigned[2]), sizeof (unsigned[2]), data);
    }
    edge_capacity = new_capacity;
  }

  // Take a new index off the free list
  int index = free_edges.back();
  free_edges.pop_back();
  return index;
}

void GraphRenderer::freeEdgeIndex(int index) {
  free_edges.push_back(index);
  glBindBuffer(GL_ARRAY_BUFFER, buffer_objects[BO_EDGE_INDICES]);
  unsigned data[2] = { SENTINEL_INDEX, SENTINEL_INDEX };
  glBufferSubData(GL_ARRAY_BUFFER, index * sizeof (unsigned[2]), sizeof (unsigned[2]), data);
}

void GraphRenderer::onEdgeAdded(ogdf::edge e) {
  int index = edge_mapping[e];
  if (index < 0) {
    index = allocateEdgeIndex();
    edge_mapping[e] = index;
  }
  int offset = index * sizeof(unsigned[2]);

  std::array<ogdf::node, 2> nodes = e->nodes();
  int data[2] = { nodes[0]->index(), nodes[1]->index() };

  glBindBuffer(GL_ARRAY_BUFFER, buffer_objects[BO_EDGE_INDICES]);
  glBufferSubData(GL_ARRAY_BUFFER, offset, sizeof (unsigned[2]), data);
}

void GraphRenderer::onEdgeDeleted(ogdf::edge e) {
  int index = edge_mapping[e];
  if (index >= 0) {
    freeEdgeIndex(index);
    edge_mapping[e] = -1;
  }
}

void GraphRenderer::onReload() {
  ogdf::Array<ogdf::node> nodes;
  my_graph.getOgdfGraph().allNodes(nodes);
  ogdf::Array<ogdf::edge> edges;
  my_graph.getOgdfGraph().allEdges(edges);

  free_edges.resize(edge_capacity);
  glBindBuffer(GL_ARRAY_BUFFER, buffer_objects[BO_EDGE_INDICES]);
  unsigned data[2] = { SENTINEL_INDEX, SENTINEL_INDEX };
  for (int i = 0; i < edge_capacity; i++) {
    free_edges[i] = i;
    glBufferSubData(GL_ARRAY_BUFFER, i * sizeof (unsigned[2]), sizeof (unsigned[2]), data);
  }
  edge_mapping.fill(-1);
  for (auto e : edges) {
    onEdgeAdded(e);
  }

  onHeatChange(nodes);
  onLayoutChange(nodes);
  onWeightChange(edges);
}

} // namespace video
} // namespace satviz
