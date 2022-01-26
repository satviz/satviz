#include <satviz/VideoController.hpp>
#include <satviz/GlUtils.hpp>

namespace satviz {
namespace video {

VideoController::VideoController(graph::Graph *gr, Display *dpy)
  : graph(gr), display(dpy), camera(), wantToClose(false) {
  logGlDebugMessages();
  video::GraphRenderer::initializeResources();
  renderer = new GraphRenderer(graph);
  graph->addObserver(renderer);
}

VideoController::~VideoController() {
  delete renderer;
  video::GraphRenderer::terminateResources();
  delete display;
}

void VideoController::nextFrame() {
  sf::Event event;
  while (display->pollEvent(event)) {
    if (event.type == sf::Event::Closed)
      wantToClose = true;
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
