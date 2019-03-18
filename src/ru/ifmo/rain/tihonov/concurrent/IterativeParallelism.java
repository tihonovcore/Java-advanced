package ru.ifmo.rain.tihonov.concurrent;

import info.kgeorgiy.java.advanced.concurrent.ScalarIP;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Contains methods for parallel evaluating
 */
public class IterativeParallelism implements ScalarIP {
    /**
     * Default constructor
     */
    public IterativeParallelism() {
    }

    private <T> List<List<? extends T>> getSublists(int amount, List<? extends T> list) {
        List<List<? extends T>> result = new ArrayList<>();

        if (amount <= 0) {
            throw new IllegalArgumentException("Count of thread should be >= 1");
        }

        int size = Math.min(amount, list.size());
        int blockSize = list.size() / size;

        int from = 0, to = blockSize;
        for (int i = 0; i < size; i++) {
            if (i < list.size() % blockSize) {
                to++;
            }
            result.add(list.subList(from, to));
            from = to;
            to = Math.min(list.size(), to + blockSize);
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
            workers.get(i).start();
        }

        for (Thread w : workers) {
            w.join();
        }

        return result;
    }

    private <T> Function<List<? extends T>, ? extends T> getMax(Comparator<? super T> comparator) {
        return (l) -> {
            T res = l.get(0);
            for (var x : l) {
                if (comparator.compare(res, x) < 0) {
                    res = x;
                }
            }
            return res;
        };
    }

    /**
     * Find and return maximum element by comparator
     *
     * @param threads    number or concurrent threads.
     * @param list       list elements for checking.
     * @param comparator value comparator.
     * @param <T>        type of elements.
     * @return maximum element
     * @throws InterruptedException if threads error happened
     */
    @Override
    public <T> T maximum(int threads, List<? extends T> list, Comparator<? super T> comparator) throws InterruptedException {
        Function<List<? extends T>, ? extends T> max = (l) -> list.stream().max(comparator).orElse(null);

        return result(threads, list, max).stream().max(comparator).orElse(null);
    }

    /**
     * Find and return minimum element by comparator
     *
     * @param threads    number or concurrent threads.
     * @param list       list elements for checking.
     * @param comparator value comparator.
     * @param <T>        type of elements.
     * @return minimum element
     * @throws InterruptedException if threads error happened
     */
    @Override
    public <T> T minimum(int threads, List<? extends T> list, Comparator<? super T> comparator) throws InterruptedException {
        Function<List<? extends T>, ? extends T> min = (l) -> list.stream().min(comparator).orElse(null);

        return result(threads, list, min).stream().min(comparator).orElse(null);
    }

    /**
     * Check all elements for matching predicate
     *
     * @param threads   number or concurrent threads.
     * @param list      {@link List} elements for checking
     * @param predicate test predicate.
     * @param <T>       type of elements
     * @return true if any element of list match test predicate, otherwise false
     * @throws InterruptedException if threads error happened
     */
    @Override
    public <T> boolean all(int threads, List<? extends T> list, Predicate<? super T> predicate) throws InterruptedException {
        Function<List<? extends T>, Boolean> filter = (l) -> list.stream().allMatch(predicate);

        return result(threads, list, filter).stream().allMatch(b -> b);
    }

    /**
     * Check elements for matching predicate
     *
     * @param threads   number or concurrent threads.
     * @param list      {@link List} elements for checking
     * @param predicate test predicate.
     * @param <T>       type of elements
     * @return true if any element of list match test predicate, otherwise false
     * @throws InterruptedException if threads error happened
     */
    @Override
    public <T> boolean any(int threads, List<? extends T> list, Predicate<? super T> predicate) throws InterruptedException {
        Function<List<? extends T>, Boolean> filter = (l) -> list.stream().anyMatch(predicate);

        return result(threads, list, filter).stream().anyMatch(b -> b);
    }
}
