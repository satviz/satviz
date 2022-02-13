#ifndef SATVIZ_ONSCREEN_DISPLAY_HPP_
#define SATVIZ_ONSCREEN_DISPLAY_HPP_

#include <satviz/Display.hpp>

namespace satviz {
namespace video {

/**
 * A Display that renders to a window.
 */
class OnscreenDisplay : public Display {
private:
  sf::Window window;

  void activateContext() override;

public:
  OnscreenDisplay(int width, int height);
  ~OnscreenDisplay() override;

  bool pollEvent(sf::Event &event) override;
  void displayFrame() override;
};

} // namespace video
} // namespace satviz

#endif
