#include <vector>
#include <string>
#include <sstream>
#include <cstdlib>
#include <cstring>

#include <satviz/Graph.hpp>
#include <satviz/GraphContraction.hpp>
#include <satviz/Display.hpp>
#include <satviz/OnscreenDisplay.hpp>
#include <satviz/OffscreenDisplay.hpp>
#include <satviz/VideoController.hpp>
#include <satviz/VideoEncoder.hpp>
#include <satviz/TheoraEncoder.hpp>

using namespace satviz::graph;
using namespace satviz::video;

extern "C" {

#include <satviz/Bindings.h>

void *satviz_new_graph(unsigned long nodes) {
  return new Graph { nodes };
}

void satviz_release_graph(void *graph) {
  delete (Graph*) graph;
}

void satviz_recalculate_layout(void *graph) {
  static_cast<Graph*>(graph)->recalculateLayout();
}

void satviz_adapt_layout(void *graph) {
  static_cast<Graph*>(graph)->adaptLayout();
}

SerializedData satviz_serialize(void *graph) {
  std::stringstream stream {};
  static_cast<Graph*>(graph)->serialize(stream);
  auto size = stream.tellp();
  char *buf = (char*) malloc(static_cast<size_t>(size));
  memcpy(buf, stream.str().c_str(), size);
  return SerializedData { buf, static_cast<unsigned long>(size) };
}

void satviz_deserialize(void *graph, const char *data, unsigned long n) {
  std::stringstream stream { std::string { data, n } };
  static_cast<Graph*>(graph)->deserialize(stream);
}

void satviz_submit_weight_update(void *graph, CWeightUpdate *update) {
  WeightUpdate realUpdate(update->n);
  for (size_t i = 0; i < update->n; i++) {
    realUpdate.values.emplace_back(update->index1[i], update->index2[i], update->weight[i]);
  }
  static_cast<Graph*>(graph)->submitWeightUpdate(realUpdate);
}

void satviz_submit_heat_update(void *graph, CHeatUpdate *update) {
  HeatUpdate realUpdate(update->n);
  for (size_t i = 0; i < update->n; i++) {
    realUpdate.values.emplace_back(update->index[i], update->heat[i] * 0xff);
  }
  static_cast<Graph*>(graph)->submitHeatUpdate(realUpdate);
}

int satviz_num_nodes(void *graph) {
  return static_cast<Graph*>(graph)->numNodes();
}

NodeInfo satviz_query_node(void *graph, int index) {
  return static_cast<Graph*>(graph)->queryNode(index);
}

EdgeInfo satviz_query_edge(void *graph, int index1, int index2) {
  return static_cast<Graph*>(graph)->queryEdge(index1, index2);
}

int satviz_compute_contraction(void *graph, int iterations, int *mapping) {
  return computeContraction(*static_cast<Graph*>(graph), iterations, mapping);
}

void *satviz_new_video_controller(void *graph, int display_type, int width, int height) {
  Display *display;
  switch (display_type) {
    case 0:
      display = new OffscreenDisplay { width, height };
      break;
    case 1:
      display = new OnscreenDisplay { width, height };
      break;
    default:
      return nullptr;
  }

  return new VideoController { *static_cast<Graph*>(graph), display };
}

void satviz_release_video_controller(void *controller) {
  delete (VideoController*) controller;
}

void satviz_apply_theme(void *controller, const Theme *theme) {
  reinterpret_cast<VideoController*>(controller)->applyTheme(*theme);
}

int satviz_start_recording(void *controller, const char *filename, const char *encoder_name) {
  VideoEncoder *encoder;
  std::string enc_name_str { encoder_name };
  if (enc_name_str == "theora") {
    encoder = new TheoraEncoder;
  } else {
    return -1;
  }
  return static_cast<VideoController*>(controller)->startRecording(filename, encoder);
}

void satviz_stop_recording(void *controller) {
  static_cast<VideoController*>(controller)->stopRecording();
}

void satviz_resume_recording(void *controller) {
  static_cast<VideoController*>(controller)->resumeRecording();
}

void satviz_finish_recording(void *controller) {
  static_cast<VideoController*>(controller)->finishRecording();
}

void satviz_reset_camera(void *controller) {
  static_cast<VideoController*>(controller)->resetCamera();
}

void satviz_next_frame(void *controller) {
  static_cast<VideoController*>(controller)->nextFrame();
}

}
