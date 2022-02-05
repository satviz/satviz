#include <satviz/VideoController.hpp>
#include <satviz/GlUtils.hpp>
#include <cassert>

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

void VideoController::processEvent(sf::Event &event) {
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
    if (event.key.code == sf::Keyboard::Left || event.key.code == sf::Keyboard::A) {
      camera.setX(camera.getX() - 0.2f / camera.getZoom());
    }
    if (event.key.code == sf::Keyboard::Right || event.key.code == sf::Keyboard::D) {
      camera.setX(camera.getX() + 0.2f / camera.getZoom());
    }
    if (event.key.code == sf::Keyboard::Down || event.key.code == sf::Keyboard::S) {
      camera.setY(camera.getY() - 0.2f / camera.getZoom());
    }
    if (event.key.code == sf::Keyboard::Up || event.key.code == sf::Keyboard::W) {
      camera.setY(camera.getY() + 0.2f / camera.getZoom());
    }
    if (event.key.code == sf::Keyboard::K) {
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

void VideoController::nextFrame() {
  sf::Event event;
  while (display->pollEvent(event)) {
    processEvent(event);
  }
  display->startFrame();
  renderer->draw(camera, display->getWidth(), display->getHeight());
  if (recording_state == REC_ON || recording_state == REC_WINDUP) {
    display->transferFrame();
  }
  if (recording_state == REC_ON || recording_state == REC_WINDDOWN) {
    VideoFrame frame = display->grabFrame();
  }
  if (recording_state == REC_WINDUP) {
    recording_state = REC_ON;
  }
  if (recording_state == REC_WINDDOWN) {
    recording_state = REC_OFF;
  }
  display->endFrame();
}

bool VideoController::startRecording(const char *filename, VideoEncoder *enc) {
  (void) filename;
  (void) enc;
  assert(recording_state == REC_OFF);
  recording_state = REC_WINDUP;
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
  recording_state = REC_WINDDOWN;
}

} // namespace video
} // namespace satviz
