#include <satviz/OnscreenDisplay.hpp>
#include <satviz/VideoController.hpp>

using namespace ::satviz;

int main() {
  video::Display *display = new video::OnscreenDisplay(640, 480);
  video::VideoController controller(NULL, display);
  while (!controller.wantToClose) {
    controller.nextFrame();
  }
  return 0;
}