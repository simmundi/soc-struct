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
