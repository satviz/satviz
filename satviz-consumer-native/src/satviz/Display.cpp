#include <satviz/Display.hpp>

#include <glad/gl.h>

namespace satviz {
namespace video {

sf::ContextSettings Display::makeContextSettings()
{
  sf::ContextSettings settings;
  settings.attributeFlags |= sf::ContextSettings::Core;
  settings.attributeFlags |= sf::ContextSettings::Debug;
  settings.majorVersion = 3;
  settings.minorVersion = 3;
  return settings;
}

void Display::loadGlExtensions()
{
  gladLoaderLoadGL();
}

void Display::startFrame()
{
  glViewport(0, 0, width, height);
  glClearColor(0.1f, 0.1f, 0.1f, 0.0f);
  glClear(GL_COLOR_BUFFER_BIT);
}

} // namespace video
} // namespace satviz
