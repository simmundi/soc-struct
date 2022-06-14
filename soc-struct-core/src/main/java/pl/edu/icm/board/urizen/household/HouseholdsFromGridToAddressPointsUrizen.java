package pl.edu.icm.board.urizen.household;

import com.google.common.collect.ListMultimap;
import com.google.common.collect.MultimapBuilder;
import com.google.common.collect.Multimaps;
import net.snowyhollows.bento2.annotation.WithFactory;
import org.apache.commons.math3.random.RandomGenerator;
import pl.edu.icm.board.Board;
import pl.edu.icm.board.geography.KilometerGridCell;
import pl.edu.icm.board.geography.prg.AddressPointManager;
import pl.edu.icm.board.model.Household;
import pl.edu.icm.board.model.Location;
import pl.edu.icm.board.urizen.generic.Entities;
import pl.edu.icm.board.urizen.household.model.ComplexBlueprint;
import pl.edu.icm.board.util.RandomProvider;
import pl.edu.icm.trurl.ecs.Entity;
import pl.edu.icm.trurl.ecs.Session;
import pl.edu.icm.trurl.util.Status;

public class HouseholdsFromGridToAddressPointsUrizen {
    private final AddressPointManager addressPointsManager;
    private final Board board;
    private final RandomGenerator random;
    private final Entities entities;
    private int failures;

    @WithFactory
    public HouseholdsFromGridToAddressPointsUrizen(AddressPointManager addressPointsManager,
                                                   Board board,
                                                   RandomProvider randomProvider,
                                                   Entities entities) {
        this.addressPointsManager = addressPointsManager;
        this.board = board;
        this.random = randomProvider.getRandomGenerator(HouseholdsFromGridToAddressPointsUrizen.class);
        this.entities = entities;
    }

    public int assignHouseholds() {
        var statusHouseholds = Status.of("extracting households", 100000);
        var householdsIdInKilometerGridCell = board.getEngine().streamDetached()
                .filter(e -> e.get(Household.class) != null && e.get(Location.class) != null)
                .peek(e -> statusHouseholds.tick())
                .collect(Multimaps.toMultimap(
                        e -> KilometerGridCell.fromLocation(e.get(Location.class)),
                        Entity::getId,
                        MultimapBuilder.hashKeys(240000).arrayListValues()::build)).asMap();
        statusHouseholds.done();

        var status = Status.of("collecting address points", 1000000);

        ListMultimap<KilometerGridCell, ComplexBlueprint> BlueprintByKilometerCell = addressPointsManager.streamAddressPoints()
                .peek(ap -> status.tick())
                .collect(Multimaps.toMultimap(
                        ap -> KilometerGridCell.fromPl1992ENMeters(ap.getEasting(), ap.getNorthing()),
                        ComplexBlueprint::from,
                        MultimapBuilder.hashKeys(240000).arrayListValues()::build));
        status.done();

        var statusOfAdjusting = Status.of("Adding households to complex blueprint");
        householdsIdInKilometerGridCell.forEach((key, value) -> {
            statusOfAdjusting.tick();
            var options = BlueprintByKilometerCell.get(key);
            if (options.size() == 0) {
                failures += value.size();
                return;
            }
            for (var householdId : value) {
                var target = options.get(random.nextInt(options.size()));
                target.addHouseholdId(householdId);
            }
        });
        statusOfAdjusting.done();

        var status2 = Status.of("Adjusting households", 500000);

        board.getEngine().execute(sessionFactory -> {
            Session session = sessionFactory.create();
            BlueprintByKilometerCell.forEach(
                        (key, value) -> entities.createBuildingFromBlueprint(session, value)
                );
            session.close();
        });


        status2.done();
        return failures;
    }

}
