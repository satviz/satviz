#ifndef SATVIZ_THEME_H_
#define SATVIZ_THEME_H_

#ifdef __cplusplus
extern "C" {
#endif

typedef struct Theme {
  // All colors are in RGB order, sRGB color space
  //float bgColor  [3] = {  };
  float coldColor[3] = { 0.0f, 0.0f, 1.0f };
  float hotColor [3] = { 1.0f, 0.0f, 0.0f };
  float edgeColor[3] = { 1.0f, 1.0f, 1.0f };
  float nodeSize     = 10.0f;
} Theme;

#ifdef __cplusplus
}
#endif

#endif // SATVIZ_THEME_H_
