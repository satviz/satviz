#ifndef SATVIZ_GRAPH_RENDERER_HPP_
#define SATVIZ_GRAPH_RENDERER_HPP_

#include <satviz/GraphObserver.hpp>
#include <satviz/Camera.hpp>

#include <ogdf/basic/EdgeArray.h>

namespace satviz {
namespace video {

/**
 *
 */
class GraphRenderer : graph::GraphObserver {
private:
  unsigned int node_vbo;
  unsigned int edge_ibo;
  ogdf::EdgeArray<int> edge_mapping;

public:
  GraphRenderer(graph::Graph *gr);
  ~GraphRenderer();

  void draw(Camera &camera);
};

} // namespace video
} // namespace satviz

#endif
