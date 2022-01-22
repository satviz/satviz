#ifndef SATVIZ_GRAPH_OBSERVER_HPP_
#define SATVIZ_GRAPH_OBSERVER_HPP_

#include <satviz/Graph.hpp>

namespace satviz {
namespace graph {

/**
 *
 */
class GraphObserver {
private:
  Graph *my_graph;

protected:
  GraphObserver(Graph *gr) : my_graph(gr) {}

public:
  virtual void onWeightUpdate(WeightUpdate &update) { (void) update; }
  virtual void onHeatUpdate(HeatUpdate &update) { (void) update; }
  virtual void onLayoutChange() {}
  virtual void onLayoutChange(std::vector<int> changed) { (void) changed; }
  virtual void onReload() {}
};

} // namespace graph
} // namespace satviz

#endif
