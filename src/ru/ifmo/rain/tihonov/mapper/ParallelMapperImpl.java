package ru.ifmo.rain.tihonov.mapper;

import info.kgeorgiy.java.advanced.mapper.ParallelMapper;

import java.util.*;
import java.util.function.Function;

import static java.lang.Thread.interrupted;

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
            tasks.notify();
        }

        task.task.run();
        task.increment.run();
    }

    @Override
    public <T, R> List<R> map(Function<? super T, ? extends R> function, List<? extends T> list) throws InterruptedException {
        final ConcurrentList<R> result = new ConcurrentList<>(Collections.nCopies(list.size(), null));
        for (int i = 0; i < list.size(); i++) {
            final int index = i;
            add(() -> result.set(index, function.apply(list.get(index))), result::increment);
        }

        return result.getList();
    }

    private <T> void add(Runnable runnable, Runnable increment) {
        synchronized (tasks) {
            tasks.add(new Task(runnable, increment));
            tasks.notify();
        }
    }

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
