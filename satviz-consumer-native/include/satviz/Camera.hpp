#ifndef SATVIZ_CAMERA_HPP_
#define SATVIZ_CAMERA_HPP_

#include <SFML/System/Clock.hpp>
#include <SFML/System/Vector2.hpp>

namespace satviz {
namespace video {

/**
 * A virtual camera from which graphs can be viewed/rendered.
 */
class Camera {
private:
  sf::Vector2f position;
  float zoomFactor = 1.0f;
  int width  = 0;
  int height = 0;

public:
  /**
   * Give the camera the opportunity to adapt to a (possibly) changed display size & update time-interpolated values.
   *
   * @param width  the width of the display
   * @param height the height of the display
   */
  void update(int width, int height);

  void drag(int fromX, int fromY, int toX, int toY);
  void zoom(int atX, int atY, float factor);
  void focusOnBox(float boxX1, float boxY1, float boxX2, float boxY2);

  /**
   * Create an OpenGL world-to-view matrix based on this camera.
   * @param matrix output parameter
   */
  void toMatrix(float *matrix);
};

} // namespace video
} // namespace satviz

#endif
