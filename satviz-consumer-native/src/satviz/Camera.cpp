#include <satviz/Camera.hpp>

#include <cstring>

namespace satviz {
namespace video {

Camera::Camera() : position{0.0f, 0.0f}, clock() {
  curZoom = oldZoom = newZoom = 2.0f;
}

void Camera::setZoom(float z) {
  oldZoom = curZoom;
  newZoom = z;
  clock.restart();
}

void Camera::update() {
  float delta = clock.getElapsedTime().asSeconds();
  delta /= 0.3f;
  if (delta < 1.0f) {
    curZoom = oldZoom * (1.0f - delta) + newZoom * delta;
  } else {
    curZoom = oldZoom = newZoom;
  }
}

void Camera::toMatrix(float matrix[16], int width, int height) {
  float zoom = curZoom;
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
