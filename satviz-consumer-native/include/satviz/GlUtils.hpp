#ifndef SATVIZ_GL_UTILS_HPP_
#define SATVIZ_GL_UTILS_HPP_

#include <string>
#include <stddef.h>
#include <glad/gl.h> // TODO don't pull this include in

namespace satviz {
namespace video {

GLuint compileGlShader(const char *source, unsigned length, GLenum type);
GLuint linkGlProgram(GLuint vert_shader, GLuint frag_shader);
void resizeGlBuffer(GLuint *id, size_t old_size, size_t new_size);

} // namespace video
} // namespace satviz

#endif
