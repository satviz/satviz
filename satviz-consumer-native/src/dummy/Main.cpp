#include <satviz/Graph.hpp>
#include <satviz/OnscreenDisplay.hpp>
#include <satviz/VideoController.hpp>

#include <ogdf/basic/graph_generators.h>
#include <ogdf/layered/DfsAcyclicSubgraph.h>

using namespace ::satviz;

int main() {
  ogdf::Graph ogdfGraph;
  ogdf::randomSimpleGraph(ogdfGraph, 100, 200);
  ogdf::DfsAcyclicSubgraph das;
  das.callAndReverse(ogdfGraph);

  graph::Graph graph(ogdfGraph);
  video::Display *display = new video::OnscreenDisplay(640, 480);
  video::VideoController controller(graph, display);
  graph.recalculateLayout();
  while (!controller.wantToClose) {
    controller.nextFrame();
  }
  return 0;
}