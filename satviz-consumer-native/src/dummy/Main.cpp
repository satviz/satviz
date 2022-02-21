#include <satviz/Graph.hpp>
#include <satviz/OnscreenDisplay.hpp>
#include <satviz/VideoController.hpp>

#include <ogdf/basic/graph_generators.h>
#include <ogdf/layered/DfsAcyclicSubgraph.h>

#include <cstdlib>

using namespace ::satviz;

int main() {
  ogdf::Graph ogdfGraph;
  ogdf::randomSimpleGraph(ogdfGraph, 100, 200);
  ogdf::DfsAcyclicSubgraph das;
  das.callAndReverse(ogdfGraph);

  graph::Graph graph(ogdfGraph);
  ogdf::Graph &og = graph.getOgdfGraph();

  graph::WeightUpdate wu;
  for (ogdf::edge e = og.firstEdge(); e; e = e->succ()) {
    wu.values.push_back(std::make_tuple(e->source()->index(), e->target()->index(), (float) rand() / (float) RAND_MAX));
  }

  video::Display *display = new video::OnscreenDisplay(639, 469);
  video::VideoController controller(graph, display);
  graph.recalculateLayout();
  graph.submitWeightUpdate(wu);
  while (!controller.wantToClose) {
    controller.nextFrame();
  }
  return 0;
}