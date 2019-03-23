package ru.ifmo.rain.tihonov.mapper;

import info.kgeorgiy.java.advanced.mapper.ParallelMapper;

import java.util.*;
import java.util.function.Function;

import static java.lang.Thread.interrupted;

/**
 * {@link ParallelMapper} implementation
 */
public class ParallelMapperImpl implements ParallelMapper {
    private final List<Thread> workers;
    private final Queue<Task> tasks;

    private class Task {
        Runnable task;
        Runnable increment;

        Task(Runnable task, Runnable increment) {
            this.task = task;
            this.increment = increment;
        }
    }

    /**
     * Create {@code threads} threads.
     *
     * @param threads number of creating threads
     */
    public ParallelMapperImpl(int threads) {
        workers = new ArrayList<>();
        tasks = new ArrayDeque<>();

        for (int i = 0; i < threads; i++) {
            workers.add(new Thread(() -> {
                try {
                    while (!interrupted()) {
                        solve();
                    }
                } catch (InterruptedException ignored) {
                }
            }));
            workers.get(i).start();
        }
    }

    private void solve() throws InterruptedException {
        Task task;
        synchronized (tasks) {
            while (tasks.isEmpty()) {
                tasks.wait();
            }
            task = tasks.poll();
        }

        task.task.run();
        task.increment.run();
    }

    /**
     * Evaluate {@code function} for each element of {@code list}.
     *
     * @param function {@link Function} for evaluating
     * @param list {@link List} for applying {@code function}
     * @param <T> type {@code list} elements
     * @param <R> type of {@code function} result
     * @return {@code list} with applied {@code function} for each element
     * @throws InterruptedException if threads error happened
     */
    @Override
    public <T, R> List<R> map(Function<? super T, ? extends R> function, List<? extends T> list) throws InterruptedException {
        final ConcurrentList<R> result = new ConcurrentList<>(Collections.nCopies(list.size(), null));
        for (int i = 0; i < list.size(); i++) {
            final int index = i;
            add(() -> result.set(index, function.apply(list.get(index))), result::increment);
        }

        return result.getList();
    }

    private void add(Runnable runnable, Runnable increment) {
        synchronized (tasks) {
            tasks.add(new Task(runnable, increment));
            tasks.notify();
        }
    }

    /**
     * Stop all threads
     */
    @Override
    public void close() {
        for (Thread t : workers) {
            t.interrupt();
            try {
                t.join();
            } catch (InterruptedException ignored) {
            }
        }
    }
}
