#ifndef SATVIZ_GRAPH_OBSERVER_HPP_
#define SATVIZ_GRAPH_OBSERVER_HPP_

#include <satviz/Graph.hpp>

namespace satviz {
namespace graph {

/**
 *
 */
class GraphObserver {
protected:
  Graph *my_graph;

  GraphObserver(Graph *gr) : my_graph(gr) {}

public:
  virtual void onWeightUpdate(WeightUpdate &update) { (void) update; }
  virtual void onHeatUpdate(HeatUpdate &update) { (void) update; }
  virtual void onLayoutChange(ogdf::Array<ogdf::node> &changed) { (void) changed; }
  virtual void onEdgeAdded(ogdf::edge e) { (void) e; }
  virtual void onEdgeDeleted(ogdf::edge e) { (void) e; }
  virtual void onReload() {}
};

} // namespace graph
} // namespace satviz

#endif
