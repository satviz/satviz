#ifndef SATVIZ_GRAPH_RENDERER_HPP_
#define SATVIZ_GRAPH_RENDERER_HPP_

#include <satviz/GraphObserver.hpp>
#include <satviz/Camera.hpp>

#include <ogdf/basic/EdgeArray.h>

#include <satviz/Theme.h>

namespace satviz {
namespace video {

/**
 * Visual representation of a graph. Implements hardware-accelerated graphics.
 *
 * Each GraphRenderer object is associated with exactly one Graph.
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
  /// Mapping from edge handles to edge indices
  ogdf::EdgeArray<int> edge_mapping;
  /// List of free/unused edge indices
  std::vector<int> free_edges;

  float bg_color[3] = { 0.0f, 0.0f, 0.0f };
  float node_size = 0.0f;

  /**
   * Initialize rendering data.
   *
   * Gets called by the constructor.
   */
  void init();
  /**
   * Un-Initialize rendering data.
   *
   * Gets called by the destructor.
   */
  void deinit();

  void uniformViewMatrix(double *matrix);

  int  allocateEdgeIndex();
  void freeEdgeIndex(int index);

public:
  /**
   * The GraphRenderer needs some global (per GL context) state to operate.
   * This static method initializes theses resources.
   */
  static void initializeResources();
  /**
   * The GraphRenderer needs some global (per GL context) state to operate.
   * This static method un-initializes theses resources.
   */
  static void terminateResources();

  /**
   * Creates a new GraphRenderer.
   *
   * Note that the GraphRenderer still has to be registered as an observer afterwards.
   *
   * @param gr a reference to the graph that this GraphRenderer should be attached to.
   */
  GraphRenderer(graph::Graph &gr);
  virtual ~GraphRenderer();

  /**
   * Applies the contents of a Theme to the renderer.
   * @param theme the theme to apply
   */
  void applyTheme(const Theme &theme);

  /**
   * Overwrite the contents of the screen with the current background color.
   */
  void clearScreen();

  /**
   * Draw the associated graph onto the OpenGL framebuffer.
   *
   * @param camera The virtual camera from which the graph should be viewed
   * @param width  the width of the display
   * @param height the height of the display
   */
  void draw(Camera &camera, int width, int height);

  // The following methods are all inherited from GraphObserver
  void onWeightChange(ogdf::Array<ogdf::edge> &changed) override;
  void onHeatChange(ogdf::Array<ogdf::node> &changed) override;
  void onLayoutChange(ogdf::Array<ogdf::node> &changed) override;
  void onEdgeAdded(ogdf::edge e) override;
  void onEdgeDeleted(ogdf::edge e) override;
  void onReload() override;
};

} // namespace video
} // namespace satviz

#endif
