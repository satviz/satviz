#ifndef SATVIZ_BINDINGS_H
#define SATVIZ_BINDINGS_H

#ifdef __cplusplus
extern "C" {
#endif

#include <satviz/info.h>

typedef struct CWeightUpdate {
  unsigned int n;
  int *from;
  int *to;
  float *weight;
} CWeightUpdate;

typedef struct CHeatUpdate {
  unsigned int n;
  int *index;
  int *heat;
} CHeatUpdate;

// Graph
void *satviz_new_graph(size_t nodes);
void satviz_release_graph(void *graph);
void satviz_submit_weight_update(void *graph, CWeightUpdate *);
void satviz_submit_heat_update(void *graph, CHeatUpdate *);
void satviz_recalculate_layout(void *graph);
void satviz_adapt_layout(void *graph);
char *satviz_serialize(void *graph);
void satviz_deserialize(void *graph, const char *);
NodeInfo satviz_query_node(void *graph, int index);
EdgeInfo satviz_query_edge(void *graph, int index1, int index2);

// VideoController
void *satviz_new_video_controller(void *graph, int display_type);
void satviz_release_video_controller(void *controller);
int satviz_start_recording(void *controller, const char *filename, const char *encoder_name);
void satviz_stop_recording(void *controller);
void satviz_resume_recording(void *controller);
void satviz_finish_recording(void *controller);


#ifdef __cplusplus
}
#endif

#endif //SATVIZ_BINDINGS_H
