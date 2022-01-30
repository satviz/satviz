#include <satviz/VideoController.hpp>
#include <satviz/GlUtils.hpp>

namespace satviz {
namespace video {

VideoController::VideoController(graph::Graph &gr, Display *dpy)
  : graph(gr), display(dpy), camera(), wantToClose(false) {
  logGlDebugMessages();
  video::GraphRenderer::initializeResources();
  renderer = new GraphRenderer(graph);
  graph.addObserver(renderer);
  renderer->onReload();
}

VideoController::~VideoController() {
  delete renderer;
  video::GraphRenderer::terminateResources();
  delete display;
}

void VideoController::nextFrame() {
  sf::Event event;
  while (display->pollEvent(event)) {
    if (event.type == sf::Event::Closed) {
      wantToClose = true;
    }
    if (event.type == sf::Event::MouseWheelScrolled) {
      float factor = 1.0f;
      if (event.mouseWheelScroll.delta < 0.0f) {
        factor = 1.0f / 1.5f;
      } else {
        factor = 1.0f * 1.5f;
      }
      camera.setZoom(camera.getZoom() * factor);
    }
    if (event.type == sf::Event::KeyPressed) {
      if (event.key.code == sf::Keyboard::D) {
        ogdf::Graph &og = graph.getOgdfGraph();
        ogdf::node node1 = og.chooseNode();
        ogdf::node node2 = og.chooseNode();
        ogdf::edge edge = og.searchEdge(node1, node2, false);
        if (edge == nullptr) {
          ogdf::edge edge = og.newEdge(node1, node2);
          renderer->onEdgeAdded(edge);
        } else {
          renderer->onEdgeDeleted(edge);
          og.delEdge(edge);
        }
      }
    }
  }
  display->startFrame();
  renderer->draw(camera, display->getWidth(), display->getHeight());
  display->displayFrame();
}

bool VideoController::startRecording(const char *filename, VideoEncoder *enc) {
  (void) filename;
  (void) enc;
  return false;
}

void VideoController::stopRecording() {
}

void VideoController::resumeRecording() {
}

void VideoController::finishRecording() {
}

} // namespace video
} // namespace satviz
