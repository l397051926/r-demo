package com.gennlife.rws.util;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SingleExecutorService {

    private volatile static SingleExecutorService instance;

    private final ExecutorService searchUqlExecutorService = Executors.newFixedThreadPool(100);
    private final ExecutorService cortrastiveAnalysisExecutor = Executors.newFixedThreadPool(8);
    private final ExecutorService referenceActiveExecutor = Executors.newFixedThreadPool(100);
    private final ExecutorService flushCountGroupExecutor = Executors.newFixedThreadPool(20);
    private final ExecutorService centerTaskeExecutor = Executors.newFixedThreadPool(10);

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
}
