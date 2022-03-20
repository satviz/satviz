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
}

VideoController::~VideoController() {
  delete video_encoder;
  delete renderer;
  video::GraphRenderer::terminateResources();
  delete display;
}

void VideoController::resetCamera() {
  ogdf::DRect box = graph.getOgdfAttrs().boundingBox();
  double cx = 0.5 * (box.p1().m_x + box.p2().m_x);
  double cy = 0.5 * (box.p1().m_y + box.p2().m_y);
  camera.setX((float) cx);
  camera.setY((float) cy);
  camera.zoomToFit((float) box.width(), (float) box.height(), display->getWidth(), display->getHeight());
}

void VideoController::processEvent(sf::Event &event) {
  if (event.type == sf::Event::Closed) {
    wantToClose = true;
  }
  if (event.type == sf::Event::MouseWheelScrolled) {
    float factor = 1.0f;
    if (event.mouseWheelScroll.delta < 0.0f) {
      factor = 1.0f / 1.3f;
    } else {
      factor = 1.0f * 1.3f;
    }
    camera.setZoom(camera.getZoom() * factor);
  }
  if (event.type == sf::Event::KeyPressed) {
    const float SPEED = 200.0f;
    if (event.key.code == sf::Keyboard::Left || event.key.code == sf::Keyboard::A) {
      camera.setX(camera.getX() - SPEED / camera.getZoom());
    }
    if (event.key.code == sf::Keyboard::Right || event.key.code == sf::Keyboard::D) {
      camera.setX(camera.getX() + SPEED / camera.getZoom());
    }
    if (event.key.code == sf::Keyboard::Down || event.key.code == sf::Keyboard::S) {
      camera.setY(camera.getY() - SPEED / camera.getZoom());
    }
    if (event.key.code == sf::Keyboard::Up || event.key.code == sf::Keyboard::W) {
      camera.setY(camera.getY() + SPEED / camera.getZoom());
    }
    if (event.key.code == sf::Keyboard::Space) {
      resetCamera();
    }
#if 0
    if (event.key.code == sf::Keyboard::K) {
      ogdf::Graph &og = graph.getOgdfGraph();
      ogdf::node node1 = og.chooseNode();
      ogdf::node node2 = og.chooseNode();
      ogdf::edge edge = og.searchEdge(node1, node2, false);
      double old_weight = 0.0;
      if (edge) {
        old_weight = graph.getOgdfAttrs().doubleWeight(edge);
      }
      double new_weight = (double) rand() / (double) RAND_MAX;
      graph::WeightUpdate wu;
      wu.values.push_back(std::make_tuple(node1->index(), node2->index(), new_weight - old_weight));
      graph.submitWeightUpdate(wu);
    }
    if (event.key.code == sf::Keyboard::R) {
      switch (recording_state) {
        case REC_OFF:
          startRecording("temp.ogv", new TheoraEncoder);
          break;
        case REC_ON:
          finishRecording();
          break;
        default:
          break;
      }
    }
    if (event.key.code == sf::Keyboard::L) {
      graph.recalculateLayout();
    }
#endif
  }
}

void VideoController::nextFrame() {
  sf::Event event;
  while (display->pollEvent(event)) {
    processEvent(event);
  }
  camera.update();
  display->startFrame();
  renderer->clearScreen();
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
