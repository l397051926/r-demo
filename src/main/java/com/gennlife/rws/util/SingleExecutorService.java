package com.gennlife.rws.util;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class SingleExecutorService {

    private volatile static SingleExecutorService instance;

    private final ExecutorService searchUqlExecutorService = Executors.newFixedThreadPool(100,new TestThreadFactory("searchUqlExecutorService"));
    private final ExecutorService cortrastiveAnalysisExecutor = Executors.newFixedThreadPool(8,new TestThreadFactory("cortrastiveAnalysisExecutor"));
    private final ExecutorService referenceActiveExecutor = Executors.newFixedThreadPool(100,new TestThreadFactory("referenceActiveExecutor"));
    private final ExecutorService flushCountGroupExecutor = Executors.newFixedThreadPool(20,new TestThreadFactory("flushCountGroupExecutor"));
    private final ExecutorService centerTaskeExecutor = Executors.newFixedThreadPool(10,new TestThreadFactory("centerTaskeExecutor"));

    private SingleExecutorService(){}

    public static SingleExecutorService getInstance(){
        if(instance == null){
            synchronized (SingleExecutorService.class){
                if(instance == null ){
                    instance = new SingleExecutorService();
                }
            }
        }
        return instance;
    }

    public ExecutorService getSearchUqlExecutorService() {
        return searchUqlExecutorService;
    }

    public ExecutorService getCortrastiveAnalysisExecutor() {
        return cortrastiveAnalysisExecutor;
    }

    public ExecutorService getReferenceActiveExecutor() {
        return referenceActiveExecutor;
    }

    public ExecutorService getFlushCountGroupExecutor() {
        return flushCountGroupExecutor;
    }

    public ExecutorService getCenterTaskeExecutor() {
        return centerTaskeExecutor;
    }

    static class TestThreadFactory implements ThreadFactory {
        private static final AtomicInteger poolNumber = new AtomicInteger(1);
        private final ThreadGroup group;
        private final AtomicInteger threadNumber = new AtomicInteger(1);
        private final String namePrefix;

        TestThreadFactory(String name) {
            SecurityManager s = System.getSecurityManager();
            group = (s != null) ? s.getThreadGroup() :
                Thread.currentThread().getThreadGroup();
            namePrefix = name + "-" +
                poolNumber.getAndIncrement() +
                "-thread-";
        }

        public Thread newThread(Runnable r) {
            Thread t = new Thread(group, r,
                namePrefix + threadNumber.getAndIncrement(),
                0);
            if (t.isDaemon())
                t.setDaemon(false);
            if (t.getPriority() != Thread.NORM_PRIORITY)
                t.setPriority(Thread.NORM_PRIORITY);
            return t;
        }
    }
}
