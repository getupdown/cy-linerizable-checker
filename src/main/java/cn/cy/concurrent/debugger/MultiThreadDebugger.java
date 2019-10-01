package cn.cy.concurrent.debugger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author shixingying
 * @version 2019-09-27 00:15
 */
public class MultiThreadDebugger {

    private static Logger logger = LoggerFactory.getLogger(MultiThreadDebugger.class);

    private static Boolean globalSwitch = true;

    public static void log(String info, Object... args) {
        if (globalSwitch) {
            logger.info("Current Thread is : {}, " + info, Thread.currentThread().getId(), args);
        }
    }

    public static void error(String info, Object... args) {
        if (globalSwitch) {
            logger.error(" error! : Current Thread is : {}, " + info, Thread.currentThread().getId(), args);
        }
    }

}
