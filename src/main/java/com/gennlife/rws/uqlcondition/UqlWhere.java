package com.gennlife.rws.uqlcondition;

import com.gennlife.rws.util.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

public class UqlWhere implements Iterable<UqlWhereElem> {

    private boolean executed = false;
    private List<UqlWhereElem> elems = new ArrayList<>();

    public boolean addElem(UqlWhereElem e) {
        return elems.add(e);
    }

    public boolean addElems(Collection<? extends UqlWhereElem> es) {
        return elems.addAll(es);
    }

    public UqlWhereElem removeElem() {
        return elems.remove(elems.size() - 1);
    }

    public boolean needsOperator() {
        if (isEmpty()) {
            return false;
        }
        UqlWhereElem last = elems.get(elems.size() - 1);
        return !(last instanceof LiteralUqlWhereElem) || !((LiteralUqlWhereElem) last).value().trim().endsWith("(");
    }

    public boolean isEmpty() {
        return elems.isEmpty();
    }

    public void execute(ExecutorService es) throws InterruptedException, ExecutionException {
        List<Future> futures = elems.stream().map(e -> es.submit(e::execute)).collect(toList());
        for (Future future: futures) {
            future.get();
        }
        executed = true;
    }
    public void execute() {
        executed = true;
    }

    public boolean isExecuted() {
        return executed;
    }

    @Override
    public String toString() {
        if (!executed) {
            throw new RuntimeException(getClass().getSimpleName() + "未执行，无法转换为字符串");
        }
        return elems.stream().map(UqlWhereElem::toString).collect(joining(" "));
    }

    @NotNull
    @Override
    public Iterator<UqlWhereElem> iterator() {
        return elems.iterator();
    }

    public boolean isSameGroup(String targetGroup) {
        Set<String> set = new HashSet<>();
        Set<String> targetSet = new HashSet<>();
        targetSet.add(targetGroup);
        if("sub_inspection".equals(targetGroup)){
            targetSet.add("inspection_reports");
        }
        for (UqlWhereElem whereElem : elems){
            if (whereElem instanceof LiteralUqlWhereElem){
                continue;
            }else if(whereElem instanceof SimpleConditionUqlWhereElem){
                String visits = ((SimpleConditionUqlWhereElem) whereElem).getSourceTagName();
                String group = StringUtils.substringBefore(visits,".");
                if(!targetSet.contains(group)){
                    return false;
                }
                set.add(group);
            }else if(whereElem instanceof ReferenceConditionUqlWhereElem){
                String visits = ((ReferenceConditionUqlWhereElem) whereElem).getVisits();
                if(!targetSet.contains(visits)){
                    return false;
                }
                set.add(visits);
            }else {
                return false;
            }
        }
        return set.size() > 0;
    }

    public void deleteHasChild() {
        List<UqlWhereElem> newElems = new ArrayList<>();
        boolean isHasChild = false;
        for (UqlWhereElem whereElem : elems){
            if (whereElem instanceof LiteralUqlWhereElem){
                String value = ((LiteralUqlWhereElem) whereElem).value();
                if("haschild(".equals(value)){
                    isHasChild = true;
                    continue;
                }else if(")".equals(value) && isHasChild) {
                    isHasChild = false;
                    continue;
                }else {
                    newElems.add(whereElem);
                }
            }else {
                newElems.add(whereElem);
            }
        }
        elems = newElems;
    }
    public UqlWhereElem getLastElems(){
        return elems.get(elems.size()-1);
    }

    public String getLastElem() {
        return elems.get(elems.size()-1).toString();
    }
}
