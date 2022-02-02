#ifndef SATVIZ_Y_CB_CR_HPP_
#define SATVIZ_Y_CB_CR_HPP_

namespace satviz {
namespace video {

/*
 * Source of conversion formulas:
 * https://sistenix.com/rgb2ycbcr.html
 */

inline int rgbToY(int R, int G, int B) {
  int Y;
  Y = 16 + (((R<<6) + (R<<1) + (G<<7) + G + (B<<4) + (B<<3) + B) >> 8);
  return Y;
}

inline int rgbToCb(int R, int G, int B) {
  int Cb;
  Cb = 128 + ((-((R<<5) + (R<<2) + (R<<1)) - ((G<<6) + (G<<3) + (G<<1)) + (B<<7) - (B<<4)) >> 8);
  return Cb;
}

inline int rgbToCr(int R, int G, int B) {
  int Cr;
  Cr = 128 + (((R<<7) - (R<<4) - ((G<<6) + (G<<5) - (G<<1)) - ((B << 4) + (B << 1))) >> 8);
  return Cr;
}

} // namespace video
} // namespace satviz

#endif //SATVIZ_Y_CB_CR_HPP_
