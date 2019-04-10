package ru.ifmo.rain.tihonov.crawler;

import java.util.*;
import java.util.function.Function;

import static java.lang.Thread.currentThread;
import static java.lang.Thread.interrupted;

public class Mapper implements AutoCloseable {
    private final List<Thread> workers;
    private final Queue<Runnable> tasks;

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
    }

    public <T, R> R map(Function<? super T, ? extends R> function, T value) throws InterruptedException {
        Task<R> result = new Task<>();
        add(() -> {
            result.result = function.apply(value);
            synchronized (result) {
                result.ready = true;
                result.notify();
            }
        });

        return result.getResult();
    }

    private void add(Runnable runnable) {
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
