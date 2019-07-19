package com.gennlife.rws.catche;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * @author lmx
 * @create 2019 16 11:53
 * @desc
 **/
public class CortrastiveCache {

    private static final Set<String> delProjectOrPatientSetTaskSet = new CopyOnWriteArraySet<>();

    public static Set<String> getDelProjectOrPatientSetTaskSet() {
        return delProjectOrPatientSetTaskSet;
    }

    public static void cleanRedisMap() {
        delProjectOrPatientSetTaskSet.clear();
    }

}
