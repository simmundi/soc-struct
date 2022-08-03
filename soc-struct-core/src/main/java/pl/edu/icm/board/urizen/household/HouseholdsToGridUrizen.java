package pl.edu.icm.board.urizen.household;

import net.snowyhollows.bento.annotation.WithFactory;
import org.apache.commons.math3.random.RandomGenerator;
import pl.edu.icm.board.Board;
import pl.edu.icm.board.geography.KilometerGridCell;
import pl.edu.icm.board.geography.commune.CommuneManager;
import pl.edu.icm.board.model.AdministrationUnit;
import pl.edu.icm.board.model.Household;
import pl.edu.icm.board.model.Location;
import pl.edu.icm.board.model.Person;
import pl.edu.icm.board.urizen.household.model.HouseholdSlot;
import pl.edu.icm.board.urizen.household.model.RcbCovidDane02Aggregate;
import pl.edu.icm.board.urizen.household.model.RoughHouseholdShape;
import pl.edu.icm.board.util.RandomProvider;
import pl.edu.icm.trurl.bin.Bin;
import pl.edu.icm.trurl.bin.BinPool;
import pl.edu.icm.trurl.ecs.Engine;
import pl.edu.icm.trurl.ecs.Entity;
import pl.edu.icm.trurl.ecs.Session;
import pl.edu.icm.trurl.ecs.mapper.Mapper;
import pl.edu.icm.trurl.ecs.mapper.Mappers;
import pl.edu.icm.trurl.ecs.util.Selectors;
import pl.edu.icm.trurl.store.Store;
import pl.edu.icm.trurl.store.array.ArrayStore;
import pl.edu.icm.trurl.util.Status;

import java.util.BitSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static pl.edu.icm.trurl.ecs.util.EntityIterator.select;

public class HouseholdsToGridUrizen {
    private final Board board;
    private final CommuneManager communeManager;
    private final RandomGenerator random;
    private final Selectors selectors;
    private final Store householdsInGrid = new ArrayStore(1_000_000);
    private final Mapper<RcbCovidDane02Aggregate> aggregateMapper = Mappers.create(RcbCovidDane02Aggregate.class);

    @WithFactory
    public HouseholdsToGridUrizen(Board board,
                                  CommuneManager communeManager,
                                  HouseholdInGridLoader householdInGridLoader,
                                  RandomProvider randomProvider, Selectors selectors) {
        this.board = board;
        this.communeManager = communeManager;
        this.selectors = selectors;
        this.board.require(Location.class, Household.class, AdministrationUnit.class);
        this.random = randomProvider.getRandomGenerator(HouseholdsToGridUrizen.class);
        householdInGridLoader.load(householdsInGrid);
        aggregateMapper.configureStore(householdsInGrid);
        aggregateMapper.attachStore(householdsInGrid);
    }

    public void allocateHouseholdsToGrid() {
        Engine engine = board.getEngine();

        Map<String, Set<String>> countiesToCells = communeManager.getCommunes().stream().collect(Collectors.groupingBy(c -> c.getTeryt().substring(0, 4)))
                .entrySet().stream()
                .collect(Collectors.toMap(
                        e -> e.getKey(),
                        e -> e.getValue().stream()
                                .flatMap(c -> c.getCells().stream().map(KilometerGridCell::toString)).collect(Collectors.toSet())));

        Map<String, BitSet> counties = new HashMap<>();

        for (String countyTeryt : countiesToCells.keySet()) {
            counties.put(countyTeryt, new BitSet());
        }

        var sbs = Status.of("building indices", 500_000);
        engine.execute(select(selectors.allWithComponents(AdministrationUnit.class))
                .detachEntities()
                .forEach(AdministrationUnit.class, (entity, unit) -> {
                    var bitSet = counties.get(unit.getTeryt().substring(0, 4));
                    bitSet.set(entity.getId());
                    sbs.tick();
                }));
        sbs.done();

        Status statusBar = Status.of("Allocating households into grid", 500000);

        for (String countyTeryt : countiesToCells.keySet()) {
            AtomicInteger failures = new AtomicInteger(0);
            AtomicInteger successes = new AtomicInteger(0);
            AtomicInteger strange = new AtomicInteger(0);

            Map<RoughHouseholdShape, BinPool<HouseholdSlot>> bins =
                    buildPoolFor(countiesToCells.get(countyTeryt));

            engine.execute(sessionFactory -> {
                Session session = sessionFactory.create();
                counties.get(countyTeryt).stream().forEach(id -> {
                    Entity entity = session.getEntity(id);
                    List<Entity> inhabitants = entity.get(Household.class).getMembers();
                    var shape = new RoughHouseholdShape(Math.min(Math.max(inhabitants.size(), 3), 10),
                            inhabitants.stream().anyMatch(in -> in.get(Person.class).getAge() > 70));
                    var slots = bins.get(shape);
                    if (slots == null) {
                        strange.incrementAndGet();
                        // this is a hack
                        shape = new RoughHouseholdShape(shape.getInhabitantsCount(), !shape.isFlag70Plus());
                        slots = bins.get(shape);
                        if (slots == null) {
                            failures.incrementAndGet();
                            return;
                        }
                    }

                    successes.incrementAndGet();
                    var slot = slots.sample(random.nextDouble());
                    var location = slot.getLabel().getCell().toLocation();
                    location.moveByMeters((int) (random.nextDouble() * 1000) - 500, (int) (random.nextDouble() * 1000) - 500);
                    entity.add(location);
                    statusBar.tick();
                });
                session.close();
            });
        }

        statusBar.done();
    }

    private Map<RoughHouseholdShape, BinPool<HouseholdSlot>> buildPoolFor(Set<String> gridIds) {
        BinPool<HouseholdSlot> result = new BinPool<>();

        Mappers.stream(aggregateMapper)
                .filter(row -> gridIds.contains(row.getRcbCovidDane02().getId_oczka()))
                .forEach(row -> {
                    var slot = new HouseholdSlot(
                            new RoughHouseholdShape(
                                    row.getRcbCovidDane02().getInhabintantsCount(),
                                    row.getRcbCovidDane02().getFlag70plus()),
                            row.getRcbCovidDane02().getCell());
                    result.add(slot, row.getOccurences());
                });

        return result.streamBins()
                .collect(Collectors.groupingBy(bin -> bin.getLabel().getShape()))
                .entrySet().stream()
                .collect(Collectors.toMap(
                        entry -> entry.getKey(),
                        entry -> result.createSubPool(entry.getValue().stream().map(Bin::getLabel).collect(Collectors.toList()))));
    }
}
