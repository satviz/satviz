#include <satviz/Graph.hpp>

#include <vector>
#include <algorithm>

namespace satviz {
namespace graph {

struct Conn {
  int   index;
  float weight;
};

class UnionFind {
private:
  int *parents;

public:
  UnionFind(int num) {
    parents = new int[num];
    for (int i = 0; i < num; i++) {
      parents[i] = i;
    }
  }

  ~UnionFind() {
    delete[] parents;
  }

  void unite(int a, int b) {
    a = find(a);
    b = find(b);
    if (a == b) return;
    parents[b] = a;
  }

  int find(int i) {
    if (parents[i] == i) {
      return i;
    } else {
      parents[i] = find(parents[i]);
      return parents[i];
    }
  }
};

static std::vector<Conn> mergeConnections(const std::vector<Conn> &a, const std::vector<Conn> &b) {
  std::vector<Conn> d;
  int i = 0, j = 0;
  int maxi = (int) a.size(), maxj = (int) b.size();
  while (i < maxi && j < maxj) {
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

static std::vector<Conn> removeSelfLoops(int index, std::vector<Conn> &adj, UnionFind &uf) {
  std::vector<Conn> d;
  int repr = uf.find(index);
  std::copy_if(adj.begin(), adj.end(), std::back_inserter(d),
               [uf, repr](Conn c) mutable { return uf.find(c.index) != repr; });
  return d;
}

std::vector<int> computeContraction(Graph &graph, int iterations) {
  const int numNodes = graph.numNodes();

  UnionFind uf(numNodes);

  std::vector<int> participants;
  for (int i = 0; i < numNodes; i++) {
    participants.push_back(i);
  }

  ogdf::Graph &og = graph.getOgdfGraph();
  ogdf::GraphAttributes &oa = graph.getOgdfAttrs();
  std::vector<Conn> *conn = new std::vector<Conn>[numNodes];
  for (auto e = og.firstEdge(); e; e = e->succ()) {
    int a = e->source()->index();
    int b = e->target()->index();
    float weight = (float) oa.doubleWeight(e);
    conn[a].push_back(Conn{ b, weight });
    conn[b].push_back(Conn{ a, weight });
  }

  while (iterations--) {
    for (int v : participants) {
      Conn best{ v, 0.0f };
      for (auto c : conn[v]) {
        if (c.weight > best.weight) best = c;
      }
      uf.unite(v, best.index);
    }

    std::vector<int> reprs;
    for (int v : participants) {
      int repr = uf.find(v);
      if (v == repr) {
        reprs.push_back(v);
      } else {
        std::vector<Conn> adj;
        adj = mergeConnections(conn[repr], conn[v]);
        adj = removeSelfLoops(repr, adj, uf);
        conn[repr] = adj;
      }
    }
    participants = reprs;
  }

  int *renumbering = new int[numNodes];
  int current = 0;
  for (int i = 0; i < numNodes; i++) {
    if (i == uf.find(i)) {
      renumbering[i] = current++;
    }
  }

  std::vector<int> mapping;
  for (int v : participants) {
    mapping.push_back(renumbering[uf.find(v)]);
  }

  delete[] renumbering;
  delete[] conn;
  return mapping;
}

} // namespace graph
} // namespace satviz
