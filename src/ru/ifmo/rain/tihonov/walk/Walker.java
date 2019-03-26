package ru.ifmo.rain.tihonov.walk;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import static java.nio.file.FileVisitResult.CONTINUE;
import static ru.ifmo.rain.tihonov.tools.Logger.error;

public class Walker extends SimpleFileVisitor<Path> {
    private static final int FNV_32_PRIME = 0x01000193;
    private static final int HASH_32_INIT = 0x811c9dc5;

    private BufferedWriter writer;

    Walker(BufferedWriter writer) {
        this.writer = writer;
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
        int hash = HASH_32_INIT;
        try (InputStream read = Files.newInputStream(file)) {
            int c;
            byte[] b = new byte[1024];
            while ((c = read.read(b)) >= 0) {
                for (int i = 0; i < c; i++) {
                    hash = (hash * FNV_32_PRIME);
                    hash ^= (b[i] & 0xff);
                }
            }
        } catch (IOException | InvalidPathException e) {
            hash = 0;
        } finally {
            write(hash, file.toString());
        }

        return CONTINUE;
    }

    @Override
    public FileVisitResult visitFileFailed(Path file, IOException exc) {
        return write(0, file.toString());
    }

    private FileVisitResult write(int hash, String path) {
        try {
            writer.write(String.format("%08x", hash) + " " + path);
            writer.newLine();
        } catch (InvalidPathException e) {
            error("Invalid path: " + e.getMessage());
        } catch (IOException e) {
            error("Output exception: " + e.getMessage());
        }

        return CONTINUE;
    }
}
