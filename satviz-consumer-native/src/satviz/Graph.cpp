#include <satviz/Graph.hpp>
#include <satviz/GraphObserver.hpp>

#include <ogdf/energybased/FMMMLayout.h>

namespace satviz {
namespace graph {

void Graph::setup() {
  attrs.init(graph, ogdf::GraphAttributes::nodeGraphics | ogdf::GraphAttributes::edgeGraphics);
  node_heat.init(graph, 0);
  edge_weights.init(graph, 0.0f);
}

Graph::Graph(size_t num_nodes) {
  node_handles.resize(num_nodes, nullptr);
  for (size_t i = 0; i < num_nodes; i++) {
    node_handles[i] = graph.newNode();
  }
  setup();
}

Graph::Graph(ogdf::Graph &graphToCopy) {
  graph = graphToCopy;
  node_handles.resize(graph.numberOfNodes(), nullptr);
  for (auto v = graph.firstNode(); v != graph.lastNode(); v = v->succ()) {
    node_handles[v->index()] = v;
  }
  setup();
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
  // TODO error handling
  ogdf::node v = node_handles[index];

  NodeInfo info;
  info.index = index;
  info.heat  = node_heat[v];
  info.x     = attrs.x(v);
  info.y     = attrs.y(v);
  return info;
}

EdgeInfo Graph::queryEdge(int index1, int index2) {
  // TODO error handling
  ogdf::node v = node_handles[index1];
  ogdf::node w = node_handles[index2];
  ogdf::edge e = graph.searchEdge(v, w, false);

  EdgeInfo info;
  info.index1 = index1;
  info.index2 = index2;
  info.weight = edge_weights[e];
  return info;
}

} // namespace graph
} // namespace satviz
