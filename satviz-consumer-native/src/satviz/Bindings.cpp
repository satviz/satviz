#include <vector>
#include <string>
#include <memory>
#include <unordered_map>

#include <satviz/Graph.hpp>
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
  reinterpret_cast<Graph*>(graph)->recalculateLayout();
}

void satviz_adapt_layout(void *graph) {
  reinterpret_cast<Graph*>(graph)->adaptLayout();
}

const char *satviz_serialize(void *graph) {
  return reinterpret_cast<Graph*>(graph)->serialize().str().c_str();
}

void satviz_deserialize(void *graph, const char *str) {
  std::stringbuf buf { std::string { str } };
  reinterpret_cast<Graph*>(graph)->deserialize(buf);
}

void satviz_submit_weight_update(void *graph, CWeightUpdate *update) {
  std::vector<std::tuple<int, int, float>> values { update->n };
  for (size_t i = 0; i < update->n; i++) {
    values.emplace_back(update->index1[i], update->index2[i], update->weight[i]);
  }
  WeightUpdate realUpdate { values };
  reinterpret_cast<Graph*>(graph)->submitWeightUpdate(realUpdate);
}

void satviz_submit_heat_update(void *graph, CHeatUpdate *update) {
  std::vector<std::tuple<int, int>> values { update->n };
  for (size_t i = 0; i < update->n; i++) {
    values.emplace_back(update->index[i], update->heat[i] * 0xff);
  }
  HeatUpdate realUpdate { values };
  reinterpret_cast<Graph*>(graph)->submitHeatUpdate(realUpdate);
}

NodeInfo satviz_query_node(void *graph, int index) {
  return reinterpret_cast<Graph*>(graph)->queryNode(index);
}

EdgeInfo satviz_query_edge(void *graph, int index1, int index2) {
  return reinterpret_cast<Graph*>(graph)->queryEdge(index1, index2);
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

  return new VideoController { *reinterpret_cast<Graph*>(graph), display };
}

void satviz_release_video_controller(void *controller) {
  delete (VideoController*) controller;
}

int satviz_start_recording(void *controller, const char *filename, const char *encoder_name) {
  VideoEncoder *encoder;
  std::string enc_name_str { encoder_name };
  if (enc_name_str == "theora") {
    encoder = new TheoraEncoder;
  } else {
    return -1;
  }
  return reinterpret_cast<VideoController*>(controller)->startRecording(filename, encoder);
}

void satviz_stop_recording(void *controller) {
  reinterpret_cast<VideoController*>(controller)->stopRecording();
}

void satviz_resume_recording(void *controller) {
  reinterpret_cast<VideoController*>(controller)->resumeRecording();
}

void satviz_finish_recording(void *controller) {
  reinterpret_cast<VideoController*>(controller)->finishRecording();
}

}
