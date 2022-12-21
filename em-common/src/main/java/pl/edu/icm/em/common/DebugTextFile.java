/*
 * Copyright (c) 2022 ICM Epidemiological Model Team at Interdisciplinary Centre for Mathematical and Computational Modelling, University of Warsaw.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *
 */

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
