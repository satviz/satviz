#include <satviz/VideoController.hpp>

namespace satviz {
namespace video {

VideoController::VideoController(graph::Graph *gr, Display *dpy)
  : graph(gr), display(dpy), wantToClose(false) {
}

VideoController::~VideoController() {
  delete display;
}

void VideoController::nextFrame() {
  sf::Event event;
  while (display->pollEvent(event)) {
    if (event.type == sf::Event::Closed)
      wantToClose = true;
  }
  display->startFrame();
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
