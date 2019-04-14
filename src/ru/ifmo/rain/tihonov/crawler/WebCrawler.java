package ru.ifmo.rain.tihonov.crawler;

import info.kgeorgiy.java.advanced.crawler.Crawler;
import info.kgeorgiy.java.advanced.crawler.Document;
import info.kgeorgiy.java.advanced.crawler.Downloader;
import info.kgeorgiy.java.advanced.crawler.Result;

import javax.print.Doc;
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

    private ThreadPoolExecutor loader;
    private ThreadPoolExecutor extractor;

    public WebCrawler(Downloader downloader, int downloaders, int extractors, int perHost) {
        this.downloader = downloader;
        int d = Integer.min(downloaders, 5000);
        int e = Integer.min(extractors, 5000);
        loader = new ThreadPoolExecutor(d, d, 1000L, TimeUnit.SECONDS, loadQueue);
        extractor = new ThreadPoolExecutor(e, e, 1000L, TimeUnit.SECONDS, extractQueue);
    }

    private BlockingQueue<Runnable> loadQueue = new ArrayBlockingQueue<>(10000);
    private BlockingQueue<Runnable> extractQueue = new ArrayBlockingQueue<>(10000);

    private BlockingQueue<Pair> documents = new ArrayBlockingQueue<>(10000);

    class Pair {
        Document document;
        String string;

        Pair(Document document, String string) {
            this.document = document;
            this.string = string;
        }
    }

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

            if (!donwloaded.containsKey(curr) && distance.get(curr) <= depth) {
                donwloaded.put(curr, true);

                Runnable extract = () -> {
                    List<String> list = new ArrayList<>();

                    Pair pair;
                    String s = "";
                    try {
                        while (true) {
                            pair = documents.poll();

                            if (pair != null && pair.document != null) {
                                s = pair.string;
                                break;
                            }
                            if (loader.getActiveCount() == 0 && documents.isEmpty()) {
                                counter.finish();
                                return;
                            }
                        }
                        list = pair.document.extractLinks();
                    } catch (IOException e) {
                        if (exceptions.containsKey(s)) {
                            exceptions.get(s).addSuppressed(e);
                        } else {
                            exceptions.put(s, e);
                        }
                    }

                    if (distance.get(s) + 1 < depth) {
                        for (String link : list) {
                            if (!distance.containsKey(link)) {
                                distance.put(link, distance.get(s) + 1);
                                queue.add(link);
                            }
                        }
                    }

                    if (!exceptions.containsKey(s)) {
                        synchronized (result) {
                            result.add(s);
                        }
                    }

                    counter.finish();
                };

                Runnable load = () -> {
                    try {
                        documents.add(new Pair(downloader.download(curr), curr));
                    } catch (IOException e) {
                        if (exceptions.containsKey(curr)) {
                            exceptions.get(curr).addSuppressed(e);
                        } else {
                            exceptions.put(curr, e);
                        }
                        counter.finish();
                        return;
                    }
                    extractor.execute(extract);
                };

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
        extractor.shutdownNow();
    }
}
