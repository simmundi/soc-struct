package pl.edu.icm.board.geography.commune;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import net.snowyhollows.bento.annotation.WithFactory;
import pl.edu.icm.board.geography.KilometerGridCell;
import pl.edu.icm.board.model.Household;
import pl.edu.icm.board.model.Location;
import pl.edu.icm.trurl.ecs.EngineConfiguration;
import pl.edu.icm.trurl.ecs.Entity;
import pl.edu.icm.trurl.ecs.util.EntityIterator;
import pl.edu.icm.trurl.ecs.util.Selectors;
import pl.edu.icm.trurl.util.Status;

import java.util.*;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class HouseholdsInCommuneAccessor {
    private static final int INITIAL_ARRAY_CAPACITY = 5_000;
    private final EngineConfiguration engineConfiguration;
    private final Selectors selectors;
    private final CommuneManager communeManager;
    private final Map<String, List<Integer>> terytToHouseholdId = new HashMap<>();

    @WithFactory
    HouseholdsInCommuneAccessor(EngineConfiguration engineConfiguration, Selectors selectors, CommuneManager communeManager) {
        this.engineConfiguration = engineConfiguration;
        this.selectors = selectors;
        this.communeManager = communeManager;
    }

    public IntStream getHouseholdIdsForTeryts(Collection<String> teryts) {
        if (terytToHouseholdId.isEmpty()) buildTerytToHouseholdMultimap();
        return terytToHouseholdId.keySet().stream()
                .filter(teryt -> teryts.stream().anyMatch(teryt::startsWith))
                .flatMapToInt(key -> terytToHouseholdId.get(key).stream().mapToInt(Integer::intValue));
    }

    private void buildTerytToHouseholdMultimap() {
        var status = Status.of("Building teryt to household multimap", 500_000);
        engineConfiguration.getEngine().execute(EntityIterator.select(selectors.allWithComponents(Household.class, Location.class)).forEach(entity -> {
            var kilometerGridCell = KilometerGridCell.fromLocation(entity.get(Location.class));
            terytToHouseholdId.computeIfAbsent(
                    communeManager.communeAt(kilometerGridCell).getTeryt(),
                    (a) -> new IntArrayList(INITIAL_ARRAY_CAPACITY)
                    ).add(entity.getId());
            status.tick();
        }));
        status.done();
    }
}
