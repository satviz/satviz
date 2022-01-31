#ifndef SATVIZ_OFFSCREEN_DISPLAY_HPP_
#define SATVIZ_OFFSCREEN_DISPLAY_HPP_

#include <satviz/Display.hpp>

namespace satviz {
namespace video {

/**
 * A Display that does not open a window or show anything on the screen.
 */
class OffscreenDisplay : public Display {
private:
  sf::Context context;

  void activateContext() override;

public:
  OffscreenDisplay(int width, int height);
  ~OffscreenDisplay();

  bool pollEvent(sf::Event &event) override { (void) event; return false; }
  void lockSize(bool lock) override { (void) lock; }
  void displayFrame() override {}
};

} // namespace video
} // namespace satviz

#endif
