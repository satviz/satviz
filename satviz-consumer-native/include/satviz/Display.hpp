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
protected:
  enum {
    PBO_IN_PROGRESS,
    PBO_READY,
    NUM_PBOS
  };

  int width;
  int height;
  unsigned pbos[NUM_PBOS];

  Display(int w, int h) : width(w), height(h) {}

  /**
   * @return our preferred OpenGL context settings
   */
  static sf::ContextSettings makeContextSettings();
  void initializeGl();
  void deinitializeGl();
  void onResize();

  /**
   * Switch to this Displays OpenGL context.
   */
  virtual void activateContext() = 0;
  /**
   * Put the newly drawn frame on the screen.
   */
  virtual void displayFrame() = 0;

public:
  virtual ~Display() {}

  inline int getWidth() { return width; }
  inline int getHeight() { return height; }

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
   * @return the VideoFrame
   */
  VideoFrame grabPreviousFrame();

  /**
   * Poll for user input events.
   * @param event structure that will be filled with event info
   * @return true if an event has been found
   */
  virtual bool pollEvent(sf::Event &event) = 0;
  virtual void lockSize(bool lock) = 0;
};

} // namespace video
} // namespace satviz

#endif
