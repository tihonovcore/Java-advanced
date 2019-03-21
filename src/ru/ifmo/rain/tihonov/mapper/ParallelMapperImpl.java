package ru.ifmo.rain.tihonov.mapper;

import info.kgeorgiy.java.advanced.mapper.ParallelMapper;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.function.Function;

import static java.lang.Thread.sleep;

public class ParallelMapperImpl implements ParallelMapper {
    private final List<Thread> workers;
    private final Queue<Runnable> tasks;

    public ParallelMapperImpl(int threads) {
        workers = new ArrayList<>();
        tasks = new ArrayDeque<>();

        for (int i = 0; i < threads; i++) {
            workers.add(new Thread(() -> {
                try {
                    while (true) {
                        solve();
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }));
            workers.get(i).start();
        }
    }

    private void solve() throws InterruptedException {
        Runnable runnable;
        synchronized (tasks) {
            while (tasks.isEmpty()) {
                tasks.wait();
            }
            runnable = tasks.poll();
            tasks.notify();
        }
        runnable.run();
    }

    @Override
    public <T, R> List<R> map(Function<? super T, ? extends R> function, List<? extends T> list) throws InterruptedException {
        final List<R> result = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            result.add(null);

            final int index = i;
            add(() -> result.set(index, function.apply(list.get(index))));
        }

//        todo wait()
        sleep(3000);
        return result;
    }

    private void add(Runnable runnable) {
        synchronized (tasks) {
            tasks.add(runnable);
            tasks.notify();
        }
    }

    @Override
    public void close() {
        //todo
    }
}
