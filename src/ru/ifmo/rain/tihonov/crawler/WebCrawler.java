package ru.ifmo.rain.tihonov.crawler;

import info.kgeorgiy.java.advanced.crawler.Crawler;
import info.kgeorgiy.java.advanced.crawler.Downloader;
import info.kgeorgiy.java.advanced.crawler.Result;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;

public class WebCrawler implements Crawler {
    private Counter counter = new Counter();

    private Downloader downloader;

    private ThreadPoolExecutor loader;
    private ThreadPoolExecutor extractor;

    private final static int capacity = 10000;
    private final static int maxPoolSize = 5000;

    public WebCrawler(Downloader downloader, int downloaders, int extractors, int perHost) {
        this.downloader = downloader;

        loader = newThreadPoolExecutor(Integer.min(downloaders, maxPoolSize));
        extractor = newThreadPoolExecutor(Integer.min(extractors, maxPoolSize));
    }

    private ThreadPoolExecutor newThreadPoolExecutor(int threadPoolSize) {
        return new ThreadPoolExecutor(threadPoolSize, threadPoolSize, 1L, TimeUnit.SECONDS, new ArrayBlockingQueue<>(capacity));
    }

    private BlockingQueue<Pair> documents = new ArrayBlockingQueue<>(capacity);
    private final BlockingQueue<String> queue = new ArrayBlockingQueue<>(capacity);

    @Override
    public Result download(String url, int depth) {
        List<String> result = new ArrayList<>();

        Map<String, Boolean> donwloaded = new ConcurrentHashMap<>();
        Map<String, IOException> exceptions = new ConcurrentHashMap<>();
        Map<String, Integer> distance = new ConcurrentHashMap<>();

        distance.put(url, 0);
        queue.add(url);

        do {
            String curr = get();
            if (curr == null) continue;

            if (!donwloaded.containsKey(curr) && distance.get(curr) <= depth) {
                donwloaded.put(curr, true);

                Runnable extract = getExtract(exceptions, distance, depth, result);

                Runnable load = getLoad(exceptions, curr, extract);

                counter.start();
                loader.execute(load);
            }
        } while (!queue.isEmpty() || counter.working());

        return new Result(result, exceptions);
    }

    private Runnable getExtract(Map<String, IOException> exceptions, Map<String, Integer> distance, int depth, List<String> result) {
        return () -> {
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
                setException(exceptions, s, e);
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
    }

    private Runnable getLoad(Map<String, IOException> exceptions, String curr, Runnable extract) {
        return () -> {
            try {
                documents.add(new Pair(downloader.download(curr), curr));
            } catch (IOException e) {
                setException(exceptions, curr, e);
                counter.finish();
                return;
            }
            extractor.execute(extract);
        };

    }

    private void setException(Map<String, IOException> exceptions, String curr, IOException e) {
        if (exceptions.containsKey(curr)) {
            exceptions.get(curr).addSuppressed(e);
        } else {
            exceptions.put(curr, e);
        }
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
