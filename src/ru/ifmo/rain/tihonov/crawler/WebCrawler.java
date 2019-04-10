package ru.ifmo.rain.tihonov.crawler;

import info.kgeorgiy.java.advanced.crawler.*;

import java.io.IOException;
import java.util.*;

public class WebCrawler implements Crawler {
    private Downloader downloader;
    private int downloaders;
    private int extractors;
    private int perHost;

    public WebCrawler(Downloader downloader, int downloaders, int extractors, int perHost) {
        this.downloader = downloader;
        this.downloaders = downloaders;
        this.extractors = extractors;
        this.perHost = perHost;
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

            if (urls.get(curr) + 1 <= depth) {
                try {
                    for (String currentLink : downloader.download(curr).extractLinks()) {
                        if (!urls.containsKey(currentLink)) {
                            queue.add(currentLink);
                            urls.put(currentLink, urls.get(curr) + 1);
                        }
                    }

                    result.add(curr);
                } catch (IOException e) {
                    if (exceptions.containsKey(curr)) {
                        exceptions.get(curr).addSuppressed(e);
                    } else {
                        exceptions.put(curr, e);
                    }
                }

            }
        }

        return new Result(result, exceptions);
    }

    @Override
    public void close() {

    }
}
