#include <sstream>
#include <vector>

#include <satviz/Graph.hpp>


using namespace satviz::graph;

extern "C" {

#include <satviz/Bindings.h>

void *satviz_new_graph(size_t nodes) {
  return new Graph { nodes };
}

void satviz_release_graph(void *graph) {
  delete (Graph*) graph;
}

void satviz_recalculate_layout(void *graph) {
  reinterpret_cast<Graph*>(graph)->recalculateLayout();
}

void satviz_adapt_layout(void *graph) {
  // TODO not implemented yet
  (void) graph;
  //reinterpret_cast<Graph*>(graph)->adaptLayout();
}

char *satviz_serialize(void *graph) {
  // TODO not implemented yet
  (void) graph;
  return NULL;
  //return reinterpret_cast<Graph*>(graph)->serialize().str().c_str();
}

void satviz_deserialize(void *graph, const char *str) {
  std::stringbuf buf { std::string { str } };
  // TODO not implemented yet
  (void) graph;
  (void) buf;
  //reinterpret_cast<Graph*>(graph)->deserialize(buf);
}

void satviz_submit_weight_update(void *graph, CWeightUpdate *update) {
  std::vector<std::tuple<int, int, float>> values { update->n };
  for (size_t i = 0; i < update->n; i++) {
    values.push_back(std::make_tuple(update->from[i], update->to[i], update->weight[i]));
  }
  WeightUpdate realUpdate { values };
  // TODO not implemented yet
  (void) graph;
  (void) realUpdate;
  //reinterpret_cast<Graph*>(graph)->submitWeightUpdate(realUpdate);
}

void satviz_submit_heat_update(void *graph, CHeatUpdate *update) {
  std::vector<std::tuple<int, int>> values { update->n };
  for (size_t i = 0; i < update->n; i++) {
    values.push_back(std::make_tuple(update->index[i], update->heat[i]));
  }
  HeatUpdate realUpdate { values };
  // TODO not implemented yet
  (void) graph;
  (void) realUpdate;
  //reinterpret_cast<Graph*>(graph)->submitHeatUpdate(realUpdate);
}

CNodeInfo satviz_query_node(void *graph, int index) {
  (void) graph;
  (void) index;
  // TODO not implemented yet
  //NodeInfo node = reinterpret_cast<Graph*>(graph)->queryNode(index);
  //return CNodeInfo { node.index, node.heat, node.x, node.y }
}

}
