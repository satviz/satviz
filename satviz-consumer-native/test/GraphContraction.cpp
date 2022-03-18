#include <gtest/gtest.h>

#include <satviz/GraphContraction.hpp>
#include <satviz/Graph.hpp>

using namespace satviz::graph;

TEST(GraphContraction, UnionFind) {
	UnionFind uf(10);

	EXPECT_NE(1, 2);
	uf.unite(1, 2);
	EXPECT_EQ(uf.find(1), uf.find(2));
	
	uf.unite(2, 1);
	EXPECT_EQ(uf.find(1), uf.find(2));

	uf.unite(3, 4);
	uf.unite(4, 5);
	EXPECT_EQ(uf.find(3), uf.find(5));

	EXPECT_NE(1, 3);
	uf.unite(1, 3);
	EXPECT_EQ(uf.find(1), uf.find(3));
}

TEST(GraphContraction, MergeConnections) {
	std::vector<Conn> a;
	a.emplace_back( 0, 1.0f);
	a.emplace_back( 2, 1.0f);
	a.emplace_back( 3, 1.0f);
	a.emplace_back(10, 1.0f);
	a.emplace_back(11, 1.0f);

	std::vector<Conn> b;
	b.emplace_back( 1, 1.0f);
	b.emplace_back(10, 1.0f);

	auto res = mergeConnections(a, b);

	ASSERT_NE(res.size(), 0);
	for (size_t i = 1; i < res.size(); i++) {
		EXPECT_LT(res[i-1].index, res[i].index);
	}
	ASSERT_EQ(res.size(), 6);
	EXPECT_EQ(res[4].index, 10);
	EXPECT_EQ(res[4].weight, 2.0f);
}

TEST(GraphContraction, RemoveSelfLoopsTrivial) {
	UnionFind uf(10);

	std::vector<Conn> adj;
	adj.emplace_back(0, 1.0f);
	adj.emplace_back(1, 1.0f);
	adj.emplace_back(1, 1.0f);
	adj.emplace_back(2, 1.0f);
	
	auto res = removeSelfLoops(1, adj, &uf);

	ASSERT_EQ(res.size(), 2);
	EXPECT_EQ(res[0].index, 0);
	EXPECT_EQ(res[1].index, 2);
}

TEST(GraphContraction, RemoveSelfLoopsWithUnions) {
	UnionFind uf(10);
	uf.unite(1, 5);
	uf.unite(5, 6);

	std::vector<Conn> adj;
	adj.emplace_back(0, 1.0f);
	adj.emplace_back(2, 1.0f);
	adj.emplace_back(6, 1.0f);
	
	auto res = removeSelfLoops(1, adj, &uf);

	ASSERT_EQ(res.size(), 2);
	EXPECT_EQ(res[0].index, 0);
	EXPECT_EQ(res[1].index, 2);
}

#if 0
TEST(GraphContraction, ComputeContraction) {
	auto res = computeContraction();
}
#endif

