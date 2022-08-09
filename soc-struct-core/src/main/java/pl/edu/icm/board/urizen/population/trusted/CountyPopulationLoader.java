package pl.edu.icm.board.urizen.population.trusted;

import net.snowyhollows.bento.annotation.WithFactory;
import net.snowyhollows.bento.config.WorkDir;
import pl.edu.icm.trurl.csv.CsvReader;
import pl.edu.icm.trurl.ecs.mapper.Mappers;
import pl.edu.icm.trurl.store.array.ArrayStoreFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class CountyPopulationLoader {
    private final Map<String, BasicPopulationDatum> population;
    private final WorkDir workDir;
    private final int totalPopulation;

    @WithFactory
    public CountyPopulationLoader(
            String powiatsPopulationFilename,
            WorkDir workDir,
            int totalPopulation) {
        this.workDir = workDir;
        this.totalPopulation = totalPopulation;

        try (InputStream inputStream = workDir.openForReading(new File(powiatsPopulationFilename))){
            var loader = new CsvReader();
            population = Mappers.stream(loader.load(
                            inputStream,
                            new ArrayStoreFactory(),
                            BasicPopulationDatum.class,
                            "", "teryt", "total"))
                    .collect(
                            Collectors.toMap(
                                    BasicPopulationDatum::getTeryt,
                                    Function.identity(),
                                    (a, b) -> a));

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public int populationOf(String teryt) {
        return population.get(teryt).getTotal();
    }

    public int totalPopulation() {
        return totalPopulation;
    }

}
