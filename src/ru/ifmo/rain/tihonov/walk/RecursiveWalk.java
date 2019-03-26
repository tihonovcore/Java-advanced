package ru.ifmo.rain.tihonov.walk;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.*;

import static ru.ifmo.rain.tihonov.tools.Logger.*;

public class RecursiveWalk {
    public static void main(String[] args) {
        if (args == null) {
            error("Array of arguments shouldn't be null");
            return;
        }

        if (args.length < 2) {
            error("Not enough arguments. Expected 2, found " + args.length);
        } else if (args.length > 2) {
            error("Too much arguments. Expected 2, found " + args.length);
        } else if (args[0] == null || args[1] == null) {
            error("Arguments shouldn't be null");
        }

        if (errorOccurred()) {
            return;
        }

        try {
            Path out = Paths.get(args[1]);
            if (out.getParent() != null) {
                Files.createDirectories(out.getParent());
            }
        } catch (InvalidPathException e) {
            error("Invalid path: " + e.getMessage());
        } catch (IOException e) {
            error("Error while creating directories: " + e.getMessage());
        }

        if (errorOccurred()) {
            return;
        }

        try (BufferedReader reader = Files.newBufferedReader(Paths.get(args[0]))) {
            try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(args[1]))) {
                String path;
                FileVisitor<Path> visitor = new Walker(writer);
                while ((path = reader.readLine()) != null) {
                    try {
                        Files.walkFileTree(Paths.get(path), visitor);
                    } catch (InvalidPathException e) {
                        writer.write("00000000 " + path);
                    }
                }
            } catch (IOException e) {
                error("Output exception: " + e.getMessage());
            }
        } catch (NoSuchFileException e) {
            error("File \"" + args[0] + "\" doesn't exist");
        } catch (IOException e) {
            error("Input exception: " + e.getMessage());
        }
    }
}
