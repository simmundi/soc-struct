package pl.edu.icm.board.util;

import net.snowyhollows.bento2.annotation.WithFactory;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

public class FileToStreamService {
    @WithFactory
    public FileToStreamService() {
    }

    public InputStream filename(String name) throws FileNotFoundException {
        return new FileInputStream(name);
    }
}
