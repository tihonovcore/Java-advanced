package ru.ifmo.rain.tihonov.crawler;

import info.kgeorgiy.java.advanced.crawler.Crawler;
import info.kgeorgiy.java.advanced.crawler.Downloader;
import info.kgeorgiy.java.advanced.crawler.Result;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;

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

public class WebCrawler implements Crawler {
    private Counter counter = new Counter();

    private Downloader downloader;

//    private Mapper loadMapper;
//    private Mapper extractMapper;

    private ThreadPoolExecutor loader;
    private ThreadPoolExecutor extractor;

    public WebCrawler(Downloader downloader, int downloaders, int extractors, int perHost) {
        this.downloader = downloader;
//        this.loadMapper = new Mapper(Integer.min(downloaders, 10)); //todo fix border
//        this.extractMapper = new Mapper(Integer.min(extractors, 10)); //todo add extractor

        loader = new ThreadPoolExecutor(10, Integer.min(downloaders, 100), 10L, TimeUnit.SECONDS, loadQueue);
//        extractor = new ThreadPoolExecutor(10, Integer.min(extractors, 100), 10L, TimeUnit.SECONDS, extractQueue);
    }

    private BlockingQueue<Runnable> loadQueue = new ArrayBlockingQueue<>(10000);
//    private BlockingQueue<Runnable> extractQueue = new ArrayBlockingQueue<>(10000);

    private final BlockingQueue<String> queue = new ArrayBlockingQueue<>(10000);

    @Override
    public Result download(String url, int depth) {

        Map<String, Boolean> donwloaded = new ConcurrentHashMap<>();
        Map<String, IOException> exceptions = new ConcurrentHashMap<>();

        List<String> result = new ArrayList<>();

        Map<String, Integer> distance = new ConcurrentHashMap<>();
        distance.put(url, 0);

        queue.add(url);

        do {
            String curr = get();
            if (curr == null) continue;

            //NOTE: if page donwloaded, but Runnable haven't finished, NPE happend
            if (!donwloaded.containsKey(curr) /*&& distance.get(curr) != null*/ && distance.get(curr) <= depth) {
                donwloaded.put(curr, true);

                Runnable load = () -> {
                    List<String> list = new ArrayList<>();
                    try {
                        list = downloader.download(curr).extractLinks();
                    } catch (IOException e) {
                        if (exceptions.containsKey(curr)) {
                            exceptions.get(curr).addSuppressed(e);
                        } else {
                            exceptions.put(curr, e);
                        }
                    }

                    if (distance.get(curr) + 1 < depth) {
                        for (String link : list) {
                            if (!distance.containsKey(link)) {
                                queue.add(link);
                                distance.put(link, distance.get(curr) + 1);
                            }
                        }
                    }

                    if (!exceptions.containsKey(curr)) {
                        synchronized (result) {
                            result.add(curr);
                        }
                    }

                    counter.finish();
                };

//                Runnable extract = () -> {
//
//                };

                counter.start();
                loader.execute(load);
            }
        } while (!queue.isEmpty() || counter.working());

        return new Result(result, exceptions);
    }

    private String get() {
        String result;
        do {
            result = queue.poll();
        } while (result == null && (!queue.isEmpty() || counter.working()));
        return result;
    }

    @Override
    public void close() {
        loader.shutdownNow();
    }
}
