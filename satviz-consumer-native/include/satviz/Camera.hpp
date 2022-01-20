#ifndef SATVIZ_CAMERA_HPP_
#define SATVIZ_CAMERA_HPP_

namespace satviz {
namespace video {

/**
 *
 */
class Camera {
private:
  float position[2];
  float zoom;

public:
  Camera();

  // TODO Position Getters & Setters
  inline float getZoom() { return zoom; }
  inline void setZoom(float z) { zoom = z; }

  void toMatrix(float *matrix, int width, int height);
};

} // namespace video
} // namespace satviz

#endif
