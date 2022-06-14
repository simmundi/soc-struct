package pl.edu.icm.board.urizen.household.cloner;

import net.snowyhollows.bento2.annotation.WithFactory;
import pl.edu.icm.board.Board;
import pl.edu.icm.board.agesex.AgeSexFromDistributionPicker;
import pl.edu.icm.board.model.AdministrationUnit;
import pl.edu.icm.board.model.Household;
import pl.edu.icm.board.model.Named;
import pl.edu.icm.board.model.Person;
import pl.edu.icm.trurl.bin.Bin;
import pl.edu.icm.trurl.bin.BinPool;
import pl.edu.icm.trurl.util.Status;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Services which counts statistics for the current engine state.
 * <p>
 * These include:
 * <ul>
 *  <li>count of families per teryt, binned by histogram of member by age
 *  <li>count of people per teryt</li>
 * </ul>
 */
public class FamilyShapeStatsService {
    private final Board board;
    private final AgeSexFromDistributionPicker ageSexFromDistributionPicker;

    @WithFactory
    public FamilyShapeStatsService(Board board, AgeSexFromDistributionPicker ageSexFromDistributionPicker) {
        this.board = board;
        this.ageSexFromDistributionPicker = ageSexFromDistributionPicker;
        board.require(
                Household.class,
                AdministrationUnit.class,
                Person.class,
                Named.class);
    }

    public FamilyShapeStats countStats() {
        FamilyShapeStats familyShapeStats = new FamilyShapeStats();
        var status = Status.of("Finding family statistics", 1_000_000);
        Map<HouseholdShape, Bin> shapes = new HashMap<>();
        board
                .getEngine()
                .streamDetached()
                .map(e -> HouseholdShape.tryCreate(e, ageSexFromDistributionPicker))
                .filter(shape -> shape != null)
                .forEach(shape -> {
                    status.tick();
                    familyShapeStats.populationByTeryt.computeIfAbsent(shape.getTeryt(), t -> new AtomicInteger())
                            .addAndGet(shape.getMemberCount());
                    shapes
                            .computeIfAbsent(shape, s -> familyShapeStats.shapesByTeryt
                                    .computeIfAbsent(s.getTeryt(), t -> new BinPool<>())
                                    .add(s, 0))
                            .add(1);
                });
        status.done();
        return familyShapeStats;
    }
}
