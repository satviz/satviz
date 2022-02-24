#ifndef SATVIZ_CAMERA_HPP_
#define SATVIZ_CAMERA_HPP_

#include <SFML/System/Clock.hpp>

namespace satviz {
namespace video {

/**
 * A virtual camera from which graphs can be viewed/rendered.
 */
class Camera {
private:
  float position[2];
  sf::Clock clock;
  float oldZoom;
  float newZoom;
  float curZoom;

public:
  Camera();

  inline float getX() { return position[0]; }
  inline void setX(float v) { position[0] = v; }
  inline float getY() { return position[1]; }
  inline void setY(float v) { position[1] = v; }
  float getZoom() { return newZoom; }
  void setZoom(float z);

  void update();

  /**
   * Create an OpenGL world-to-view matrix based on this camera.
   * @param matrix output parameter
   * @param width  the width of the display
   * @param height the height of the display
   */
  void toMatrix(float *matrix, int width, int height);
};

} // namespace video
} // namespace satviz

#endif
