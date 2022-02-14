#include <satviz/Graph.hpp>
#include <satviz/GraphObserver.hpp>

#include <ogdf/energybased/FMMMLayout.h>

#include <algorithm>

namespace satviz {
namespace graph {

void Graph::setup() {
  using GA = ogdf::GraphAttributes;
  attrs.init(graph, GA::nodeGraphics | GA::nodeWeight | GA::edgeGraphics | GA::edgeDoubleWeight);
  attrs.directed = false;
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

void Graph::addObserver(GraphObserver *o) {
  observers.push_back(o);
}

void Graph::removeObserver(GraphObserver *o) {
  auto pos = std::find(observers.begin(), observers.end(), o);
  if (pos != observers.end()) {
    observers.erase(pos);
  }
}

void Graph::submitWeightUpdate(WeightUpdate &update) {
  for (auto row : update.values) {
    auto v = node_handles[std::get<0>(row)];
    auto w = node_handles[std::get<1>(row)];
    auto e = graph.searchEdge(v, w, false);
    attrs.doubleWeight(e) = std::get<2>(row);
  }
}

void Graph::submitHeatUpdate(HeatUpdate &update) {
  for (auto row : update.values) {
    auto v = node_handles[std::get<0>(row)];
    attrs.weight(v) = std::get<1>(row);
  }
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
  info.heat  = attrs.weight(v);
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
  info.weight = attrs.doubleWeight(e);
  return info;
}

} // namespace graph
} // namespace satviz
