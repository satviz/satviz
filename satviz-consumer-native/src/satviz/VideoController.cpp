#include <satviz/VideoController.hpp>
#include <satviz/GlUtils.hpp>
#include <cassert>

#include <satviz/TheoraEncoder.hpp>

namespace satviz {
namespace video {

VideoController::VideoController(graph::Graph &gr, Display *dpy)
  : graph(gr), display(dpy), camera(), wantToClose(false) {
  //logGlDebugMessages();
  video::GraphRenderer::initializeResources();
  renderer = new GraphRenderer(graph);
  graph.addObserver(renderer);
  renderer->onReload();
  camera.update(display->getWidth(), display->getHeight());
}

VideoController::~VideoController() {
  delete video_encoder;
  delete renderer;
  video::GraphRenderer::terminateResources();
  delete display;
}

void VideoController::resetCamera() {
  ogdf::DRect box = graph.getOgdfAttrs().boundingBox();
  camera.focusOnBox(
      box.p1().m_x, box.p1().m_y,
      box.p2().m_x, box.p2().m_y);
}

void VideoController::processEvent(sf::Event &event) {
  if (event.type == sf::Event::Closed) {
    wantToClose = true;
  }
  if (event.type == sf::Event::LostFocus) {
    mouse_grabbed = false;
  }
  if (event.type == sf::Event::MouseButtonPressed && event.mouseButton.button == sf::Mouse::Left) {
    mouse_grabbed = true;
    mouse_x = event.mouseButton.x;
    mouse_y = event.mouseButton.y;
  }
  if (event.type == sf::Event::MouseButtonReleased && event.mouseButton.button == sf::Mouse::Left) {
    mouse_grabbed = false;
  }
  if (event.type == sf::Event::MouseMoved && mouse_grabbed) {
    camera.drag(mouse_x, mouse_y, event.mouseMove.x, event.mouseMove.y);
    mouse_x = event.mouseMove.x;
    mouse_y = event.mouseMove.y;
  }
  if (event.type == sf::Event::MouseWheelScrolled) {
    double factor = 1.0;
    if (event.mouseWheelScroll.delta < 0.0f) {
      factor = 1.0 / 1.3;
    } else {
      factor = 1.0 * 1.3;
    }
    camera.zoom(event.mouseWheelScroll.x, event.mouseWheelScroll.y, factor);
  }
#if 0
  if (event.type == sf::Event::KeyPressed) {
    if (event.key.code == sf::Keyboard::Space) {
      resetCamera();
    }
  }
#endif
}

void VideoController::nextFrame() {
  sf::Event event;
  while (display->pollEvent(event)) {
    processEvent(event);
  }
  camera.update(display->getWidth(), display->getHeight());
  display->startFrame();
  renderer->draw(camera, display->getWidth(), display->getHeight());
  if (recording_state == REC_ON || recording_state == REC_WINDUP) {
    display->transferCurrentFrame();
  }
  if (recording_state == REC_ON || recording_state == REC_WINDDOWN) {
    VideoFrame frame = display->grabPreviousFrame(video_encoder->getGeometry());
    video_encoder->submitFrame(frame, recording_state == REC_WINDDOWN);
  }
  if (recording_state == REC_WINDUP) {
    recording_state = REC_ON;
    display->lockSize(true);
  }
  if (recording_state == REC_WINDDOWN) {
    delete video_encoder;
    video_encoder = nullptr;
    recording_state = REC_OFF;
    display->lockSize(false);
  }
  display->endFrame();
}

bool VideoController::startRecording(const char *filename, VideoEncoder *enc) {
  assert(recording_state == REC_OFF);
  if (!enc->startRecording(filename, display->getWidth(), display->getHeight())) {
    return false;
  }
  std::cout << "STARTED RECORDING" << std::endl;
  recording_state = REC_WINDUP;
  video_encoder = enc;
  return true;
}

void VideoController::stopRecording() {
  assert(recording_state == REC_ON);
  recording_state = REC_PAUSED;
}

void VideoController::resumeRecording() {
  assert(recording_state == REC_PAUSED);
  recording_state = REC_ON;
}

void VideoController::finishRecording() {
  assert(recording_state == REC_ON);
  std::cout << "FINISHED RECORDING" << std::endl;
  recording_state = REC_WINDDOWN;
  display->lockSize(false);
}

} // namespace video
} // namespace satviz
