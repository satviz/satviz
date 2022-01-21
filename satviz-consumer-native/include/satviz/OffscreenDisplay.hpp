#ifndef SATVIZ_OFFSCREEN_DISPLAY_HPP_
#define SATVIZ_OFFSCREEN_DISPLAY_HPP_

#include <satviz/Display.hpp>

namespace satviz {
namespace video {

/**
 * A Display that does not open a window or show anything on the screen.
 */
class OffscreenDisplay : Display {
private:
  sf::Context context;

  void activateContext();

public:
  OffscreenDisplay(int width, int height);
  ~OffscreenDisplay();

  bool pollEvent(sf::Event &event) { (void) event; return false; }
  void lockSize(bool lock) { (void) lock; }
  void displayFrame() {}
};

} // namespace video
} // namespace satviz

#endif
