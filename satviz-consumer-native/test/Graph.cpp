#include <gtest/gtest.h>

#include <satviz/Graph.hpp>
#include <satviz/GraphObserver.hpp>

#include <string>
#include <sstream>

using namespace ::satviz::graph;

class DummyObserver : public GraphObserver {
public:
  std::string log;

  DummyObserver(Graph &gr) : GraphObserver(gr) {}

  virtual void onWeightUpdate(WeightUpdate &update) { (void) update; log.push_back('w'); }
  virtual void onHeatUpdate(HeatUpdate &update) { (void) update; log.push_back('h'); }
  virtual void onLayoutChange(ogdf::Array<ogdf::node> &changed) { (void) changed; log.push_back('l'); }
  virtual void onEdgeAdded(ogdf::edge e) { (void) e; log.push_back('+'); }
  virtual void onEdgeDeleted(ogdf::edge e) { (void) e; log.push_back('-'); }
  virtual void onReload() { log.push_back('r'); }

  /**
   * Check whether a sequence of events happened (in that specific order).
   * @param seq the sequence of events
   * @return true if it happened
   */
  bool sequenceHappened(const char *seq) {
    size_t cur = 0;
    for (size_t i = 0; i < log.length(); i++) {
      if (log[i] == seq[cur]) {
        cur++;
        if (!seq[cur]) return true;
      }
    }
    return false;
  }
};

class GraphTest : public ::testing::Test {
protected:
  const int     numNodes = 5;
  Graph         graph;
  DummyObserver observer;

  WeightUpdate  weightUpdate1;
  HeatUpdate    heatUpdate1;

  WeightUpdate  weightUpdate2;
  HeatUpdate    heatUpdate2;

  GraphTest() : graph(numNodes), observer(graph) {
    graph.addObserver(&observer);

    for (int i = 0; i < numNodes; i++) {
      // Ring of edges with non-zero, non-uniform weights
      weightUpdate1.values.push_back(std::make_tuple(i, (i + 1) % numNodes, (float) (i + 1) / (float) numNodes));
      // Set node heat values to non-zero, non-uniform values
      heatUpdate1.values.push_back(std::make_tuple(i, i + 1));

      // Set every other edge weight to 0
      if (i % 2 == 0) {
        weightUpdate2.values.push_back(std::make_tuple(i, (i + 1) % numNodes, 0.0f));
      }
      // Set every other heat value to 0
      if (i % 2 == 0) {
        heatUpdate2.values.push_back(std::make_tuple(i, 0));
      }
    }

    graph.submitWeightUpdate(weightUpdate1);
    graph.submitHeatUpdate(heatUpdate1);
    graph.submitWeightUpdate(weightUpdate2);
    graph.submitHeatUpdate(heatUpdate2);
  }
};

TEST_F(GraphTest, WeightUpdate) {
  ASSERT_TRUE(observer.sequenceHappened("+++++w---w"));
  ASSERT_EQ(graph.numEdges(), weightUpdate1.values.size() - weightUpdate2.values.size());
  for (int i = 0; i < numNodes; i++) {
    if (i % 2 != 0) {
      EdgeInfo info = graph.queryEdge(i, (i + 1) % numNodes);
      ASSERT_EQ(info.weight, (float) (i + 1) / (float) numNodes);
    }
  }
}

TEST_F(GraphTest, HeatUpdate) {
  ASSERT_TRUE(observer.sequenceHappened("hh"));
  for (int i = 0; i < numNodes; i++) {
    NodeInfo info = graph.queryNode(i);
    if (i % 2 == 0) {
      ASSERT_EQ(info.heat, 0);
    } else {
      ASSERT_EQ(info.heat, i + 1);
    }
  }
}

TEST_F(GraphTest, RecalculateLayout) {
  ASSERT_FALSE(observer.sequenceHappened("l"));
  graph.recalculateLayout();
  ASSERT_TRUE(observer.sequenceHappened("l"));
}

TEST_F(GraphTest, Serialization) {
  std::stringstream stream;

  graph.serialize(stream);
  ASSERT_FALSE(observer.sequenceHappened("r"));

  // Change heat values, then reload previous values
  graph.submitHeatUpdate(heatUpdate1);
  graph.deserialize(stream);
  ASSERT_TRUE(observer.sequenceHappened("r"));

  // Make sure the heat value update has been properly undone
  for (int i = 0; i < numNodes; i++) {
    if (i % 2 != 0) continue;
    NodeInfo info = graph.queryNode(i);
    ASSERT_EQ(info.heat, 0);
  }
}
