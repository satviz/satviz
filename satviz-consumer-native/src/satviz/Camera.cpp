#include <satviz/Camera.hpp>

#include <cstring>
#include <cmath>

namespace satviz {
namespace video {

Camera::SmoothedValue::SmoothedValue(float v) : clock(), oldValue(v), newValue(v), curValue(v) {}

void Camera::SmoothedValue::update() {
  float delta = clock.getElapsedTime().asSeconds();
  delta *= Camera::SMOOTH_SPEED;
  if (delta < 1.0f) {
    curValue = oldValue * (1.0f - delta) + newValue * delta;
  } else {
    curValue = oldValue = newValue;
  }
}

void Camera::SmoothedValue::set(float v) {
  oldValue = curValue;
  newValue = v;
  clock.restart();
}

Camera::Camera() : xpos(), ypos(), zoom(2.0f) {}

void Camera::zoomToFit(float boxWidth, float boxHeight, int dpyWidth, int dpyHeight) {
  float xZoom = boxWidth  ? (float) dpyWidth  / boxWidth  : INFINITY;
  float yZoom = boxHeight ? (float) dpyHeight / boxHeight : INFINITY;
  float mZoom = xZoom < yZoom ? xZoom : yZoom;
  if (std::isinf(mZoom)) mZoom = 2.0f;
  else mZoom *= 0.95f;
  setZoom(mZoom);
}

void Camera::update() {
  xpos.update();
  ypos.update();
  zoom.update();
}

void Camera::toMatrix(float matrix[16], int width, int height) {
  float zoom = this->zoom.current();
  float xScale = 2.0f / (float) width  * zoom;
  float yScale = 2.0f / (float) height * zoom;
  memset(matrix, 0, 16 * sizeof (float));
  matrix[ 0] = xScale;
  matrix[ 5] = yScale;
  matrix[10] = -1.0f;
  matrix[12] = -xpos.current() * xScale;
  matrix[13] = -ypos.current() * yScale;
  matrix[15] = 1.0f;
}

} // namespace video
} // namespace satviz
