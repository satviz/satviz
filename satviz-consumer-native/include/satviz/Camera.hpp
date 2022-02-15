#ifndef SATVIZ_CAMERA_HPP_
#define SATVIZ_CAMERA_HPP_

namespace satviz {
namespace video {

/**
 * A virtual camera from which graphs can be viewed/rendered.
 */
class Camera {
private:
  float position[2];
  float zoom;

public:
  Camera() : position{0.0f, 0.0f}, zoom(2.0f) {}

  inline float getX() { return position[0]; }
  inline void setX(float v) { position[0] = v; }
  inline float getY() { return position[1]; }
  inline void setY(float v) { position[1] = v; }
  inline float getZoom() { return zoom; }
  inline void setZoom(float z) { zoom = z; }

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
