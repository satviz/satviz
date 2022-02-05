#ifndef SATVIZ_BINDINGS_H
#define SATVIZ_BINDINGS_H

#ifdef __cplusplus
extern "C" {
#endif

typedef struct CNodeInfo {
  int index;
  int head;
  float x;
  float y;
} CNodeInfo;

typedef struct CEdgeInfo {
  int index1;
  int index2;
  float weight;
} CEdgeInfo;

typedef struct CWeightUpdate {
  int *from;
  int *to;
  float *weight;
} CWeightUpdate;

typedef struct CHeatUpdate {
  int *index;
  int *heat;
} CHeatUpdate;

typedef struct CGraph CGraph;

CGraph *satviz_new_graph(int nodes);
void satviz_release_graph(CGraph *);
void satviz_submit_weight_update(CGraph *, CWeightUpdate *);
void satviz_submit_heat_update(CGraph *, CHeatUpdate *);
void satviz_recalculate_layout(CGraph *);
void satviz_adapt_layout(CGraph *);
char *satviz_serialize(CGraph *);
void satviz_deserialize(CGraph *, char *);
CNodeInfo satviz_query_node(CGraph *, int index);
CEdgeInfo satviz_query_edge(CGraph *, int index1, int index2);


#ifdef __cplusplus
}
#endif

#endif //SATVIZ_BINDINGS_H
