#include <satviz/Graph.hpp>

using satviz::graph::Graph;

extern "C" {

#include <satviz/Bindings.h>

CGraph *satviz_new_graph(unsigned int nodes) {
  return (CGraph*) new Graph{nodes};
}

void satviz_release_graph(CGraph *graph) {
  delete (Graph*) graph;
}

}
