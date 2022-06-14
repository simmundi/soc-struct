package pl.edu.icm.board.pdyn1;

import net.snowyhollows.bento2.annotation.WithFactory;
import pl.edu.icm.trurl.util.TextFile;

import java.io.FileNotFoundException;

public class DatFileCreator {

    @WithFactory
    public DatFileCreator() {
    }

    public TextFile create(String path) throws FileNotFoundException {
        return TextFile.create(path);
    }
}
