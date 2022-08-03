package pl.edu.icm.em.common;

import net.snowyhollows.bento.config.WorkDir;

import java.io.BufferedOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;

public class DebugTextFile implements Closeable {
    final PrintWriter printWriter;

    public DebugTextFile(PrintWriter printWriter) {
        this.printWriter = printWriter;
    }

    public void println(String x) {
        printWriter.println(x);
    }

    public void println() {
        printWriter.println();
    }

    public void printf(String x, Object... args) {
        printWriter.printf(x, args);
    }

    public void printlnf(String x, Object... args) {
        printWriter.printf(x, args);
        printWriter.println();
    }

    public void print(int i) {
        printWriter.print(i);
    }

    @Override
    public void close() throws IOException {
        printWriter.close();
    }

    public static DebugTextFile create(WorkDir workDir, String path) {
        return create(workDir, path, 1024);
    }

    public static DebugTextFile create(WorkDir workDir, String path, int bufferSize) {
        return new DebugTextFile(
                new PrintWriter(
                        new OutputStreamWriter(
                                new BufferedOutputStream(workDir.openForWriting(new File(path)), bufferSize),
                                StandardCharsets.UTF_8)));
    }

    public void flush() {
        printWriter.flush();
    }
}
