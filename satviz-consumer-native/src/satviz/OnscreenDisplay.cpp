#include <satviz/OnscreenDisplay.hpp>

namespace satviz {
namespace video {

OnscreenDisplay::OnscreenDisplay(int w, int h)
  : Display(w, h)
{
  sf::ContextSettings settings;
  settings.attributeFlags |= sf::ContextSettings::Core;
  settings.attributeFlags |= sf::ContextSettings::Debug;
  settings.majorVersion = 3;
  settings.minorVersion = 3;
  window.create(sf::VideoMode(w, h), "satviz", sf::Style::Default, settings);
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

} // namespace video
} // namespace satviz