package ru.ifmo.rain.tihonov.crawler;

import info.kgeorgiy.java.advanced.crawler.*;

import java.io.IOException;
import java.util.*;
import java.util.function.Function;

public class WebCrawler implements Crawler {
    private Downloader downloader;
    private Mapper mapper;

    public WebCrawler(Downloader downloader, int downloaders, int extractors, int perHost) {
        this.downloader = downloader;
        this.mapper = new Mapper(Integer.min(downloaders, 50)); //todo ??????
    }

    @Override
    public Result download(String url, int depth) {
        Map<String, IOException> exceptions = new HashMap<>();
        List<String> result = new ArrayList<>();

        Map<String, Integer> urls = new HashMap<>();
        urls.put(url, 0);
        Queue<String> queue = new ArrayDeque<>();
        queue.add(url);

        while (!queue.isEmpty()) {
            String curr = queue.poll();

            if (urls.get(curr) >= depth) {
                continue;
            }

            Function<String, List<String>> load = (String s) -> {
                List<String> list = new ArrayList<>();
                try {
                    list = downloader.download(s).extractLinks();
                } catch (IOException e) {
                    synchronized (exceptions) {
                        if (exceptions.containsKey(curr)) {
                            exceptions.get(curr).addSuppressed(e);
                        } else {
                            exceptions.put(curr, e);
                        }
                    }
                }
                return list;
            };

            try {
                for (String currentLink : mapper.map(load, curr)) {
                    if (!urls.containsKey(currentLink)) {
                        queue.add(currentLink);
                        urls.put(currentLink, urls.get(curr) + 1);
                    }
                }
            } catch (InterruptedException ignored) {
            }

            if (!exceptions.containsKey(curr)) {
                result.add(curr);
            }
        }

        return new Result(result, exceptions);
    }

    @Override
    public void close() {
        mapper.close();
    }
}
