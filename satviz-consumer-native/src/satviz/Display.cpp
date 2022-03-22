#include <satviz/Display.hpp>

#include <glad/gl.h>

#include <cstring>

namespace satviz {
namespace video {

sf::ContextSettings Display::makeContextSettings() {
  sf::ContextSettings settings;
  settings.attributeFlags |= sf::ContextSettings::Core;
  //settings.attributeFlags |= sf::ContextSettings::Debug;
  settings.majorVersion = 3;
  settings.minorVersion = 3;
  return settings;
}

void Display::initializeGl() {
  gladLoaderLoadGL();
  glGenBuffers(NUM_PBOS, pbos);
  onResize(width, height);
}

void Display::deinitializeGl() {
  glDeleteBuffers(NUM_PBOS, pbos);
}

void Display::onResize(int w, int h) {
  if (size_locked) return;
  width  = w;
  height = h;
  // Resize GL viewport
  glViewport(0, 0, width, height);
  // Resize pixel buffer objects
  glBindBuffer(GL_PIXEL_PACK_BUFFER, pbos[PBO_IN_PROGRESS]);
  glBufferData(GL_PIXEL_PACK_BUFFER, 4 * width * height, NULL, GL_STREAM_READ);
  glBindBuffer(GL_PIXEL_PACK_BUFFER, pbos[PBO_READY]);
  glBufferData(GL_PIXEL_PACK_BUFFER, 4 * width * height, NULL, GL_STREAM_READ);
  glBindBuffer(GL_PIXEL_PACK_BUFFER, 0);
}

void Display::lockSize(bool lock) {
  size_locked = lock;
  if (!size_locked) {
    onResize(width, height);
  }
}

void Display::startFrame() {
  glClearColor(0.3f, 0.3f, 0.3f, 0.0f);
  glClear(GL_COLOR_BUFFER_BIT);
}

void Display::endFrame() {
  // Switch PBOs
  {
    unsigned temp = pbos[PBO_READY];
    pbos[PBO_READY] = pbos[PBO_IN_PROGRESS];
    pbos[PBO_IN_PROGRESS] = temp;
  }
  displayFrame();
}

void Display::transferCurrentFrame() {
  glBindBuffer(GL_PIXEL_PACK_BUFFER, pbos[PBO_IN_PROGRESS]);
  glReadPixels(0, 0, width, height, GL_BGRA, GL_UNSIGNED_BYTE, (void *) 0);
  glBindBuffer(GL_PIXEL_PACK_BUFFER, 0);
}

static void
flipImage(char *pixels, int width, int height) {
  int stride = 4 * width;
  char *temp = new char[stride];
  for (int r = 0; r < height / 2; r++) {
    char *row1 = pixels + stride * r;
    char *row2 = pixels + stride * (height - 1 - r);
    memcpy(temp, row1, stride);
    memcpy(row1, row2, stride);
    memcpy(row2, temp, stride);
  }
  delete[] temp;
}

VideoFrame Display::grabPreviousFrame(const VideoGeometry &geom) {
  glBindBuffer(GL_PIXEL_PACK_BUFFER, pbos[PBO_READY]);
  void *pixels = glMapBuffer(GL_PIXEL_PACK_BUFFER, GL_READ_ONLY);
  flipImage((char *) pixels, width, height);
  VideoFrame frame = VideoFrame::fromBgraImage(geom, pixels);
  glUnmapBuffer(GL_PIXEL_PACK_BUFFER);
  glBindBuffer(GL_PIXEL_PACK_BUFFER, 0);
  return frame;
}

} // namespace video
} // namespace satviz
