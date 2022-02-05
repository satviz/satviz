#ifndef SATVIZ_INFO_H
#define SATVIZ_INFO_H

#ifdef __cplusplus
extern "C" {
#endif

typedef struct NodeInfo {
  int index;
  int heat;
  float x;
  float y;
} NodeInfo;

typedef struct EdgeInfo {
  int index1;
  int index2;
  float weight;
} EdgeInfo;

#ifdef __cplusplus
}
#endif

#endif //SATVIZ_INFO_H
