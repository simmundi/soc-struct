package pl.edu.icm.board.geography.commune;

import net.snowyhollows.bento2.annotation.WithFactory;
import pl.edu.icm.board.Board;
import pl.edu.icm.board.geography.KilometerGridCell;
import pl.edu.icm.board.geography.density.PopulationDensityLoader;
import pl.edu.icm.board.model.Household;
import pl.edu.icm.board.model.Location;
import pl.edu.icm.trurl.ecs.util.EntityIterator;
import pl.edu.icm.trurl.ecs.util.Selectors;
import pl.edu.icm.trurl.ecs.util.Systems;
import pl.edu.icm.trurl.util.Status;

import java.util.HashMap;
import java.util.Map;

public class PopulationService {
    private final Map<String, Integer> populationByTeryt = new HashMap<>();
    private final PopulationDensityLoader populationDensityLoader;
    private final CommuneManager communeManager;
    private final Board board;
    private final Selectors selectors;

    @WithFactory
    public PopulationService(Board board,
                             CommuneManager communeManager,
                             PopulationDensityLoader populationDensityLoader,
                             Selectors selectors) {
        this.populationDensityLoader = populationDensityLoader;
        this.communeManager = communeManager;
        this.board = board;
        this.selectors = selectors;
    }

    public void load() {
        if (!populationByTeryt.isEmpty()) {
            return;
        }
        populationDensityLoader.loadActualPopulationFromEngine();
        var status = Status.of("loading population", 1_000_000);
        board.getEngine().execute(EntityIterator.select(selectors.allWithComponents(Household.class, Location.class)).forEach(Household.class, Location.class, (entity, household, location) -> {
            var teryt = communeManager.communeAt(KilometerGridCell.fromLocation(location)).getTeryt();
            var size = household.getMembers().size();
            if (isACityWithPowiatRights(teryt)) {
                teryt = teryt.substring(0, 4);
            } else {
                teryt = teryt.substring(0, 6);
            }
            populationByTeryt.compute(teryt, (t, v) -> (v == null) ? size : v + size);
            status.tick();
        }));
        status.done();
    }

    public AdministrationAreaType typeFromLocation(Location location) {
        var cell = KilometerGridCell.fromLocation(location);
        var teryt = communeManager.communeAt(cell).getTeryt();
        var areaCode = teryt.substring(6);
        switch (areaCode) {
            case "2":
            case "5":
                return AdministrationAreaType.VILLAGE;
            case "1":
            case "4":
            case "8":
            case "9":
                if (isACityWithPowiatRights(teryt)) {
                    return AdministrationAreaType.fromCityPopulation(populationByTeryt.get(teryt.substring(0, 4)));
                }
                return AdministrationAreaType.fromCityPopulation(populationByTeryt.get(teryt.substring(0, 6)));
            default:
                if (isACityWithPowiatRights(teryt)) {
                    return AdministrationAreaType.fromCityPopulation(populationByTeryt.get(teryt.substring(0, 4)));
                }
            case "3":
                if (populationDensityLoader.density(cell) > 1000) {
                    return AdministrationAreaType.fromCityPopulation(populationByTeryt.get(teryt.substring(0, 6)));
                } else return AdministrationAreaType.VILLAGE;
        }
    }

    public boolean isACityWithPowiatRights(String teryt) {
        var terytPowiatu = Integer.parseInt(teryt.substring(2, 4));
        return (terytPowiatu >= 61 && terytPowiatu <= 99);
    }
}
