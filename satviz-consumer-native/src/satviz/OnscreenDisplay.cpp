#include <satviz/OnscreenDisplay.hpp>

namespace satviz {
namespace video {

OnscreenDisplay::OnscreenDisplay(int w, int h)
  : Display(w, h) {
  sf::ContextSettings settings = makeContextSettings();
  window.create(sf::VideoMode(w, h), "satviz", sf::Style::Default, settings);
  initializeGl();
}

OnscreenDisplay::~OnscreenDisplay() {
  deinitializeGl();
  window.close();
}

void OnscreenDisplay::activateContext() {
  window.setActive(true);
}

bool OnscreenDisplay::pollEvent(sf::Event &event) {
  if (window.pollEvent(event)) {
    if (event.type == sf::Event::Resized) {
      if (size_locked) {
        window.setSize(sf::Vector2<unsigned>(getWidth(), getHeight()));
      } else {
        onResize(event.size.width, event.size.height);
      }
    }
    return true;
  } else {
    return false;
  }
}

void OnscreenDisplay::displayFrame() {
  window.display();
}

} // namespace video
} // namespace satviz