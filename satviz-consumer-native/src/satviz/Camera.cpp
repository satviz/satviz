#include <satviz/Camera.hpp>

#include <cstring>

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

Camera::Camera() : position{0.0f, 0.0f}, zoom(2.0f) {}

void Camera::update() {
  zoom.update();
}

void Camera::toMatrix(float matrix[16], int width, int height) {
  float zoom = this->zoom.current();
  memset(matrix, 0, 16 * sizeof (float));
  matrix[ 0] = 2.0f / (float) width  * zoom;
  matrix[ 5] = 2.0f / (float) height * zoom;
  matrix[10] = -1.0f;
  matrix[12] = -position[0] * zoom;
  matrix[13] = -position[1] * zoom;
  matrix[15] = 1.0f;
}

} // namespace video
} // namespace satviz
