package pl.edu.icm.em.common;


import net.snowyhollows.bento.annotation.WithFactory;
import net.snowyhollows.bento.config.WorkDir;

public class DebugTextFileService {
    private final WorkDir workDir;

    @WithFactory
    public DebugTextFileService(WorkDir workDir) {
        this.workDir = workDir;
    }

    public DebugTextFile createTextFile(String path) {
        return DebugTextFile.create(workDir, path);
    }

    public DebugTextFile createTextFile(String path, int bufferSize) {
        return DebugTextFile.create(workDir, path, bufferSize);
    }
}
