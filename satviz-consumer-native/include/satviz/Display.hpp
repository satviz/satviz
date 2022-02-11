#ifndef SATVIZ_DISPLAY_HPP_
#define SATVIZ_DISPLAY_HPP_

#include <satviz/VideoFrame.hpp>

#include <SFML/System.hpp>
#include <SFML/Window.hpp>

namespace satviz {
namespace video {

/**
 * A canvas for drawing operations.
 *
 * Internally, each Display object manages its own OpenGL context.
 */
class Display {
private:
  int width;
  int height;

protected:
  enum {
    PBO_IN_PROGRESS,
    PBO_READY,
    NUM_PBOS
  };

  bool size_locked = false;
  unsigned pbos[NUM_PBOS];

  Display(int w, int h) : width(w), height(h) {}

  /**
   * @return our preferred OpenGL context settings
   */
  static sf::ContextSettings makeContextSettings();
  void initializeGl();
  void deinitializeGl();
  void onResize(int w, int h);

  /**
   * Switch to this Displays OpenGL context.
   */
  virtual void activateContext() = 0;
  /**
   * Put the newly drawn frame on the screen.
   */
  virtual void displayFrame() = 0;

public:
  virtual ~Display() = default;

  inline int getWidth() { return width; }
  inline int getHeight() { return height; }

  /**
   * Set whether the current size of the display should be mutable or immutable.
   * @param lock if set to true, the display size will be immutable
   */
  void lockSize(bool lock);

  /**
   * Prepare the OpenGL state to draw a new frame.
   */
  void startFrame();
  /**
   * Stop rendering the current frame and display it on the screen.
   */
  void endFrame();
  /**
   * Initiate the transfer of the currently drawn frame into RAM.
   *
   * The actual transfer will take place asynchronously during the next frame.
   */
  void transferCurrentFrame();
  /**
   * Convert the previously transferred frame to a VideoFrame.
   *
   * (This data is from *two* invocations of transferCurrentFrame() ago!)
   * @param geom TODO
   * @return the VideoFrame
   */
  VideoFrame grabPreviousFrame(const VideoGeometry &geom);

  /**
   * Poll for user input events.
   * @param event structure that will be filled with event info
   * @return true if an event has been found
   */
  virtual bool pollEvent(sf::Event &event) = 0;
};

} // namespace video
} // namespace satviz

#endif
