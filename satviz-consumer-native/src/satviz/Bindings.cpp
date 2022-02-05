#include <satviz/Graph.hpp>

using satviz::graph::Graph;

extern "C" {

#include <satviz/Bindings.h>

void *satviz_new_graph(unsigned int nodes) {
  return new Graph{nodes};
}

void satviz_release_graph(void *graph) {
  delete (Graph*) graph;
}

}
