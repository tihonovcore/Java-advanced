package ru.ifmo.rain.tihonov.mapper;

import info.kgeorgiy.java.advanced.mapper.ParallelMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class ParallelMapperImpl implements ParallelMapper {
    private List<Thread> workers = new ArrayList<>();

    public ParallelMapperImpl(int threads) {
        for (int i = 0; i < threads; i++) {
            workers.add(new Thread(() -> {

            }));
        }
    }

    @Override
    public <T, R> List<R> map(Function<? super T, ? extends R> function, List<? extends T> list) throws InterruptedException {
        return null;
    }

    @Override
    public void close() {

    }
}
