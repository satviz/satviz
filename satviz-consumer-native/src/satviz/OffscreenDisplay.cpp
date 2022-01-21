#include <satviz/OffscreenDisplay.hpp>

namespace satviz {
namespace video {

OffscreenDisplay::OffscreenDisplay(int w, int h)
  : Display(w, h), context(makeContextSettings(), w, h)
{
  loadGLExtensions();
}

OffscreenDisplay::~OffscreenDisplay()
{
}

void OffscreenDisplay::activateContext()
{
  context.setActive(true);
}

} // namespace video
} // namespace satviz