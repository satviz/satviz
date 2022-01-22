#ifndef SATVIZ_GL_UTILS_HPP_
#define SATVIZ_GL_UTILS_HPP_

#include <string>
#include <stddef.h>
#include <glad/gl.h>

namespace satviz {
namespace video {

bool readTextFile(const char *filename, std::string &string);
GLuint compileGlShader(const char *filename, GLenum type);
GLuint linkGlProgram(GLuint vert_shader, GLuint frag_shader);
void resizeGlBuffer(GLuint *id, size_t old_size, size_t new_size);

} // namespace video
} // namespace satviz

#endif
