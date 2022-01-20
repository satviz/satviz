#ifndef SATVIZ_ONSCREEN_DISPLAY_HPP_
#define SATVIZ_ONSCREEN_DISPLAY_HPP_

#include <satviz/Display.hpp>

namespace satviz {
namespace video {

/**
 *
 */
class OnscreenDisplay : Display {
private:
  sf::Window window;

  void activateContext();

public:
  OnscreenDisplay(int width, int height);
  ~OnscreenDisplay();

  bool pollEvent(sf::Event &event);
  void lockSize(bool lock);
};

} // namespace video
} // namespace satviz

#endif
