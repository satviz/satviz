#ifndef SATVIZ_GL_UTILS_HPP_
#define SATVIZ_GL_UTILS_HPP_

#include <string>
#include <stddef.h>
#include <glad/gl.h> // TODO don't pull this include in

namespace satviz {
namespace video {

void logGlDebugMessages();
GLuint compileGlShader(const char *source, unsigned length, GLenum type);
GLuint linkGlProgram(GLuint vert_shader, GLuint frag_shader);
void allocateGlBuffer(GLuint id, const char *name, size_t size);
void resizeGlBuffer(GLuint *id, size_t old_size, size_t new_size);
void simpleGlVertexAttrib(GLuint attr, GLuint buffer, int count, GLenum type, int divisor);

} // namespace video
} // namespace satviz

#endif
