#include <satviz/Camera.hpp>

#include <cstring>
#include <cmath>

namespace satviz {
namespace video {

void Camera::update(int width, int height) {
  this->width  = width;
  this->height = height;
}

void Camera::drag(int fromX, int fromY, int toX, int toY) {
  sf::Vector2f from = deviceCoordsOfPixel(fromX, fromY);
  sf::Vector2f to   = deviceCoordsOfPixel(toX, toY);

  position.x += (float) width  / (2.0f * zoomFactor) * (from.x - to.x);
  position.y += (float) height / (2.0f * zoomFactor) * (from.y - to.y);
}

void Camera::zoom(int atX, int atY, float factor) {
  sf::Vector2f at = deviceCoordsOfPixel(atX, atY);
  position.x += (float) width  * at.x / 2.0f * (1.0f / zoomFactor - 1.0f / (zoomFactor * factor));
  position.y += (float) height * at.y / 2.0f * (1.0f / zoomFactor - 1.0f / (zoomFactor * factor));
  zoomFactor *= factor;
}

void Camera::focusOnBox(float boxX1, float boxY1, float boxX2, float boxY2) {
  position.x = 0.5f * (boxX1 + boxX2);
  position.y = 0.5f * (boxY1 + boxY2);

  float xZoom = (float) width  / (boxX2 - boxX1);
  float yZoom = (float) height / (boxY2 - boxY1);
  float mZoom = xZoom < yZoom ? xZoom : yZoom;
  if (std::isinf(mZoom)) mZoom = 2.0f;
  else mZoom *= 0.95f;
  zoomFactor = mZoom;
}

void Camera::toMatrix(float matrix[16]) {
  memset(matrix, 0, 16 * sizeof (float));
  matrix[ 0] = getXScale();
  matrix[ 5] = getYScale();
  matrix[10] = -1.0f;
  matrix[12] = getXTranslation();
  matrix[13] = getYTranslation();
  matrix[15] = 1.0f;
}

} // namespace video
} // namespace satviz
