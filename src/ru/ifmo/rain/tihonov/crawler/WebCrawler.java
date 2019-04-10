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

    private Map<String, Integer> urls;
    private Queue<String> queue = new ArrayDeque<>();

    @Override
    public Result download(String url, int depth) {
        urls = new HashMap<>();
        queue.add(url);
        urls.put(url, 0);

        try {
            return bfs(depth);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private Map<String, IOException> excp = new HashMap<>();
    private Result bfs(int depth) throws IOException {
        List<String> result = new ArrayList<>();

        while (!queue.isEmpty()) {
            String curr = queue.poll();

            if (urls.get(curr) + 1 <= depth) {
                Document document;
                try {
                    document = downloader.download(curr);
                } catch (IOException e) {
                    System.err.println(curr);
                    if (excp.containsKey(curr)) {
                        excp.get(curr).addSuppressed(e);
                    } else {
                        excp.put(curr, e);
                    }
                    continue;
                } catch (NullPointerException e) {
                    System.err.println(curr);
                    continue;
                }
                result.add(curr);
                for (String currentLink : document.extractLinks()) {
                    if (!urls.containsKey(currentLink)) {
                        queue.add(currentLink);
                        urls.put(currentLink, urls.get(curr) + 1);
                    }
                }
            }
        }

        return new Result(result, excp);
    }

    @Override
    public void close() {

    }
}
