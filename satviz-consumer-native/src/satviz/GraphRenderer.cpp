#include <satviz/GraphRenderer.hpp>

#include <glad/gl.h>

/* Vertex Attribute Indices */
#define VAI_TEMPLATE 0
#define VAI_POSITION 1
#define VAI_HEAT     2

/* Buffer Binding Indices */
#define BBI_TEMPLATE 0
#define BBI_NODE     1
#define BBI_HEAT     2

#define NODES_PER_VBO 1000
#define EDGES_PER_IBO 1000

namespace satviz {
namespace video {

static const float GR_template_data[] = {
    1.0f, -1.0f,
    1.0f, 1.0f,
    -1.0f, -1.0f,
    -1.0f, 1.0f,
};

static GLuint GR_node_prog;
static GLuint GR_edge_prog;
static GLuint GR_graph_vao;
static GLuint GR_template_vbo;

GraphRenderer::GraphRenderer(graph::Graph *gr)
  : GraphObserver(gr)
{
  this->node_count = 0;
  glGenBuffers(1, &this->node_vbo);
  glBindBuffer(GL_ARRAY_BUFFER, this->node_vbo);
  glBufferData(GL_ARRAY_BUFFER, (8 + 1) * NODES_PER_VBO, NULL, GL_DYNAMIC_DRAW);

  this->edge_count = 0;
  glGenBuffers(1, &this->edge_ibo);
  glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, this->edge_ibo);
  glBufferData(GL_ELEMENT_ARRAY_BUFFER, 8 * EDGES_PER_IBO, NULL, GL_DYNAMIC_DRAW);
}

GraphRenderer::~GraphRenderer()
{
  glDeleteBuffers(1, &this->node_vbo);
  glDeleteBuffers(1, &this->edge_ibo);
}

void GraphRenderer::draw(Camera &camera)
{
  (void) camera;

  glEnable(GL_BLEND);
  glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

  glBindVertexArray(GR_graph_vao);
  glBindVertexBuffer(BBI_NODE, this->node_vbo, 0, 2 * sizeof (float));
  glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, this->edge_ibo);

  glUseProgram(GR_edge_prog);
  glUniformMatrix4fv(glGetUniformLocation(GR_edge_prog, "world_to_view"), 1, GL_FALSE, transform);
  glVertexBindingDivisor(BBI_NODE, 0);
  glDrawElements(GL_LINES, 2 * this->edge_count, GL_UNSIGNED_INT, 0);

  glUseProgram(GR_node_prog);
  glUniformMatrix4fv(glGetUniformLocation(GR_node_prog, "world_to_view"), 1, GL_FALSE, transform);
  glVertexBindingDivisor(BBI_NODE, 1);
  glVertexBindingDivisor(BBI_HEAT, 1);
  glBindVertexBuffer(BBI_HEAT, this->node_vbo, 8 * NODES_PER_VBO, 1);
  glDrawArraysInstanced(GL_TRIANGLE_STRIP, 0, 4, this->node_count);
}

} // namespace video
} // namespace satviz
