#include <satviz/Display.hpp>

#include <glad/gl.h>

namespace satviz {
namespace video {

sf::ContextSettings Display::makeContextSettings() {
  sf::ContextSettings settings;
  settings.attributeFlags |= sf::ContextSettings::Core;
  settings.attributeFlags |= sf::ContextSettings::Debug;
  settings.majorVersion = 3;
  settings.minorVersion = 3;
  return settings;
}

void Display::loadGlExtensions() {
  gladLoaderLoadGL();
}

void Display::startFrame() {
  glViewport(0, 0, width, height);
  glClearColor(0.3f, 0.3f, 0.3f, 0.0f);
  glClear(GL_COLOR_BUFFER_BIT);
}

void Display::endFrame() {
  displayFrame();
}

VideoFrame Display::grabFrame() {
  unsigned char *pixels = new unsigned char[4 * (size_t) width * (size_t) height];
  glReadPixels(0, 0, width, height, GL_RGBA, GL_UNSIGNED_BYTE, pixels);
  VideoFrame frame = VideoFrame::fromImage(width, height, pixels);
  delete[] pixels;
  return frame;
}

} // namespace video
} // namespace satviz
