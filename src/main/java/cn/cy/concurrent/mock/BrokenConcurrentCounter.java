package cn.cy.concurrent.mock;

import cn.cy.concurrent.checker.TopoSortBasedChecker;

/**
 * 错误的并发计数器
 */
public class BrokenConcurrentCounter {

    private TopoSortBasedChecker topoSortBasedChecker;

    public BrokenConcurrentCounter(TopoSortBasedChecker topoSortBasedChecker) {
        this.topoSortBasedChecker = topoSortBasedChecker;
    }

    private volatile Integer cnt = 0;

    public void add() {
        topoSortBasedChecker.startRecord("add", new Object[] {});
        cnt++;
        topoSortBasedChecker.endRecord(null);
    }

    public int get() {
        int res = 0;
        try {
            topoSortBasedChecker.startRecord("get", new Object[] {});
            res = cnt;
            return res;
        } finally {
            topoSortBasedChecker.endRecord(res);
        }
    }

    public boolean checkRes() {
        return topoSortBasedChecker.check();
    }
}