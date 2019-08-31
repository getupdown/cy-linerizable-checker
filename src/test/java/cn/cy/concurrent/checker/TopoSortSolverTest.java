package cn.cy.concurrent.checker;

import org.junit.Test;

public class TopoSortSolverTest {

    @Test
    public void testEnumerate() {
        TopoSortSolver topoSortSolver = new TopoSortSolver(4);

        topoSortSolver.addEdge(1, 2);

        topoSortSolver.enumerateAllTopoSort();
    }
}