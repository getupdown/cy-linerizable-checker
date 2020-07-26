package cn.cy.concurrent.executor;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.codahale.metrics.ConsoleReporter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;

/**
 * MonitoredThreadPoolExecutor
 */
public class MonitoredThreadPoolExecutor extends ThreadPoolExecutor {

    private MetricRegistry metrics;

    private ConsoleReporter reporter;

    private ThreadLocal<Timer.Context> metricTimerLocal;

    private Timer timer;

    public MonitoredThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit,
                                       BlockingQueue<Runnable> workQueue, ThreadFactory threadFactory,
                                       RejectedExecutionHandler handler) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory, handler);
        this.metrics = new MetricRegistry();
        this.reporter = ConsoleReporter.forRegistry(metrics)
                .convertRatesTo(TimeUnit.SECONDS)
                .convertDurationsTo(TimeUnit.MILLISECONDS)
                .build();
        this.reporter.start(1, TimeUnit.SECONDS);
        this.metricTimerLocal = new ThreadLocal<>();
        this.timer = metrics.timer(MetricRegistry.name(MonitoredThreadPoolExecutor.class, "worker"));
    }

    @Override
    protected void beforeExecute(Thread t, Runnable r) {
        Timer.Context context = timer.time();
        metricTimerLocal.set(context);
    }

    @Override
    protected void afterExecute(Runnable r, Throwable throwable) {
        Timer.Context context = metricTimerLocal.get();
        context.stop();
        metricTimerLocal.remove();
    }

    @Override
    protected void terminated() {
        System.out.println("thread pool termianted!");
    }
}
