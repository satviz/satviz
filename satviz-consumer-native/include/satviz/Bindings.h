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

#ifdef __cplusplus
}
#endif

#endif //SATVIZ_BINDINGS_H
