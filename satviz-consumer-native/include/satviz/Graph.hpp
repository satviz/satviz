#ifndef SATVIZ_GRAPH_HPP_
#define SATVIZ_GRAPH_HPP_

#include <vector>
#include <tuple>
#include <iostream>

#include <satviz/info.h>
#include <ogdf/basic/Graph.h>
#include <ogdf/basic/GraphAttributes.h>

namespace satviz {
namespace graph {

class GraphObserver;

struct WeightUpdate {
  std::vector<std::tuple<int, int, float> > values;

  WeightUpdate() = default;
  explicit WeightUpdate(size_t n) {
    values.reserve(n);
  }
};

struct HeatUpdate {
  std::vector<std::tuple<int, int> > values;

  HeatUpdate() = default;
  explicit HeatUpdate(size_t n) {
    values.reserve(n);
  }
};

/**
 * Data storage for the visualization-generated graph.
 */
class Graph {
private:
  ogdf::Graph graph;
  ogdf::GraphAttributes attrs;
  std::vector<ogdf::node> node_handles;
  std::vector<GraphObserver*> observers;

  void initAttrs();
  void initNodeHandles();

public:
  /**
   * The preferred constructor for Graph objects.
   * @param num_nodes the (fixed) number of nodes that the graph should have
   */
  Graph(size_t num_nodes);
  /**
   * This variant of the constructor only exists to aid
   * debugging & testing. It should not be used directly.
   */
  Graph(ogdf::Graph &graphToCopy);
  ~Graph() = default;

  /**
   * Getter for the number of nodes
   * @return the number of nodes
   */
  int numNodes() { return graph.numberOfNodes(); }
  /**
   * Getter for the number of edges
   * @return the number of edges
   */
  int numEdges() { return graph.numberOfEdges(); }

  /**
   * Access the underlying OGDF graph
   * @return a reference to the OGDF graph
   */
  ogdf::Graph &getOgdfGraph() { return graph; }
  /**
   * Access the underlying OGDF graph atttributes
   * @return a reference to the OGDF graph attributes
   */
  ogdf::GraphAttributes &getOgdfAttrs() { return attrs; }

  /**
   * Register an observer
   * @param o the observer
   */
  void addObserver(GraphObserver *o);
  /**
   * De-register an observer
   * @param o the observer
   */
  void removeObserver(GraphObserver *o);

  /**
   * bulk-update the edge weights in the graph
   * @param update the weight update
   */
  void submitWeightUpdate(WeightUpdate &update);
  /**
   * bulk-update the node heat values in the graph
   * @param update the heat update
   */
  void submitHeatUpdate(HeatUpdate &update);

  /**
   * Completely recalculate the spacial positioning of the nodes on the screen.
   */
  void recalculateLayout();
  void adaptLayout();

  /**
   * Serialize the graph's contents into an output stream.
   * @param stream the output stream
   */
  void serialize(std::ostream &stream);
  /**
   * Deserialize the graph's contents from an input stream.
   * @param stream the input stream
   */
  void deserialize(std::istream &stream);

  /**
   * Build a bundle of all relevant information pertaining to one specific node in the graph.
   * @param index the index of the node in the graph
   * @return a NodeInfo structure
   */
  NodeInfo queryNode(int index);
  /**
   * Build a bundle of all relevant information pertaining to one specific edge in the graph.
   * @param index1 the index of one end of the edge
   * @param index1 the index of the other end of the edge
   * @return an EdgeInfo structure
   */
  EdgeInfo queryEdge(int index1, int index2);

  /**
   * Query the x coordinate of a node using an opaque handle.
   * @param v the node handle
   * @return the x coordinate value
   */
  double getX(ogdf::node v) { return attrs.x(v); }
  /**
   * Query the y coordinate of a node using an opaque handle.
   * @param v the node handle
   * @return the y coordinate value
   */
  double getY(ogdf::node v) { return attrs.y(v); }

  ogdf::node getNodeHandle(int index) { return node_handles[index]; }
  ogdf::edge getEdgeHandle(int index1, int index2);
};

std::vector<int> computeContraction(Graph &graph, int iterations);

} // namespace graph
} // namespace satviz

#endif
