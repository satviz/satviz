#ifndef SATVIZ_GRAPH_RENDERER_HPP_
#define SATVIZ_GRAPH_RENDERER_HPP_

#include <satviz/GraphObserver.hpp>
#include <satviz/Camera.hpp>

#include <ogdf/basic/EdgeArray.h>

namespace satviz {
namespace video {

/**
 * Visual representation of a graph. Implements hardware-accelerated graphics.
 */
class GraphRenderer : public graph::GraphObserver {
private:
  enum {
    BO_NODE_OFFSET,
    BO_NODE_HEAT,
    BO_EDGE_INDICES,
    BO_EDGE_WEIGHT,
    NUM_BUFFER_OBJECTS
  };

  /**
   * A bundle of OpenGL resources that only need to be created once, not per graph.
   */
  struct Resources {
    unsigned node_prog;
    unsigned edge_prog;
    unsigned template_vbo;
  };

  static Resources resources;

  unsigned node_state;
  unsigned edge_state;
  unsigned buffer_objects[NUM_BUFFER_OBJECTS];
  unsigned offset_texview;
  unsigned heat_palette;
  int node_count;
  int edge_capacity;
  ogdf::EdgeArray<int> edge_mapping;
  std::vector<int> free_edges;

public:
  static void initializeResources();
  static void terminateResources();

  GraphRenderer(graph::Graph &gr);
  virtual ~GraphRenderer();

  /**
   * Draw the associated graph onto the OpenGL framebuffer.
   *
   * @param camera The virtual camera from which the graph should be viewed
   * @param width  the width of the display
   * @param height the height of the display
   */
  void draw(Camera &camera, int width, int height);

  void onWeightUpdate(graph::WeightUpdate &update) override;
  void onHeatUpdate(graph::HeatUpdate &update) override;
  void onLayoutChange(ogdf::Array<ogdf::node> &changed) override;
  void onEdgeAdded(ogdf::edge e) override;
  void onEdgeDeleted(ogdf::edge e) override;
  void onReload() override;
};

} // namespace video
} // namespace satviz

#endif
