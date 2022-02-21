#include <satviz/VideoController.hpp>
#include <satviz/GlUtils.hpp>
#include <cassert>

#include <satviz/TheoraEncoder.hpp>

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
  delete video_encoder;
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
      graph::WeightUpdate wu;
      wu.values.push_back(std::make_tuple(node1->index(), node2->index(), (float) rand() / (float) RAND_MAX));
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
