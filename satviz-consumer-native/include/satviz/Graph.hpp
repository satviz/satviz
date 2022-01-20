#ifndef SATVIZ_GRAPH_HPP_
#define SATVIZ_GRAPH_HPP_

#include <sstream>

namespace satviz {
namespace graph {

class GraphObserver;

struct NodeInfo {
};

struct EdgeInfo {
};

class WeightUpdate {
};

class HeatUpdate {
};

/**
 *
 */
class Graph {
public:
  Graph();

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
