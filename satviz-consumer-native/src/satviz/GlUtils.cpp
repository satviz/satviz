#include <satviz/GlUtils.hpp>

#include <glad/gl.h>

#define MIN(a,b) ((a)<(b)?(a):(b))

namespace satviz {
namespace video {

GLuint compileGlShader(const char *source, unsigned length, GLenum type) {
  GLuint id = glCreateShader(type);
  glShaderSource(id, 1, (const GLchar * const *) &source, (const GLint *) &length);
  glCompileShader(id);
  GLint success;
  glGetShaderiv(id, GL_COMPILE_STATUS, &success);
  if (!success) {
    char msg_buf[1024];
    GLsizei msg_len;
    glGetShaderInfoLog(id, sizeof msg_buf, &msg_len, msg_buf);
    fprintf(stderr, "GLSL error: %s\n", msg_buf);
  }
  return id;
}

GLuint linkGlProgram(GLuint vert_shader, GLuint frag_shader) {
  GLuint id = glCreateProgram();

  glAttachShader(id, vert_shader);
  glAttachShader(id, frag_shader);

  glLinkProgram(id);
  GLint success;
  glGetProgramiv(id, GL_LINK_STATUS, &success);
  if (!success) {
    char msg_buf[1024];
    GLsizei msg_len;
    glGetProgramInfoLog(id, sizeof msg_buf, &msg_len, msg_buf);
    fprintf(stderr, "glsl link error: %s\n", msg_buf);
  }

  glDetachShader(id, vert_shader);
  glDetachShader(id, frag_shader);

  return id;
}

void resizeGlBuffer(GLuint *id, size_t old_size, size_t new_size) {
  GLuint new_id;
	glGenBuffers(1, &new_id);
	glBindBuffer(GL_COPY_READ_BUFFER,  *id);
	glBindBuffer(GL_COPY_WRITE_BUFFER, new_id);
	glBufferStorage(GL_COPY_WRITE_BUFFER, new_size, NULL, GL_DYNAMIC_DRAW);
	glCopyBufferSubData(GL_COPY_READ_BUFFER, GL_COPY_WRITE_BUFFER, 0, 0, MIN(old_size, new_size));
	glBindBuffer(GL_COPY_READ_BUFFER,  0);
	glBindBuffer(GL_COPY_WRITE_BUFFER, 0);
	glDeleteBuffers(1, id);
	*id = new_id;
}

} // namespace video
} // namespace satviz