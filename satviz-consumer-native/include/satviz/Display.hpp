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
  /**
   * Load OpenGL function pointers.
   *
   * This is done at object construction time.
   */
  void loadGlExtensions();
  /**
   * Switch to this Displays OpenGL context.
   */
  virtual void activateContext() = 0;

  /**
   * Put the newly drawn frame on the screen.
   */
  virtual void displayFrame() = 0;

  void onResize();

public:
  virtual ~Display();

  inline int getWidth() { return width; }
  inline int getHeight() { return height; }

  // TODO Camera

  /**
   * Prepare the OpenGL state to draw a new frame.
   */
  void startFrame();
  void endFrame();
  void transferFrame();
  VideoFrame grabFrame();

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
