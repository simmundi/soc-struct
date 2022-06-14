package pl.edu.icm.board.urizen.population.trusted;

import net.snowyhollows.bento2.annotation.WithFactory;
import pl.edu.icm.board.util.FileToStreamService;
import pl.edu.icm.trurl.csv.CsvReader;
import pl.edu.icm.trurl.ecs.mapper.Mappers;
import pl.edu.icm.trurl.store.array.ArrayStoreFactory;

import java.io.FileNotFoundException;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class CountyPopulationLoader {
    private final Map<String, BasicPopulationDatum> population;
    private final FileToStreamService fileToStreamService;
    private final int totalPopulation;

    @WithFactory
    public CountyPopulationLoader(
            String powiatsPopulationFilename,
            FileToStreamService fileToStreamService,
            int totalPopulation) {
        this.fileToStreamService = fileToStreamService;
        this.totalPopulation = totalPopulation;

        try {
            var loader = new CsvReader();
            var inputStream = fileToStreamService.filename(powiatsPopulationFilename);

            population = Mappers.stream(loader.load(
                            inputStream,
                            new ArrayStoreFactory(500),
                            BasicPopulationDatum.class,
                            "", "teryt", "total"))
                    .collect(
                            Collectors.toMap(
                                    BasicPopulationDatum::getTeryt,
                                    Function.identity(),
                                    (a, b) -> a));

        } catch (FileNotFoundException e) {
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
