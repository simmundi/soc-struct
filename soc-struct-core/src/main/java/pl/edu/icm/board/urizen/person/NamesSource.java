package pl.edu.icm.board.urizen.person;

import com.google.common.base.Charsets;
import com.univocity.parsers.csv.CsvParser;
import com.univocity.parsers.csv.CsvParserSettings;
import net.snowyhollows.bento2.annotation.WithFactory;
import pl.edu.icm.trurl.bin.BinPool;
import pl.edu.icm.trurl.util.DefaultFilesystem;
import pl.edu.icm.trurl.util.Filesystem;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

public class NamesSource {

    private final String namesFPath;
    private final String namesMPath;
    private final String surnamesPath;
    private final Filesystem filesystem;

    @WithFactory
    public NamesSource(
            String namesFPath,
            String namesMPath,
            String surnamesPath
    ) {
        this(namesFPath, namesMPath, surnamesPath, new DefaultFilesystem());
    }

    public NamesSource(
            String namesFPath,
            String namesMPath,
            String surnamesPath,
            Filesystem filesystem
    ) {
        this.namesFPath = namesFPath;
        this.namesMPath = namesMPath;
        this.surnamesPath = surnamesPath;
        this.filesystem = filesystem;
        load();
    }

    public NamePools load() {
        try {
            NamePools namePools = new NamePools();
            fill(namePools.maleNames, namesMPath, 0, 2);
            fill(namePools.femaleNames, namesFPath, 0, 2);
            fill(namePools.surnames, surnamesPath, 0, 1);
            return namePools;
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    private void fill(BinPool<String> binPool, String path, int labelColumn, int countColumn) throws FileNotFoundException {
        CsvParserSettings settings = new CsvParserSettings();
        settings.setHeaderExtractionEnabled(true);
        CsvParser csvParser = new CsvParser(settings);

        csvParser.iterateRecords(filesystem.openForReading(new File(path)), Charsets.UTF_8).forEach(record -> {
            binPool.add(record.getString(labelColumn), record.getInt(countColumn));
        });

    }
}
