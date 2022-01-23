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
    GLuint node_prog;
    GLuint edge_prog;
    GLuint template_vbo;
  };

  static Resources resources;

  GLuint node_vao;
  GLuint edge_vao;
  unsigned int node_vbo;
  unsigned int edge_ibo;
  int node_count;
  int edge_count;
  ogdf::EdgeArray<int> edge_mapping;

public:
  static void initializeResources();
  static void terminateResources();

  GraphRenderer(graph::Graph *gr);
  ~GraphRenderer();

  void draw(Camera &camera, int width, int height);

  void onWeightUpdate(WeightUpdate &update);
  void onHeatUpdate(HeatUpdate &update);
  void onLayoutChange();
  void onLayoutChange(std::vector<int> changed);
  void onReload();
};

} // namespace video
} // namespace satviz

#endif
