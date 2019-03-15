package ru.ifmo.rain.tihonov.concurrent;

import info.kgeorgiy.java.advanced.concurrent.ScalarIP;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;

public class IterativeParallelism implements ScalarIP {
    private <T> List<List<? extends T>> getSublists(int amount, List<? extends T> list) {
        List<List<? extends T>> result = new ArrayList<>();

        int size = Math.min(amount, list.size());
        int blockSize = list.size() / size;

        for (int i = 0; i < size; i++) {
            result.add(list.subList(i * blockSize, (i + 1) * blockSize));
        }

        if (blockSize * result.size() < list.size()) {
            result.add(list.subList(blockSize * result.size(), list.size()));
        }
        return result;
    }

    private <T, R> List<R> result(int threads, List<? extends T> list, Function<List<? extends T>, R> func) throws InterruptedException {
        var lists = getSublists(threads, list);

        List<Thread> workers = new ArrayList<>();
        List<R> result = new ArrayList<>();
        for (int i = 0; i < lists.size(); i++) {
            result.add(null);
        }

        for (int i = 0; i < lists.size(); i++) {
            final List<? extends T> temp = lists.get(i);
            final int curr = i;
            workers.add(new Thread(() -> result.set(curr, func.apply(temp))));
        }

        for (Thread w : workers) {
            w.start();
            w.join();
        }

        return result;
    }

    @Override
    public <T> T maximum(int threads, List<? extends T> list, Comparator<? super T> comparator) throws InterruptedException {
        Function<List<? extends T>, ? extends T> getMax = (l) -> {
            T res = l.get(0);
            for (var x : l) {
                if (comparator.compare(res, x) < 0) {
                    res = x;
                }
            }
            return res;
        };

        return getMax.apply(result(threads, list, getMax));
    }

    @Override
    public <T> T minimum(int threads, List<? extends T> list, Comparator<? super T> comparator) throws InterruptedException {
        Function<List<? extends T>, ? extends T> getMin = (l) -> {
            T res = l.get(0);
            for (var x : l) {
                if (comparator.compare(res, x) > 0) {
                    res = x;
                }
            }
            return res;
        };

        return getMin.apply(result(threads, list, getMin));
    }

    @Override
    public <T> boolean all(int threads, List<? extends T> list, Predicate<? super T> predicate) throws InterruptedException {
        Function<List<? extends T>, Boolean> filter = (l) -> list.stream().allMatch(predicate);

        return result(threads, list, filter).stream().allMatch(b -> b);
    }

    @Override
    public <T> boolean any(int threads, List<? extends T> list, Predicate<? super T> predicate) throws InterruptedException {
        Function<List<? extends T>, Boolean> filter = (l) -> list.stream().anyMatch(predicate);

        return result(threads, list, filter).stream().allMatch(b -> b);

    }
}
