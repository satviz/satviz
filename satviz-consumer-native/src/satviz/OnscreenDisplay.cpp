#include <satviz/OnscreenDisplay.hpp>

namespace satviz {
namespace video {

OnscreenDisplay::OnscreenDisplay(int w, int h)
  : Display(w, h)
{
  sf::ContextSettings settings = makeContextSettings();
  window.create(sf::VideoMode(w, h), "satviz", sf::Style::Default, settings);
  loadGlExtensions();
}

OnscreenDisplay::~OnscreenDisplay()
{
  window.close();
}

void OnscreenDisplay::activateContext()
{
  window.setActive(true);
}

bool OnscreenDisplay::pollEvent(sf::Event &event)
{
  if (window.pollEvent(event)) {
    if (event.type == sf::Event::Resized) {
      width  = event.size.width;
      height = event.size.height;
    }
    return true;
  } else {
    return false;
  }
}

void OnscreenDisplay::lockSize(bool lock)
{
  // TODO stub
  (void) lock;
}

void OnscreenDisplay::displayFrame()
{
  window.display();
}

} // namespace video
} // namespace satviz