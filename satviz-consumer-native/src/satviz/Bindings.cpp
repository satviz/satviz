#include <sstream>

#include <satviz/Graph.hpp>


using satviz::graph::Graph;

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

}
