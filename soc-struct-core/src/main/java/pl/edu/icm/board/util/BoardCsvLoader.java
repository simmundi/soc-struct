package pl.edu.icm.board.util;

import com.univocity.parsers.common.record.Record;
import com.univocity.parsers.csv.CsvParser;
import com.univocity.parsers.csv.CsvParserSettings;
import net.snowyhollows.bento.annotation.WithFactory;
import net.snowyhollows.bento.config.WorkDir;

import java.io.File;

/**
 * Wrapper around csv loading functionality (eases mocking etc.)
 */
public class BoardCsvLoader {

    private final WorkDir workDir;

    @WithFactory
    public BoardCsvLoader(WorkDir workDir) {
        this.workDir = workDir;
    }

    public Iterable<Record> stream(String filename) {
        CsvParserSettings settings = new CsvParserSettings();
        settings.setHeaderExtractionEnabled(true);
        settings.setDelimiterDetectionEnabled(true);
        return new CsvParser(settings).iterateRecords(workDir.openForReading(new File(filename)));
    }
}
