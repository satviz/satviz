#ifndef SATVIZ_BINDINGS_H
#define SATVIZ_BINDINGS_H

#ifdef __cplusplus
extern "C" {
#endif

typedef struct CNodeInfo {
  int index;
  int heat;
  float x;
  float y;
} CNodeInfo;

typedef struct CEdgeInfo {
  int index1;
  int index2;
  float weight;
} CEdgeInfo;

typedef struct CWeightUpdate {
  size_t n;
  int *from;
  int *to;
  float *weight;
} CWeightUpdate;

typedef struct CHeatUpdate {
  size_t n;
  int *index;
  int *heat;
} CHeatUpdate;

void *satviz_new_graph(size_t nodes);
void satviz_release_graph(void *);
void satviz_submit_weight_update(void *, CWeightUpdate *);
void satviz_submit_heat_update(void *, CHeatUpdate *);
void satviz_recalculate_layout(void *);
void satviz_adapt_layout(void *);
char *satviz_serialize(void *);
void satviz_deserialize(void *, const char *);
CNodeInfo satviz_query_node(void *, int index);
CEdgeInfo satviz_query_edge(void *, int index1, int index2);


#ifdef __cplusplus
}
#endif

#endif //SATVIZ_BINDINGS_H
