#include <satviz/Graph.hpp>
#include <satviz/GraphObserver.hpp>

#include <ogdf/energybased/FMMMLayout.h>

namespace satviz {
namespace graph {

Graph::Graph(size_t num_nodes)
  : graph(), attrs(graph, ogdf::GraphAttributes::nodeGraphics | ogdf::GraphAttributes::edgeGraphics) {
  for (size_t i = 0; i < num_nodes; i++) {
    graph.newNode();
  }
}

Graph::Graph(ogdf::Graph &graphToCopy)
    : graph(graphToCopy), attrs(graph, ogdf::GraphAttributes::nodeGraphics | ogdf::GraphAttributes::edgeGraphics) {
}

void Graph::recalculateLayout() {
  ogdf::FMMMLayout fmmm;
  fmmm.useHighLevelOptions(true);
  fmmm.unitEdgeLength(10.0);
  fmmm.newInitialPlacement(true);
  fmmm.qualityVersusSpeed(ogdf::FMMMOptions::QualityVsSpeed::GorgeousAndEfficient);
  fmmm.call(attrs);

  ogdf::Array<ogdf::node> nodes;
  graph.allNodes(nodes);
  for (auto o : observers) {
    o->onLayoutChange(nodes);
  }
}

} // namespace graph
} // namespace satviz
