package pl.edu.icm.board.pdyn1;

import net.snowyhollows.bento2.annotation.WithFactory;
import pl.edu.icm.board.geography.KilometerGridCell;
import pl.edu.icm.board.geography.commune.CommuneManager;
import pl.edu.icm.trurl.util.DefaultFilesystem;
import pl.edu.icm.trurl.util.Filesystem;
import pl.edu.icm.trurl.util.TextFile;

import java.io.IOException;

/**
 * GminyDatExporter reads data back from the CommuneManager
 * and produces a textfile in pdyn1 format.
 * The file associates each point in a kilometer grid with 7-digit teryt number
 * (or seven zeros, if the cell does not belong to any commune)
 */
public class GminyDatExporter {
    private final Filesystem filesystem;
    private final CommuneManager communeManager;

    @WithFactory
    public GminyDatExporter(CommuneManager communeManager) {
        this(communeManager, new DefaultFilesystem());
    }

    public GminyDatExporter(CommuneManager communeManager, Filesystem filesystem) {
        this.communeManager = communeManager;
        this.filesystem = filesystem;
    }

    public void saveGridToFile(String path) throws IOException {
        int rows = communeManager.getGridRows();;
        int cols = communeManager.getGridCols();
        TextFile textFile = TextFile.create(filesystem, path, 64 * 1024);
        textFile.printf("%d %d\r\n", rows, cols);
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                textFile.printf("%s%s", col != 0 ? " " : "", communeManager.communeAt(KilometerGridCell.fromLegacyPdynCoordinates(col, row)).getTeryt());
            }
            textFile.printf("\r\n");
        }
        textFile.close();
    }
}
