package cn.cy.concurrent.checker;

import org.junit.Test;

import cn.cy.concurrent.checker.topo.TopoSortSolver;

public class TopoSortSolverTest {

    @Test
    public void testEnumerate() {
        TopoSortSolver topoSortSolver = new TopoSortSolver(6);

        topoSortSolver.addEdge(1, 2);
        topoSortSolver.addEdge(0, 3);
        topoSortSolver.addEdge(0, 5);
        topoSortSolver.addEdge(2, 4);

        topoSortSolver.enumerateAllTopoSort();
    }
}