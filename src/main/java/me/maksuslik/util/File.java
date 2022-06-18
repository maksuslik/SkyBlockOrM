package me.maksuslik.util;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Stream;

public class File {
    public static void appendLine(String line, String filename) throws IOException {
        try (PrintWriter output = new PrintWriter(new FileWriter(filename, true))) {
            output.printf("%s\r\n", line);
        }
    }

    public static Stream<String> readLines(String fileName) throws IOException {
        return Files.lines(Paths.get(fileName));
    }
}
