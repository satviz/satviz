#include <satviz/Graph.hpp>
#include <satviz/OnscreenDisplay.hpp>
#include <satviz/VideoController.hpp>

using namespace ::satviz;

int main() {
  graph::Graph graph(10);
  video::Display *display = new video::OnscreenDisplay(640, 480);
  video::VideoController controller(&graph, display);
  graph.recalculateLayout();
  while (!controller.wantToClose) {
    controller.nextFrame();
  }
  return 0;
}