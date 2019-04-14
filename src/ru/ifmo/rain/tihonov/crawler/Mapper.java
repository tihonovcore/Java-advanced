package ru.ifmo.rain.tihonov.crawler;

import java.util.*;
import java.util.function.Function;

import static java.lang.Thread.currentThread;
import static java.lang.Thread.interrupted;

public class Mapper implements AutoCloseable {
    private final List<Thread> workers;
    private final Queue<Runnable> tasks;

    private final Counter counter = new Counter();

    private class Counter {
        int started = 0, finished = 0;
    }

    private class Task<R> {
        public R result;
        public boolean ready = false;

        public R getResult() throws InterruptedException {
            synchronized (this) {
                while (!ready) {
                    wait();
                }
                notify();
            }
            return result;
        }
    }

    public boolean working() {
        boolean result;
        synchronized (counter) {
            result = counter.started != counter.finished;
            counter.notify();
        }
        return result;
    }

    /**
     * Create {@code threads} threads.
     *
     * @param threads number of creating threads
     */
    public Mapper(int threads) {
        workers = new ArrayList<>();
        tasks = new ArrayDeque<>();

        for (int i = 0; i < threads; i++) {
            workers.add(new Thread(() -> {
                try {
                    while (!interrupted()) {
                        solve();
                    }
                } catch (InterruptedException ignored) {
                    currentThread().interrupt();
                }
            }));
            workers.get(i).start();
        }
    }

    private void solve() throws InterruptedException {
        Runnable task;
        synchronized (tasks) {
            while (tasks.isEmpty()) {
                tasks.wait();
            }
            task = tasks.poll();
            tasks.notify();
        }
        task.run();

        synchronized (counter) {
            counter.finished++;
            counter.notify();
        }
    }

    public <T, R> void map(Function<? super T, ? extends R> function, T value) throws InterruptedException {
        Task<R> result = new Task<>();
        add(() -> {
            result.result = function.apply(value);
            synchronized (result) {
                result.ready = true;
                result.notify();
            }
        });
    }

    private void add(Runnable runnable) {
        synchronized (counter) {
            counter.started++;
            counter.notify();
        }
        synchronized (tasks) {
            tasks.add(runnable);
            tasks.notify();
        }
    }

    /**
     * Stop all threads
     */
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