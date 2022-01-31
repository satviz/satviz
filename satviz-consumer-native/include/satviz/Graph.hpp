#ifndef SATVIZ_GRAPH_HPP_
#define SATVIZ_GRAPH_HPP_

#include <vector>
#include <tuple>
#include <sstream>

#include <ogdf/basic/Graph.h>
#include <ogdf/basic/GraphAttributes.h>

namespace satviz {
namespace graph {

class GraphObserver;

struct NodeInfo {
  int index;
  int heat;
  float x;
  float y;
};

struct EdgeInfo {
  int index1;
  int index2;
  float weight;
};

struct WeightUpdate {
  std::vector<std::tuple<int, int, float> > values;
};

struct HeatUpdate {
  std::vector<std::tuple<int, int> > values;
};

/**
 *
 */
class Graph {
private:
  ogdf::Graph graph;
  ogdf::GraphAttributes attrs;

public:
  ogdf::NodeArray<float> &nodeX;
  ogdf::NodeArray<float> &nodeY;

  Graph();

  ogdf::Graph getOgdfGraph() { return graph; }
  ogdf::GraphAttributes getOgdfAttrs() { return attrs; }

  void addObserver(GraphObserver *o);

  void submitWeightUpdate(WeightUpdate &update);
  void submitHeatUpdate(HeatUpdate &update);

  void recalculateLayout();
  void adaptLayout();

  std::stringbuf serialize();
  void deserialize(std::stringbuf &buf);

  NodeInfo queryNode(int index);
  EdgeInfo queryEdge(int index1, int index2);
};

} // namespace graph
} // namespace satviz

#endif
