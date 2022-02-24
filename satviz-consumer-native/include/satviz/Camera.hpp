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
  constexpr static const float SMOOTH_SPEED = 1.0f / 0.3f;

  struct SmoothedValue {
    sf::Clock clock;
    float oldValue;
    float newValue;
    float curValue;

    explicit SmoothedValue(float v = 0.0f);

    void update();
    void set(float v);

    inline float get() const { return newValue; }
    inline float current() const { return curValue; }
  };

  SmoothedValue xpos;
  SmoothedValue ypos;
  SmoothedValue zoom;

public:
  Camera();

  inline float getX() { return xpos.get(); }
  inline void setX(float v) { xpos.set(v); }
  inline float getY() { return ypos.get(); }
  inline void setY(float v) { ypos.set(v); }
  float getZoom() { return zoom.get(); }
  void setZoom(float z) { zoom.set(z); }

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
