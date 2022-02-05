#include <satviz/GlUtils.hpp>

#include <iostream>

#include <glad/gl.h>

#define MIN(a,b) ((a)<(b)?(a):(b))

namespace satviz {
namespace video {

static void messageCallback(GLenum source, GLenum type, GLuint id, GLenum severity,
  GLsizei length, const GLchar *message, const void *userParam) {
  (void) source;
  (void) type;
  (void) id;
  (void) severity;
  (void) length;
  (void) userParam;
  std::cerr << "GL: " << message << std::endl;
}

void logGlDebugMessages() {
  glDebugMessageCallback(messageCallback, NULL);
}

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
    std::cerr << "GLSL error: " << msg_buf << std::endl;
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
    std::cerr << "GLSL link error: " << msg_buf << std::endl;
  }

  glDetachShader(id, vert_shader);
  glDetachShader(id, frag_shader);

  return id;
}

void allocateGlBuffer(GLuint id, const char *name, size_t size) {
  glBindBuffer(GL_COPY_WRITE_BUFFER, id);
  glObjectLabel(GL_BUFFER, id, -1, name);
  glBufferData(GL_COPY_WRITE_BUFFER, size, NULL, GL_DYNAMIC_DRAW);
  glBindBuffer(GL_COPY_WRITE_BUFFER, 0);
}

void resizeGlBuffer(GLuint *id, size_t old_size, size_t new_size) {
  char label[128];
  GLuint new_id;

	glGenBuffers(1, &new_id);
  glBindBuffer(GL_COPY_READ_BUFFER,  *id);
  glBindBuffer(GL_COPY_WRITE_BUFFER, new_id);

  glGetObjectLabel(GL_BUFFER, *id, sizeof label, NULL, label);
  glObjectLabel(GL_BUFFER, new_id, -1, label);

  glBufferData(GL_COPY_WRITE_BUFFER, new_size, NULL, GL_DYNAMIC_DRAW);
	glCopyBufferSubData(GL_COPY_READ_BUFFER, GL_COPY_WRITE_BUFFER, 0, 0, MIN(old_size, new_size));

  glBindBuffer(GL_COPY_READ_BUFFER,  0);
	glBindBuffer(GL_COPY_WRITE_BUFFER, 0);

  glDeleteBuffers(1, id);
	*id = new_id;
}

void simpleGlVertexAttrib(GLuint attr, GLuint buffer, int count, GLenum type, int divisor) {
  glBindBuffer(GL_ARRAY_BUFFER, buffer);
  glEnableVertexAttribArray(attr);
  glVertexAttribPointer(attr, count, type, GL_TRUE, 0, (void *) 0);
  glVertexAttribDivisor(attr, divisor);
}

} // namespace video
} // namespace satviz