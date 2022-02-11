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

void Graph::submitWeightUpdate(WeightUpdate &update) {
  // TODO stub
  (void) update;
}

void Graph::submitHeatUpdate(HeatUpdate &update) {
  // TODO stub
  (void) update;
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

void Graph::adaptLayout() {
  // TODO stub
}

std::stringbuf Graph::serialize() {
  // TODO stub
  std::stringbuf buf;
  return buf;
}

void Graph::deserialize(std::stringbuf &buf) {
  // TODO stub
  (void) buf;
}

NodeInfo Graph::queryNode(int index) {
  // TODO stub
  (void) index;
  NodeInfo info = { 0, 0, 0.0f, 0.0f };
  return info;
}

EdgeInfo Graph::queryEdge(int index1, int index2) {
  // TODO stub
  (void) index1;
  (void) index2;
  EdgeInfo info = { 0, 0, 0.0f };
  return info;
}

} // namespace graph
} // namespace satviz
