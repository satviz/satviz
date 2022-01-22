#include <satviz/GraphRenderer.hpp>

namespace satviz {
namespace video {

GraphRenderer::GraphRenderer(graph::Graph *gr)
  : GraphObserver(gr)
{
}

GraphRenderer::~GraphRenderer()
{
}

void GraphRenderer::draw(Camera &camera)
{
  (void) camera;
}

} // namespace video
} // namespace satviz
