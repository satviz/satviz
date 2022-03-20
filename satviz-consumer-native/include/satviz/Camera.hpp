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

  /**
   * Encapsulates a float value that is smoothed (interpolated) over time.
   */
  struct SmoothedValue {
    sf::Clock clock;
    float oldValue;
    float newValue;
    float curValue;

    /**
     * Constructor for SmoothedValue.
     * @param v the starting value
     */
    explicit SmoothedValue(float v = 0.0f);

    /**
     * Update the interpolation factor based on elapsed time.
     */
    void update();
    /**
     * Set the desired (target) value.
     * @param v the desired value
     */
    void set(float v);

    /**
     * Get the desired (target) value.
     * @return the desired value
     */
    inline float get() const { return newValue; }
    /**
     * Get the current (interpolated) value.
     * @return the current value
     */
    inline float current() const { return curValue; }
  };

  SmoothedValue xpos;
  SmoothedValue ypos;
  SmoothedValue zoom;

  int width  = 0;
  int height = 0;

  float getXScale() { return 2.0f / (float) width  * zoom.current(); }
  float getYScale() { return 2.0f / (float) height * zoom.current(); }
  float getXTranslation() { return -xpos.current() * getXScale(); }
  float getYTranslation() { return -ypos.current() * getYScale(); }

public:
  Camera();

  inline float getX() { return xpos.get(); }
  inline void setX(float v) { xpos.set(v); }
  inline float getY() { return ypos.get(); }
  inline void setY(float v) { ypos.set(v); }
  float getZoom() { return zoom.get(); }
  void setZoom(float z) { zoom.set(z); }
  void zoomToFit(float boxWidth, float boxHeight, int dpyWidth, int dpyHeight);

  /**
   * Give the camera the opportunity to adapt to a (possibly) changed display size & update time-interpolated values.
   *
   * @param width  the width of the display
   * @param height the height of the display
   */
  void update(int width, int height);

  /**
   * Create an OpenGL world-to-view matrix based on this camera.
   * @param matrix output parameter
   */
  void toMatrix(float *matrix);
};

} // namespace video
} // namespace satviz

#endif
