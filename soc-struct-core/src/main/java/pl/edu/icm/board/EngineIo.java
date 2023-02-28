/*
 * Copyright (c) 2022 ICM Epidemiological Model Team at Interdisciplinary Centre for Mathematical and Computational Modelling, University of Warsaw.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *
 */

package pl.edu.icm.board;

import net.snowyhollows.bento.annotation.WithFactory;
import net.snowyhollows.bento.config.WorkDir;
import pl.edu.icm.trurl.csv.CsvReader;
import pl.edu.icm.trurl.csv.CsvWriter;
import pl.edu.icm.trurl.ecs.Engine;
import pl.edu.icm.trurl.ecs.EngineConfiguration;
import pl.edu.icm.trurl.ecs.EngineCreationListener;
import pl.edu.icm.trurl.io.orc.OrcStoreService;
import pl.edu.icm.trurl.util.Status;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class EngineIo {
    private OrcStoreService lazyOrcStoreService;
    private final EngineConfiguration engineConfiguration;
    private final CsvWriter csvWriter;
    private final WorkDir workDir;
    private final String rootPath;

    @WithFactory
    public EngineIo(EngineConfiguration engineConfiguration, CsvWriter csvWriter, WorkDir workDir, String rootPath) {
        this.engineConfiguration = engineConfiguration;
        this.csvWriter = csvWriter;
        this.workDir = workDir;
        this.rootPath = rootPath;
    }

    public Engine getEngine() {
        return engineConfiguration.getEngine();
    }

    public void load(InputStream inputStream, Class... components) throws IOException {
        load(inputStream, "[" + inputStream + "]", components);
    }

    public void load(String fileName, Class... components) throws IOException {
        load(workDir.openForReading(new File(fileName)), fileName, components);
    }

    public void loadOrc(String fileName, Class... components) throws IOException {
        loadOrc(fileName, fileName, components);
    }

    public void loadOrc(String inputFile, String sourceForStatus, Class... components) throws IOException {
        require(components);
        Status sts = Status.of("Loading board from " + absolutize(sourceForStatus));
        Engine engine = getEngine();
        getLazyOrcStoreService().read(engine.getStore(), absolutize(inputFile));
        sts.done();
    }

    public void saveOrc(String outputPath) throws IOException {
        Status sts = Status.of("Saving population data to " + absolutize(outputPath));
        getEngine().getStore().fireUnderlyingDataChanged(0, getEngine().getCount(), getEngine());
        getLazyOrcStoreService().write(getEngine().getStore(), absolutize(outputPath));
        sts.done();
    }

    public void load(InputStream inputStream, String sourceForStatus, Class... components) throws IOException {
        require(components);
        Status sts = Status.of("Loading board from " + sourceForStatus);
        Engine engine = getEngine();
        new CsvReader().load(inputStream, engine.getStore());
        sts.done();
    }
    public void save(String outputPath) throws IOException {
        Status sts = Status.of("Saving population data to " + outputPath);
        getEngine().getStore().fireUnderlyingDataChanged(0, getEngine().getCount(), getEngine());
        csvWriter.writeCsv(outputPath, getEngine().getStore());
        sts.done();
    }

    public void require(Class... components) {
        engineConfiguration.addComponentClasses(components);
    }

    public void addListener(EngineCreationListener engineCreationListener) {
        engineConfiguration.addEngineCreationListener(engineCreationListener);
    }

    private OrcStoreService getLazyOrcStoreService() {
        if (lazyOrcStoreService == null) {
            lazyOrcStoreService = new OrcStoreService();
        }
        return lazyOrcStoreService;
    }

    private String absolutize(String path) {
        return new File(rootPath, path).getAbsolutePath();
    }

}
