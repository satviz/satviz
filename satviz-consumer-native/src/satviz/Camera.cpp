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
  positionX += (float) (fromX - toX) / zoomFactor;
  positionY -= (float) (fromY - toY) / zoomFactor;
}

void Camera::zoom(int atX, int atY, float factor) {
  float ratio = 1.0f / zoomFactor - 1.0f / (zoomFactor * factor);
  positionX += ratio * ((float) atX - (float) width  / 2.0f);
  positionY -= ratio * ((float) atY - (float) height / 2.0f);
  zoomFactor *= factor;
}

void Camera::focusOnBox(float boxX1, float boxY1, float boxX2, float boxY2) {
  positionX = 0.5f * (boxX1 + boxX2);
  positionY = 0.5f * (boxY1 + boxY2);

  float xZoom = (float) width  / (boxX2 - boxX1);
  float yZoom = (float) height / (boxY2 - boxY1);
  float mZoom = xZoom < yZoom ? xZoom : yZoom;
  if (std::isinf(mZoom)) mZoom = 2.0f;
  else mZoom *= 0.95f;
  zoomFactor = mZoom;
}

void Camera::toMatrix(float matrix[16]) {
  float xScale = 2.0f / (float) width  * zoomFactor;
  float yScale = 2.0f / (float) height * zoomFactor;
  float xTranslation = -positionX * xScale;
  float yTranslation = -positionY * yScale;

  memset(matrix, 0, 16 * sizeof (float));
  matrix[ 0] = xScale;
  matrix[ 5] = yScale;
  matrix[10] = -1.0f;
  matrix[12] = xTranslation;
  matrix[13] = yTranslation;
  matrix[15] = 1.0f;
}

} // namespace video
} // namespace satviz
