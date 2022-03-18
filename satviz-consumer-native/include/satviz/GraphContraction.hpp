#include <vector>

namespace satviz {
namespace graph {

class Graph;

struct Conn {
  int   index;
  float weight;

  Conn(int index = 0, float weight = 0.0f) : index(index), weight(weight) {}
};

class UnionFind {
private:
  int num;
  int *parents;

public:
  UnionFind(int num) : num(num) {
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

std::vector<Conn> *extractConnections(Graph &graph);

std::vector<Conn> mergeConnections(const std::vector<Conn> &a, const std::vector<Conn> &b);
std::vector<Conn> removeSelfLoops(int index, std::vector<Conn> &adj, UnionFind *uf);

int *computeContraction(int numNodes, std::vector<Conn> *conn, int iterations);
int *computeContraction(Graph &graph, int iterations);

} // namespace graph
} // namespace satviz

