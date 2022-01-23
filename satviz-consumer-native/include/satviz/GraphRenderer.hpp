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
  struct Resources {
    unsigned node_prog;
    unsigned edge_prog;
    unsigned template_vbo;
  };

  static Resources resources;

  unsigned node_vao;
  unsigned edge_vao;
  unsigned node_vbo;
  unsigned edge_ibo;
  int node_count;
  int edge_count;
  ogdf::EdgeArray<int> edge_mapping;

public:
  static void initializeResources();
  static void terminateResources();

  GraphRenderer(graph::Graph *gr);
  ~GraphRenderer();

  void draw(Camera &camera, int width, int height);

  void onWeightUpdate(graph::WeightUpdate &update);
  void onHeatUpdate(graph::HeatUpdate &update);
  void onLayoutChange();
  void onLayoutChange(std::vector<int> changed);
  void onReload();
};

} // namespace video
} // namespace satviz

#endif
