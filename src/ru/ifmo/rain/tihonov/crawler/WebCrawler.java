package ru.ifmo.rain.tihonov.crawler;

import info.kgeorgiy.java.advanced.crawler.Crawler;
import info.kgeorgiy.java.advanced.crawler.Document;
import info.kgeorgiy.java.advanced.crawler.Downloader;
import info.kgeorgiy.java.advanced.crawler.Result;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

public class WebCrawler implements Crawler {
    private Downloader downloader;

    private Mapper loadMapper;
    private Mapper extractMapper;

    public WebCrawler(Downloader downloader, int downloaders, int extractors, int perHost) {
        this.downloader = downloader;
        this.loadMapper = new Mapper(Integer.min(downloaders, 10)); //todo fix border
        this.extractMapper = new Mapper(Integer.min(extractors, 10)); //todo add extractor
    }

    private final BlockingQueue<String> queue = new ArrayBlockingQueue<>(10000);

    @Override
    public Result download(String url, int depth) {
//        BlockingQueue<String> loadQueue = new ArrayBlockingQueue<>(100);

        Map<String, Boolean> donwloaded = new ConcurrentHashMap<>();
        Map<String, IOException> exceptions = new ConcurrentHashMap<>();

        List<String> result = new ArrayList<>();

        Map<String, Integer> distance = new ConcurrentHashMap<>();
        distance.put(url, 0);

        queue.add(url);

        Function<String, Void> load = s -> {
            List<String> list = new ArrayList<>();
            try {
                list = downloader.download(s).extractLinks();
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
                        queue.add(link);
                        distance.put(link, distance.get(s) + 1);
                    }
                }
            }

            if (!exceptions.containsKey(s)) {
                synchronized (result) {
                    result.add(s);
                }
            }

            return null;
        };

        boolean flag = true;
        while (flag || loadMapper.working()) {
            flag = false;
            try {
                String curr = get();
                if (curr == null) continue;

                if (!donwloaded.containsKey(curr) && distance.get(curr) <= depth) {
                    donwloaded.put(curr, true);
                    loadMapper.map(load, curr);
                }
            } catch (InterruptedException ignored) {
            }

        }
        return new Result(result, exceptions);
    }

    private String get() {
        String result;
        do {
            result = queue.poll();
        } while (result == null && (queue.size() != 0 || loadMapper.working()));
        return result;
    }

    @Override
    public void close() {
        loadMapper.close();
        extractMapper.close();
    }
}
