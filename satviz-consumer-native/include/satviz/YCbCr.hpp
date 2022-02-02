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
  Y   = (R<<6) + (R<<1);
  Y  += (G<<7) + G;
  Y  += (B<<4) + (B<<3) + B;
  Y >>= 8;
  Y  += 16;
  return Y;
}

inline int rgbToCb(int R, int G, int B) {
  int Cb = 0;
  Cb  -= (R<<5) + (R<<2) + (R<<1);
  Cb  -= (G<<6) + (G<<3) + (G<<1);
  Cb  += (B<<7) - (B<<4);
  Cb >>= 8;
  Cb  += 128;
  return Cb;
}

inline int rgbToCr(int R, int G, int B) {
  int Cr;
  Cr   = (R<<7) - (R<<4);
  Cr  -= (G<<6) + (G<<5) - (G<<1);
  Cr  -= (B<<4) + (B<<1);
  Cr >>= 8;
  Cr  += 128;
  return Cr;
}

} // namespace video
} // namespace satviz

#endif //SATVIZ_Y_CB_CR_HPP_
