package ru.ifmo.rain.tihonov.crawler;

import info.kgeorgiy.java.advanced.crawler.CachingDownloader;
import info.kgeorgiy.java.advanced.crawler.Crawler;
import info.kgeorgiy.java.advanced.crawler.Downloader;
import info.kgeorgiy.java.advanced.crawler.Result;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;

/**
 * Implementation of {@link Crawler}
 */
public class WebCrawler implements Crawler {
    private Counter counter = new Counter();

    private Downloader downloader;

    private ThreadPoolExecutor loader;
    private ThreadPoolExecutor extractor;

    private final static int capacity = 10000;
    private final static int maxPoolSize = 5000;

    /**
     * @param downloader  {@link Downloader} for loading pages
     * @param downloaders max number parallel downloads pages
     * @param extractors  max number parallel extractors pages
     * @param perHost     max number parallel calls host
     */
    public WebCrawler(Downloader downloader, int downloaders, int extractors, int perHost) {
        this.downloader = downloader;

        loader = newThreadPoolExecutor(Integer.min(downloaders, maxPoolSize));
        extractor = newThreadPoolExecutor(Integer.min(extractors, maxPoolSize));
    }

    private BlockingQueue<Pair> documents = new ArrayBlockingQueue<>(capacity);
    private final BlockingQueue<String> queue = new ArrayBlockingQueue<>(capacity);

    /**
     * Start recursive download from {@code url} to depth equals {@code depth}
     *
     * @param url   is page which recursive walk start from
     * @param depth is walk's depth
     * @return {@link Result} with {@link List} of saved pages and {@link Map} contained {@link IOException}
     */
    @Override
    public Result download(String url, int depth) {
        List<String> result = new ArrayList<>();

        Map<String, Boolean> downloaded = new ConcurrentHashMap<>();
        Map<String, IOException> exceptions = new ConcurrentHashMap<>();
        Map<String, Integer> distance = new ConcurrentHashMap<>();

        distance.put(url, 0);
        queue.add(url);

        do {
            String curr = get();
            if (curr == null) continue;

            if (!downloaded.containsKey(curr) && distance.get(curr) <= depth) {
                downloaded.put(curr, true);

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

            Pair pair = documents.poll();
            while (pair == null) {
                if (loader.getActiveCount() == 0 && documents.isEmpty()) {
                    counter.finish();
                    return;
                }
                pair = documents.poll();
            }

            String parentLink = pair.string;

            try {
                list = pair.document.extractLinks();
            } catch (IOException e) {
                setException(exceptions, parentLink, e);
            }

            if (distance.get(parentLink) + 1 < depth) {
                for (String extractedLink : list) {
                    if (!distance.containsKey(extractedLink)) {
                        distance.put(extractedLink, distance.get(parentLink) + 1);
                        queue.add(extractedLink);
                    }
                }
            }

            if (!exceptions.containsKey(parentLink)) {
                synchronized (result) {
                    result.add(parentLink);
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

    /**
     * Stop all threads
     */
    @Override
    public void close() {
        loader.shutdownNow();
        extractor.shutdownNow();
    }

    /**
     * Start downloading pages by {@link WebCrawler} if arguments are correct
     *
     * @param args there may be 5 or less values: url [depth [downloads [extractors [perHost]]]]
     *             {@code url} is page which recursive walk start from
     *             {@code depth} is walk's depth
     *             {@code downloads} max number parallel downloads pages
     *             {@code extractors} max number parallel extractors pages
     *             {@code perHost} max number parallel calls host
     */
    public static void main(String[] args) {
        if (args == null) {
            error("args[] can not be null");
            return;
        }

        if (Arrays.stream(args).anyMatch(Objects::isNull)) {
            error("arguments can not be null");
        }

        String url = args[0];

        int[] arguments = new int[4];
        for (int i = 0; i < 4; i++) {
            try {
                arguments[i] = i < args.length ? Integer.parseInt(args[i]) : 1;
            } catch (NumberFormatException e) {
                error(args[i] + " isn't correct number");
            }
        }

        try {
            WebCrawler crawler = new WebCrawler(new CachingDownloader(), arguments[1], arguments[2], arguments[3]);
            crawler.download(url, arguments[0]);
        } catch (IOException e) {
            error("error occurred while downloading");
        }
    }

    private static void error(String message) {
        System.err.println(message);
    }

    private ThreadPoolExecutor newThreadPoolExecutor(int threadPoolSize) {
        return new ThreadPoolExecutor(threadPoolSize, threadPoolSize, 1L, TimeUnit.SECONDS, new ArrayBlockingQueue<>(capacity));
    }
}
