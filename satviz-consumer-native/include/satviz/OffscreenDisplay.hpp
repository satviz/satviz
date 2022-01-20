#ifndef SATVIZ_OFFSCREEN_DISPLAY_HPP_
#define SATVIZ_OFFSCREEN_DISPLAY_HPP_

#include <satviz/Display.hpp>

namespace satviz {
namespace video {

/**
 *
 */
class OffscreenDisplay : Display {
private:
  sf::Context context;

  void activateContext();

public:
  OffscreenDisplay(int width, int height);
  ~OffscreenDisplay();

  bool pollEvent(sf::Event &event) { return false; }
  void lockSize(bool lock);
};

} // namespace video
} // namespace satviz

#endif
