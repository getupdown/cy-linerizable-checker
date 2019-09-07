package cn.cy.concurrent.mock;

import cn.cy.concurrent.checker.TopoSortBasedChecker;

/**
 * 错误的并发计数器
 */
public class BrokenConcurrentCounter {

    private TopoSortBasedChecker topoSortBasedChecker;

    private Integer cnt = 0;

    public void add() {
        topoSortBasedChecker.startRecord("add", null);
        cnt++;
        topoSortBasedChecker.endRecord(Void.TYPE);
    }

    public int get() {
        try {
            topoSortBasedChecker.startRecord("get", null);
            return cnt;
        } finally {
            topoSortBasedChecker.endRecord(cnt);
        }
    }
}