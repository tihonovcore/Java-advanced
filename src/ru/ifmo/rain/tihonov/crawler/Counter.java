package ru.ifmo.rain.tihonov.crawler;

class Counter {
    private long started = 0;
    private long finished = 0;

    synchronized void start() {
        started++;
    }

    synchronized void finish() {
        finished++;
    }

    synchronized boolean working() {
        return started != finished;
    }
}
