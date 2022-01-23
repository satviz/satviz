#include <satviz/OnscreenDisplay.hpp>

using namespace ::satviz;

int main() {
  video::Display *display = new video::OnscreenDisplay(640, 480);
  bool running = true;
  while (running) {
    sf::Event event;
    while (display->pollEvent(event)) {
      if (event.type == sf::Event::Closed)
        running = false;
    }
    display->displayFrame();
  }
  delete display;
  return 0;
}