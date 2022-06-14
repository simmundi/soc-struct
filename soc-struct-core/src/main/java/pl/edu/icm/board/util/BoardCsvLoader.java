package pl.edu.icm.board.util;

import com.univocity.parsers.common.record.Record;
import com.univocity.parsers.csv.CsvParser;
import com.univocity.parsers.csv.CsvParserSettings;
import net.snowyhollows.bento2.annotation.WithFactory;

import java.io.File;
import java.io.InputStream;

/**
 * Wrapper around csv loading functionality (eases mocking etc.)
 */
public class BoardCsvLoader {

    @WithFactory
    public BoardCsvLoader() {
    }

    public Iterable<Record> stream(String filename) {
        CsvParserSettings settings = new CsvParserSettings();
        settings.setHeaderExtractionEnabled(true);
        settings.setDelimiterDetectionEnabled(true);
        return new CsvParser(settings).iterateRecords(new File(filename));
    }
}
