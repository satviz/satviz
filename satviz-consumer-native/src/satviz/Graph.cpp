#include <satviz/Graph.hpp>
#include <satviz/GraphObserver.hpp>

#include <ogdf/energybased/FMMMLayout.h>
#include <ogdf/fileformats/GraphIO.h>

#include <algorithm>

namespace satviz {
namespace graph {

void Graph::initAttrs() {
  using GA = ogdf::GraphAttributes;
  attrs.init(graph, GA::nodeGraphics | GA::nodeWeight | GA::edgeGraphics | GA::edgeDoubleWeight);
  attrs.directed() = false;
}

void Graph::initNodeHandles() {
  node_handles.resize(graph.numberOfNodes(), nullptr);
  for (auto v = graph.firstNode(); v != nullptr; v = v->succ()) {
    node_handles[v->index()] = v;
  }
}

Graph::Graph(size_t num_nodes) {
  for (size_t i = 0; i < num_nodes; i++) {
    graph.newNode();
  }
  initAttrs();
  initNodeHandles();
}

Graph::Graph(ogdf::Graph &graphToCopy) {
  graph = graphToCopy;
  initAttrs();
  initNodeHandles();
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
  std::vector<ogdf::edge> changed;

  for (auto row : update.values) {
    auto v = node_handles[std::get<0>(row)];
    auto w = node_handles[std::get<1>(row)];
    auto e = graph.searchEdge(v, w, false);
    if (e == nullptr) {
      if (std::get<2>(row) != 0.0f) {
        e = graph.newEdge(v, w);
        // Apparently OGDF initializes edge weights with 1.0 ...
        attrs.doubleWeight(e) = 0.0;
        for (auto o: observers) {
          o->onEdgeAdded(e);
        }
      } else {
        continue;
      }
    }
    attrs.doubleWeight(e) += std::get<2>(row);
    if (attrs.doubleWeight(e) <= 0.0) {
      for (auto o : observers) {
        o->onEdgeDeleted(e);
      }
      graph.delEdge(e);
      continue;
    } else {
      changed.push_back(e);
    }
  }

  // This is a workaround to an awful design decision of OGDF
  // (OGDF containers/iterators having a non-standard interface),
  // which in itself is only something we need to work around another bug in OGDF
  // (shrinking an Array with resize() results in a double free).
  ogdf::Array<ogdf::edge> ogdfIsTerrible((int) changed.size());
  for (int i = 0; i < (int) changed.size(); i++) { ogdfIsTerrible[i] = changed[i]; }

  for (auto o : observers) {
    o->onWeightChange(ogdfIsTerrible);
  }
}

void Graph::submitHeatUpdate(HeatUpdate &update) {
  ogdf::Array<ogdf::node> changed((int) update.values.size());
  int chg_idx = 0;

  for (auto row : update.values) {
    auto v = node_handles[std::get<0>(row)];
    attrs.weight(v) = std::get<1>(row);
    changed[chg_idx++] = v;
  }

  for (auto o : observers) {
    o->onHeatChange(changed);
  }
}

void Graph::recalculateLayout() {
  ogdf::EdgeArray<double> lengths(graph);
  for (auto e = graph.firstEdge(); e; e = e->succ()) {
    double w = attrs.doubleWeight(e);
    //double l = 1.0 / (w * w);
    double l = 1.0 / w;
    lengths[e] = l;
  }

  ogdf::FMMMLayout fmmm;
  fmmm.useHighLevelOptions(true);
  fmmm.unitEdgeLength(10.0);
  fmmm.newInitialPlacement(false);
  fmmm.qualityVersusSpeed(ogdf::FMMMOptions::QualityVsSpeed::NiceAndIncredibleSpeed);
  fmmm.call(attrs, lengths);

  ogdf::Array<ogdf::node> nodes;
  graph.allNodes(nodes);
  for (auto o : observers) {
    o->onLayoutChange(nodes);
  }
}

void Graph::adaptLayout() {
  // TODO stub
}

void Graph::serialize(std::ostream &stream) {
  ogdf::GraphIO::writeGDF(attrs, stream);
}

void Graph::deserialize(std::istream &stream) {
  ogdf::GraphIO::readGDF(attrs, graph, stream);
  initNodeHandles();

  for (auto o : observers) {
    o->onReload();
  }
}

NodeInfo Graph::queryNode(int index) {
  // TODO error handling
  ogdf::node v = node_handles[index];

  NodeInfo info;
  info.index = index;
  info.heat  = attrs.weight(v);
  info.x     = (float) attrs.x(v);
  info.y     = (float) attrs.y(v);
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
  info.weight = (float) attrs.doubleWeight(e);
  return info;
}

ogdf::edge Graph::getEdgeHandle(int index1, int index2) {
  auto v = node_handles[index1];
  auto w = node_handles[index2];
  return graph.searchEdge(v, w, false);
}

} // namespace graph
} // namespace satviz
