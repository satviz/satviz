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
  positionX += (double) (fromX - toX) / zoomFactor;
  positionY -= (double) (fromY - toY) / zoomFactor;
}

void Camera::zoom(int atX, int atY, double factor) {
  double ratio = 1.0 / zoomFactor - 1.0 / (zoomFactor * factor);
  positionX += ratio * ((double) atX - (double) width  / 2.0);
  positionY -= ratio * ((double) atY - (double) height / 2.0);
  zoomFactor *= factor;
}

void Camera::focusOnBox(double boxX1, double boxY1, double boxX2, double boxY2) {
  positionX = 0.5 * (boxX1 + boxX2);
  positionY = 0.5 * (boxY1 + boxY2);

  double xZoom = (double) width  / (boxX2 - boxX1);
  double yZoom = (double) height / (boxY2 - boxY1);
  double mZoom = xZoom < yZoom ? xZoom : yZoom;
  if (std::isinf(mZoom)) mZoom = 2.0;
  else mZoom *= 0.95;
  zoomFactor = mZoom;
}

void Camera::toMatrix(double matrix[16]) {
  double xScale = 2.0 / (double) width  * zoomFactor;
  double yScale = 2.0 / (double) height * zoomFactor;
  double xTranslation = -positionX * xScale;
  double yTranslation = -positionY * yScale;

  memset(matrix, 0, 16 * sizeof (double));
  matrix[ 0] = xScale;
  matrix[ 5] = yScale;
  matrix[10] = -1.0;
  matrix[12] = xTranslation;
  matrix[13] = yTranslation;
  matrix[15] = 1.0;
}

} // namespace video
} // namespace satviz
