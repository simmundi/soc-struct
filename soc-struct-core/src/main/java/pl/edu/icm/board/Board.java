package pl.edu.icm.board;

import net.snowyhollows.bento2.annotation.WithFactory;
import pl.edu.icm.trurl.csv.CsvReader;
import pl.edu.icm.trurl.csv.CsvWriter;
import pl.edu.icm.trurl.ecs.Engine;
import pl.edu.icm.trurl.ecs.EngineConfiguration;
import pl.edu.icm.trurl.ecs.EngineCreationListener;
import pl.edu.icm.trurl.io.orc.OrcStoreService;
import pl.edu.icm.trurl.util.Status;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

public class Board  {
    private OrcStoreService lazyOrcStoreService;
    private final EngineConfiguration engineConfiguration;
    private final CsvWriter csvWriter;

    @WithFactory
    public Board(EngineConfiguration engineConfiguration, CsvWriter csvWriter) {
        this.engineConfiguration = engineConfiguration;
        this.csvWriter = csvWriter;
    }

    public Engine getEngine() {
        return engineConfiguration.getEngine();
    }

    public void load(InputStream inputStream, Class... components) throws IOException {
        load(inputStream, "[" + inputStream + "]", components);
    }

    public void load(String fileName, Class... components) throws IOException {
        load(new FileInputStream(fileName), fileName, components);
    }

    public void loadOrc(String fileName, Class... components) throws IOException {
        loadOrc(fileName, fileName, components);
    }

    public void loadOrc(String inputFile, String sourceForStatus, Class... components) throws IOException {
        require(components);
        Status sts = Status.of("Loading board from " + sourceForStatus);
        Engine engine = getEngine();
        getLazyOrcStoreService().read(engine.getComponentStore(), inputFile);
        sts.done();
    }

    public void saveOrc(String outputPath) throws IOException {
        Status sts = Status.of("Saving population data to " + outputPath);
        getEngine().getComponentStore().fireUnderlyingDataChanged(0, getEngine().getCount(), getEngine());
        getLazyOrcStoreService().write(getEngine().getComponentStore(), outputPath);
        sts.done();
    }

    public void load(InputStream inputStream, String sourceForStatus, Class... components) throws IOException {
        require(components);
        Status sts = Status.of("Loading board from " + sourceForStatus);
        Engine engine = getEngine();
        new CsvReader().load(inputStream, engine.getComponentStore());
        sts.done();
    }

    public void save(String outputPath) throws IOException {
        Status sts = Status.of("Saving population data to " + outputPath);
        getEngine().getComponentStore().fireUnderlyingDataChanged(0, getEngine().getCount(), getEngine());
        csvWriter.writeCsv(outputPath, getEngine().getComponentStore());
        sts.done();
    }

    public void require(Class... components) {
        engineConfiguration.addComponentClasses(components);
    }

    public void addListener(EngineCreationListener engineCreationListener) {
        engineConfiguration.addEngineCreationListeners(engineCreationListener);
    }

    private OrcStoreService getLazyOrcStoreService() {
        if (lazyOrcStoreService == null) {
            lazyOrcStoreService = new OrcStoreService();
        }
        return lazyOrcStoreService;
    }

}
