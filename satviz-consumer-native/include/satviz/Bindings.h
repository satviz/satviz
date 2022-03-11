#ifndef SATVIZ_BINDINGS_H
#define SATVIZ_BINDINGS_H

#ifdef __cplusplus
extern "C" {
#endif

#include <satviz/info.h>
#include <satviz/Theme.h>

typedef struct CWeightUpdate {
  int *index1;
  int *index2;
  float *weight;
  unsigned int n;
} CWeightUpdate;

typedef struct CHeatUpdate {
  int *index;
  float *heat;
  unsigned int n;
} CHeatUpdate;

typedef struct SerializedData {
  const char *data;
  unsigned long n;
} SerializedData;

// Graph
void *satviz_new_graph(unsigned long nodes);
void satviz_release_graph(void *graph);
void satviz_submit_weight_update(void *graph, CWeightUpdate *);
void satviz_submit_heat_update(void *graph, CHeatUpdate *);
void satviz_recalculate_layout(void *graph);
void satviz_adapt_layout(void *graph);
SerializedData satviz_serialize(void *graph);
void satviz_deserialize(void *graph, const char *data, unsigned long n);
NodeInfo satviz_query_node(void *graph, int index);
EdgeInfo satviz_query_edge(void *graph, int index1, int index2);

// VideoController
void *satviz_new_video_controller(void *graph, int display_type, int width, int height);
void satviz_release_video_controller(void *controller);
void satviz_apply_theme(void *controller, const Theme *theme);
int satviz_start_recording(void *controller, const char *filename, const char *encoder_name);
void satviz_stop_recording(void *controller);
void satviz_resume_recording(void *controller);
void satviz_finish_recording(void *controller);
void satviz_reset_camera(void *controller);
void satviz_next_frame(void *controller);

#ifdef __cplusplus
}
#endif

#endif //SATVIZ_BINDINGS_H
