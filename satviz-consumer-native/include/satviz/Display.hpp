#ifndef SATVIZ_DISPLAY_HPP_
#define SATVIZ_DISPLAY_HPP_

#include <satviz/VideoFrame.hpp>

#include <SFML/System.hpp>
#include <SFML/Window.hpp>

namespace satviz {
namespace video {

/**
 *
 */
class Display {
private:
  int width;
  int height;

  virtual void activateContext() = 0;

public:
  inline int getWidth() { return width; }
  inline int getHeight() { return height; }
  // TODO Camera

  void drawFrame();
  VideoFrame grabFrame();

  virtual bool pollEvent(sf::Event &event) = 0;
  virtual void lockSize(bool lock) = 0;
};

} // namespace video
} // namespace satviz

#endif
