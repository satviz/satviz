#include <satviz/Graph.hpp>

#include <ogdf/energybased/FMMMLayout.h>

namespace satviz {
namespace graph {

Graph::Graph(size_t num_nodes)
  : graph(), attrs(graph, ogdf::GraphAttributes::nodeGraphics) {
  for (size_t i = 0; i < num_nodes; i++) {
    graph.newNode();
  }
}

void Graph::recalculateLayout() {
  ogdf::FMMMLayout fmmm;
  fmmm.useHighLevelOptions(true);
  fmmm.unitEdgeLength(15.0);
  fmmm.newInitialPlacement(true);
  fmmm.qualityVersusSpeed(ogdf::FMMMOptions::QualityVsSpeed::GorgeousAndEfficient);
  fmmm.call(attrs);
}

} // namespace graph
} // namespace satviz
