#include <satviz/Camera.hpp>

#include <cstring>

namespace satviz {
namespace video {

void Camera::toMatrix(float matrix[16], int width, int height) {
  memset(matrix, 0, 16 * sizeof (float));
  matrix[ 0] = 2.0f / (float) width  * zoom;
  matrix[ 5] = 2.0f / (float) height * zoom;
  matrix[10] = -1.0f;
  matrix[12] = -position[0];
  matrix[13] = -position[1];
  matrix[15] = 1.0f;
}

} // namespace video
} // namespace satviz
