#ifndef SATVIZ_THEME_H_
#define SATVIZ_THEME_H_

#ifdef __cplusplus
extern "C" {
#endif

typedef struct Theme {
  // All colors are in RGB order, sRGB color space
  //float bgColor  [3];
  float coldColor[3];
  float hotColor [3];
  float edgeColor[3];
  float nodeSize;
} Theme;

#ifdef __cplusplus
}
#endif

#endif // SATVIZ_THEME_H_
