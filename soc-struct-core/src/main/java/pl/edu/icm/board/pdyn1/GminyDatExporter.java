package pl.edu.icm.board.pdyn1;

import net.snowyhollows.bento.annotation.WithFactory;
import net.snowyhollows.bento.config.WorkDir;
import pl.edu.icm.board.geography.KilometerGridCell;
import pl.edu.icm.board.geography.commune.CommuneManager;
import pl.edu.icm.em.common.DebugTextFile;
import pl.edu.icm.em.common.DebugTextFileService;

import java.io.IOException;

/**
 * GminyDatExporter reads data back from the CommuneManager
 * and produces a textfile in pdyn1 format.
 * The file associates each point in a kilometer grid with 7-digit teryt number
 * (or seven zeros, if the cell does not belong to any commune)
 */
public class GminyDatExporter {
    private final WorkDir workDir;
    private final CommuneManager communeManager;
    private final DebugTextFileService debugTextFileService;



    @WithFactory
    public GminyDatExporter(CommuneManager communeManager, WorkDir workDir, DebugTextFileService debugTextFileService) {
        this.communeManager = communeManager;
        this.workDir = workDir;
        this.debugTextFileService = debugTextFileService;
    }

    public void saveGridToFile(String path) throws IOException {
        int rows = communeManager.getGridRows();;
        int cols = communeManager.getGridCols();
        DebugTextFile textFile = debugTextFileService.createTextFile(path, 64 * 1024);
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
