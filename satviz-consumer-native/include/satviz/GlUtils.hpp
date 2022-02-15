#ifndef SATVIZ_GL_UTILS_HPP_
#define SATVIZ_GL_UTILS_HPP_

#include <string>
#include <stddef.h>
#include <glad/gl.h> // TODO don't pull this include in

namespace satviz {
namespace video {

/**
 * Set up a callback to listen for OpenGL debug messages and warnings.
 */
void logGlDebugMessages();

/**
 * Easily compile a GL shader.
 *
 * These have to be linked together to create GL programs.
 * @param source The source code that should be compiled
 * @param length The length of the source code in bytes
 * @param type   The type of GL shader (vertex shader, fragment shader, ...)
 * @return       The id of the shader object
 */
GLuint compileGlShader(const char *source, unsigned length, GLenum type);
/**
 * Link multiple GL shaders to create a GL program.
 * @param vert_shader The vertex shader that should be used
 * @param frag_shader The fragment shader that should be used
 * @return            The id of the program object
 */
GLuint linkGlProgram(GLuint vert_shader, GLuint frag_shader);

/**
 * Allocate storage space for a dynamic GL buffer.
 * @param id   The id of the buffer object
 * @param name A debug name that shall be attached to the buffer
 * @param size The size of the storage space in bytes
 */
void allocateGlBuffer(GLuint id, const char *name, size_t size);
/**
 * Resize the storage space of a dynamic GL buffer.
 * @param id       A pointer to the id of the buffer object. This is an in-out parameter.
 * @param old_size The previous size of the buffer in bytes
 * @param new_size The desired size of the buffer in bytes
 */
void resizeGlBuffer(GLuint *id, size_t old_size, size_t new_size);

/**
 * Easily register a new vertex attributes in the current VAO.
 * @param attr    The vertex attribute location (index)
 * @param buffer  The buffer that holds the attribute data
 * @param count   Number of components per vertex
 * @param type    The type of each component
 * @param divisor the GL vertex attrib divisor value (How many elements per instance)
 */
void simpleGlVertexAttrib(GLuint attr, GLuint buffer, int count, GLenum type, int divisor);

} // namespace video
} // namespace satviz

#endif
