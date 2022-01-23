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
class GraphRenderer : public graph::GraphObserver {
private:
  enum {
    BO_NODE_OFFSET,
    BO_NODE_HEAT,
    BO_EDGE_WEIGHT,
    BO_EDGE_INDICES,
    NUM_BUFFER_OBJECTS
  };

  struct Resources {
    unsigned node_prog;
    unsigned edge_prog;
    unsigned template_vbo;
  };

  static Resources resources;

  unsigned node_state;
  unsigned edge_state;
  unsigned buffer_objects[NUM_BUFFER_OBJECTS];
  unsigned heat_palette;
  int node_count;
  int edge_count;
  int node_capacity;
  int edge_capacity;
  ogdf::EdgeArray<int> edge_mapping;

public:
  static void initializeResources();
  static void terminateResources();

  GraphRenderer(graph::Graph *gr);
  ~GraphRenderer();

  void draw(Camera &camera, int width, int height);

  void onWeightUpdate(graph::WeightUpdate &update) override;
  void onHeatUpdate(graph::HeatUpdate &update) override;
  void onLayoutChange(ogdf::Array<ogdf::node> &changed) override;
  void onReload() override;
};

} // namespace video
} // namespace satviz

#endif
