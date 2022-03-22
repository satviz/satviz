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
  double positionX = 0.0;
  double positionY = 0.0;
  double zoomFactor = 1.0;
  int width  = 0;
  int height = 0;

public:
  /**
   * Lets the camera know about the current display size.
   *
   * @param width  the width of the display
   * @param height the height of the display
   */
  void update(int width, int height);

  /**
   * Move the camera by dragging across the screen.
   *
   * @param fromX the pixel x coordinate where the dragging started
   * @param fromY the pixel y coordinate where the dragging started
   * @param toX the pixel x coordinate where the dragging ended
   * @param toY the pixel y coordinate where the dragging ended
   */
  void drag(int fromX, int fromY, int toX, int toY);

  /**
   * Zoom towards (or away from) a point on the screen.
   *
   * @param atX the x coordinate of the point in pixels
   * @param atY the y coordinate of the point in pixels
   * @param factor how much to zoom by. Positive values mean zooming in, negative mean zooming out.
   */
  void zoom(int atX, int atY, double factor);

  /**
   * Focus the camera on a box of coordinates that should be completely within view.
   *
   * @param boxX1 minimum x coordinate of the box
   * @param boxY1 minimum y coordinate of the box
   * @param boxX2 maximum x coordinate of the box
   * @param boxY2 maximum y coordinate of the box
   */
  void focusOnBox(double boxX1, double boxY1, double boxX2, double boxY2);

  /**
   * Create an OpenGL world-to-view matrix based on this camera.
   *
   * @param matrix output parameter
   */
  void toMatrix(double *matrix);
};

} // namespace video
} // namespace satviz

#endif
