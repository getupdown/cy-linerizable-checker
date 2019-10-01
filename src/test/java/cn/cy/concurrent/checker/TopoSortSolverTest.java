package cn.cy.concurrent.checker;

import java.util.List;

import org.junit.Test;

import cn.cy.concurrent.checker.topo.TopoSortSolver;

public class TopoSortSolverTest {

    @Test
    public void testEnumerate() {
        TopoSortSolver topoSortSolver = new TopoSortSolver(12);

        topoSortSolver.addEdge(0, 1);
        topoSortSolver.addEdge(0, 2);
        topoSortSolver.addEdge(1, 4);
        topoSortSolver.addEdge(2, 4);
        topoSortSolver.addEdge(1, 5);
        topoSortSolver.addEdge(1, 3);
        topoSortSolver.addEdge(5, 6);
        topoSortSolver.addEdge(2, 6);
        topoSortSolver.addEdge(4, 6);

        List<List<Integer>> res = topoSortSolver.enumerateAllTopoSort();

        //        for (List<Integer> re : res) {
        //            for (Integer integer : re) {
        //                System.out.print(integer + 1);
        //                System.out.print(" ");
        //            }
        //            System.out.println();
        //        }

        System.out.println(res.size());
    }
}