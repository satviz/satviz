#include <satviz/GraphContraction.hpp>
#include <satviz/Graph.hpp>

#include <algorithm>

namespace satviz {
namespace graph {

std::vector<Conn> *extractConnections(Graph &graph) {
  ogdf::Graph &og = graph.getOgdfGraph();
  ogdf::GraphAttributes &oa = graph.getOgdfAttrs();

  std::vector<Conn> *conn = new std::vector<Conn>[graph.numNodes()];
  for (auto e = og.firstEdge(); e; e = e->succ()) {
    int a = e->source()->index();
    int b = e->target()->index();
    float weight = (float) oa.doubleWeight(e);
    conn[a].emplace_back(b, weight);
    conn[b].emplace_back(a, weight);
  }

  for (int i = 0; i < (int) graph.numNodes(); i++) {
    std::sort(conn[i].begin(), conn[i].end(),
              [](Conn a, Conn b) { return a.index < b.index; });
  }

  return conn;
}

std::vector<Conn> mergeConnections(const std::vector<Conn> &a, const std::vector<Conn> &b) {
  std::vector<Conn> d;
  int i = 0, j = 0;
  int maxi = (int) a.size(), maxj = (int) b.size();
  while (i < maxi || j < maxj) {
    Conn c;
    if (j == maxj) c = a[i++];
    else if (i == maxi) c = b[j++];
    else if (a[i].index < b[j].index) c = a[i++];
    else if (a[i].index > b[j].index) c = b[j++];
    else {
      c.index  = a[i].index;
      c.weight = a[i++].weight + b[j++].weight;
    }
    d.push_back(c);
  }
  return d;
}

std::vector<Conn> removeSelfLoops(int index, std::vector<Conn> &adj, UnionFind *uf) {
  std::vector<Conn> d;
  int repr = uf->find(index);
  std::copy_if(adj.begin(), adj.end(), std::back_inserter(d),
               [uf, repr](Conn c) mutable { return uf->find(c.index) != repr; });
  return d;
}

/** An axis along which to collapse. */
struct Axis {
  int  node;
  Conn conn;
  Axis(int node, Conn conn) : node(node), conn(conn) {}
};

int computeContraction(int numNodes, std::vector<Conn> *conn, int iterations, int *mapping) {
  const float AGGRESSION = 0.3f;

  UnionFind uf(numNodes);

  std::vector<int> participants;
  for (int i = 0; i < numNodes; i++) {
    participants.push_back(i);
  }

  while (iterations--) {
    std::vector<Axis> choices;
    for (int v : participants) {
      Conn best{ v, 0.0f };
      for (auto c : conn[v]) {
        if (c.weight > best.weight) best = c;
      }
      choices.emplace_back(v, best);
    }

    std::sort(choices.begin(), choices.end(), [](Axis a, Axis b) { return a.conn.weight > b.conn.weight; });

    for (int i = 0; i < (int) ((float) choices.size() * AGGRESSION); i++) {
      auto axis = choices[i];
      uf.unite(axis.node, axis.conn.index);
    }

    std::vector<int> reprs;
    for (int v : participants) {
      int repr = uf.find(v);
      if (v == repr) {
        reprs.push_back(v);
      } else {
        std::vector<Conn> adj;
        adj = mergeConnections(conn[repr], conn[v]);
        adj = removeSelfLoops(repr, adj, &uf);
        conn[repr] = adj;
      }
    }
    participants = reprs;
  }

  int *renumbering = new int[numNodes];
  int remainingNodes = 0;
  for (int i = 0; i < numNodes; i++) {
    if (i == uf.find(i)) {
      renumbering[i] = remainingNodes++;
    }
  }

  for (int i = 0; i < numNodes; i++) {
    mapping[i] = renumbering[uf.find(i)];
  }
  
  delete[] renumbering;
  return remainingNodes;
}

int computeContraction(Graph &graph, int iterations, int *mapping) {
  auto conn = extractConnections(graph);
  int remainingNodes = computeContraction(graph.numNodes(), conn, iterations, mapping);
  delete[] conn;
  return remainingNodes;
}

} // namespace graph
} // namespace satviz
