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

import net.snowyhollows.bento.annotation.WithFactory;
import pl.edu.icm.trurl.util.StatusListener;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

public class LoggingStatusListener implements StatusListener, AutoCloseable {
    private final DebugTextFile log;
    private final DebugTextFileService debugTextFileService;
    private final String rootPath;

    @WithFactory
    public LoggingStatusListener(DebugTextFileService debugTextFileService, String rootPath) {
        this.debugTextFileService = debugTextFileService;
        this.rootPath = rootPath;
        try {
            Files.createDirectories(Paths.get(rootPath, "output/logs"));
            log = debugTextFileService.createTextFile(
                    "output/logs/perf-"
                            + LocalDateTime
                            .now(ZoneOffset.UTC)
                            .format(DateTimeFormatter.ISO_DATE_TIME)
                            .replace(':','-')
                            + ".log");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void done(long millis, String text, String comment) {
        log.printlnf("%f,\"%s\",\"%s\",\"done\"", millis / 1000.0, text, comment != null ? comment : "-");
        log.flush();
    }

    @Override
    public void problem(String text, int count) {
        log.printlnf(",,\"%s\",problems,%d", text, count);
        log.flush();
    }

    public void close() throws IOException {
        log.close();
    }
}
