package pl.edu.icm.board.urizen.place;

import net.snowyhollows.bento2.annotation.WithFactory;
import org.apache.commons.math3.distribution.ZipfDistribution;
import org.apache.commons.math3.random.RandomGenerator;
import pl.edu.icm.board.Board;
import pl.edu.icm.board.model.AdministrationUnit;
import pl.edu.icm.board.geography.KilometerGridCell;
import pl.edu.icm.board.model.Location;
import pl.edu.icm.board.geography.commune.Commune;
import pl.edu.icm.board.geography.commune.CommuneManager;
import pl.edu.icm.board.model.Household;
import pl.edu.icm.board.model.Employee;
import pl.edu.icm.board.model.Person;
import pl.edu.icm.board.urizen.generic.EntityStreamManipulator;
import pl.edu.icm.board.util.RandomProvider;
import pl.edu.icm.board.workplace.ProfessionalActivityAssessor;
import pl.edu.icm.board.model.Workplace;
import pl.edu.icm.board.workplace.WorkplacesInCommunes;
import pl.edu.icm.trurl.bin.BinPool;
import pl.edu.icm.trurl.bin.BinPoolsByShape;
import pl.edu.icm.trurl.ecs.Entity;
import pl.edu.icm.trurl.ecs.Session;
import pl.edu.icm.trurl.ecs.util.Selectors;
import pl.edu.icm.trurl.util.Status;
import pl.edu.icm.trurl.util.TextFile;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import static pl.edu.icm.trurl.ecs.util.EntityIterator.select;

public class WorkplacesUrizen {
    private final WorkplacesInCommunes workplacesInCommunes;
    private final CommuneManager communeManager;
    private final EntityStreamManipulator entityStreamManipulator;
    private final Board board;
    private final ProfessionalActivityAssessor professionalActivityAssessor;
    private final RandomGenerator random;
    private final Selectors selectors;

    private final double[] workplaceCountsByEmployees;

    @WithFactory
    public WorkplacesUrizen(
            WorkplacesInCommunes workplacesInCommunes,
            Board board,
            int publicSectorZipfN,
            String publicSectorZipfS,
            int publicSectorTotal,
            int privateSectorZipfN,
            String privateSectorZipfS,
            int privateSectorTotal,
            CommuneManager communeManager,
            EntityStreamManipulator entityStreamManipulator,
            ProfessionalActivityAssessor professionalActivityAssessor,
            RandomProvider randomProvider, Selectors selectors) {
        this.workplacesInCommunes = workplacesInCommunes;
        this.board = board;
        this.communeManager = communeManager;
        this.entityStreamManipulator = entityStreamManipulator;
        this.professionalActivityAssessor = professionalActivityAssessor;
        this.random = randomProvider.getRandomGenerator(WorkplacesUrizen.class);
        this.selectors = selectors;

        int maxEmployees = Math.max(publicSectorZipfN, privateSectorZipfN);
        workplaceCountsByEmployees = new double[maxEmployees];

        accumulateEmpoyeeSlots(publicSectorTotal, new ZipfDistribution(publicSectorZipfN, Double.parseDouble(publicSectorZipfS)));
        accumulateEmpoyeeSlots(privateSectorTotal, new ZipfDistribution(privateSectorZipfN, Double.parseDouble(privateSectorZipfS)));
        smearOneSlot();

        board.require(Workplace.class, AdministrationUnit.class, Employee.class);
    }

    private void smearOneSlot() {
        // we want to remove all the workplaces with size of 1
        double toSmear = workplaceCountsByEmployees[1];
        workplaceCountsByEmployees[1] = 0;

        // we assume that the self-employed (aka b2b) only work in workplaces between 2-49
        double perBin = toSmear / 48.0;
        for (int i = 2; i <= 49; i++) {
            workplaceCountsByEmployees[i] += (perBin / i);
        }
    }

    private void accumulateEmpoyeeSlots(int total, ZipfDistribution distribution) {
        for (int i = 0; i < workplaceCountsByEmployees.length; i++) {
            workplaceCountsByEmployees[i] += distribution.probability(i) * total;
        }
    }

    public void createWorkplaces() {
        try {
            workplacesInCommunes.calculateWorkplaces();
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
        var engine = board.getEngine();
        BinPool<Commune> employees = new BinPool<>();
        for (Commune commune : communeManager.getCommunes()) {
            employees.add(commune, workplacesInCommunes.getEmploymentDataFor(commune).getWorkplaces());
        }

        try (FileWriter fw = new FileWriter("output/slots_in_communes.csv")) {
            PrintWriter pw = new PrintWriter(fw);
            pw.println("teryt,nazwa,slots");
            employees.streamBins().forEach(b -> {
                pw.println(String.format("%s,%s,%d", b.getLabel().getTeryt(), b.getLabel().getName(), b.getCount()));
            });
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }

        var status = Status.of("Creating workplaces", 200000);
        engine.execute(sessionFactory -> {
            Session session = sessionFactory.create();
            double accumulator = 0.0;
            for (int i = workplaceCountsByEmployees.length - 1; i > 0; i--) {
                accumulator += workplaceCountsByEmployees[i];
                while (accumulator > 1.0) { // integerization
                    Entity workplaceEntity = session.createEntity();
                    workplaceEntity.add(new Workplace((short) i));
                    var choice = employees.sample(random.nextDouble()).pick(i);
                    workplaceEntity.add(new AdministrationUnit(choice.getTeryt()));
                    status.tick();
                    accumulator -= 1.0;
                }
            }
            session.close();
            status.done();
        });
    }

    public void assignWorkersToWorkplaces() {
        Status status = Status.of("grouping workplaces by communes");
        Map<Commune, Integer> slotsInExistingWorplaces = new HashMap<>();
        BinPoolsByShape<Commune, Entity> workplacesByCommunes = entityStreamManipulator.groupIntoShapes(
                board.getEngine().streamDetached()
                        .filter(e -> e.get(Workplace.class) != null),
                e -> e.get(Workplace.class).getEmployees(),
                e -> Stream.of(
                        communeManager
                                .communeForTeryt(
                                        e.get(AdministrationUnit.class).getTeryt())
                                .get()
                )
        );
        workplacesByCommunes.getGroupedBins().entrySet().stream().forEach(entry -> {
            slotsInExistingWorplaces.put(entry.getKey(), entry.getValue().getTotalCount());
        });
        status.done();

        Map<Commune, BinPool<Commune>> flows = new HashMap<>();
        Map<Commune, BinPool<Classification>> classifications = new HashMap<>();

        try (var df = TextFile.create("output/debug_flows.csv")) {
            df.println("commune,traveling,local,unemployed,workplaces");
            communeManager.getCommunes().forEach(commune -> {
                var employment = workplacesInCommunes.getEmploymentDataFor(commune);
                BinPool<Classification> classificationBinPool = new BinPool<>();
                classifications.put(commune, classificationBinPool);
                BinPool<Commune> flowsForOneCommune = new BinPool<>();
                flows.put(commune, flowsForOneCommune);

                employment.getTravelingEmployees().forEach(flow -> {
                    flowsForOneCommune.add(flow.getCommune(), flow.getNumberOfPeople());
                });

                var traveling = classificationBinPool.add(Classification.EMPLOYED_IN_ANOTHER_COMMUNE, flowsForOneCommune.getTotalCount());
                var local = classificationBinPool.add(Classification.EMPLOYED_IN_OWN_COMMUNE, employment.getLocalEmployees());
                var unemployed = classificationBinPool.add(Classification.UNEMPLOYED, employment.getPotentialEmployees() - classificationBinPool.getTotalCount());

                df.printf("%s,%d,%d,%d,%d\n", commune.getName(),
                        traveling.getCount(),
                        local.getCount(),
                        unemployed.getCount(),
                        slotsInExistingWorplaces.get(commune));
            });
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }

        Set<Commune> failures = new HashSet<>();
        AtomicInteger successesLocal = new AtomicInteger();
        AtomicInteger successesTravel = new AtomicInteger();
        AtomicInteger unemployed = new AtomicInteger();
        AtomicInteger errors = new AtomicInteger();
        Status.of("Assigning " + workplacesByCommunes.getAllBins().getTotalCount() + " workplaces", 100000);
        board.getEngine().execute(select(selectors.allWithComponents(Household.class, Location.class)).forEach(Household.class, Location.class, (e, household, location) -> {

            KilometerGridCell cell = KilometerGridCell.fromLocation(location);
            var commune = communeManager.communeAt(cell);
            var workplaces = workplacesByCommunes.getGroupedBins().get(commune);
            var flowsFromCommune = flows.get(commune);
            var classificationsForCommune = classifications.get(commune);

            if (workplaces == null) {
                failures.add(commune);
                return;
            }

            if (workplaces.getTotalCount() > 0 || flowsFromCommune.getTotalCount() > 0) {
                for (Entity member : household.getMembers()) {
                    Person person = member.get(Person.class);
                    boolean isAWorker = professionalActivityAssessor.assess(person);
                    if (isAWorker) {
                        Classification classification = classificationsForCommune.sample(random.nextDouble()).pick();
                        switch (classification) {
                            case EMPLOYED_IN_ANOTHER_COMMUNE:
                                var targetCommune = flowsFromCommune.sample(random.nextDouble()).pick();
                                var workplacesInAnotherCommune = workplacesByCommunes
                                        .getGroupedBins()
                                        .get(targetCommune);
                                if (workplacesInAnotherCommune == null) {
                                    failures.add(targetCommune);
                                    errors.incrementAndGet();
                                    break;
                                }
                                var targetWorkplaceInAnotherCommune = workplacesInAnotherCommune
                                        .sample(random.nextDouble())
                                        .pick();
                                member.add(new Employee()).setWork(targetWorkplaceInAnotherCommune);
                                status.tick();
                                successesTravel.incrementAndGet();
                                break;
                            case EMPLOYED_IN_OWN_COMMUNE:
                                var targetWorkplace = workplaces.sample(random.nextDouble()).pick();
                                member.add(new Employee()).setWork(targetWorkplace);
                                successesLocal.incrementAndGet();
                                status.tick();
                                break;
                            default:
                            case UNEMPLOYED:
                                unemployed.incrementAndGet();
                                break;
                        }
                    }
                }
            }
        }));
        status.done();
        System.out.println(String.format("Managed %d local, %d remote, %d unemployed and %d errors", successesLocal.get(), successesTravel.get(), unemployed.get(), errors.get()));
        try (
                FileWriter fw = new FileWriter("output/unused_slots_in_communes.csv")) {
            PrintWriter pw = new PrintWriter(fw);
            pw.println("teryt,nazwa,unusedSlots");
            workplacesByCommunes.getGroupedBins().entrySet().forEach(b -> {
                pw.println(String.format("%s,%s,%d", b.getKey().getTeryt(), b.getKey().getName(), b.getValue().getTotalCount()));
            });
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
        System.out.println("Failures:");
        System.out.println(failures);
        System.out.println("==============");
    }

    private enum Classification {
        UNEMPLOYED,
        EMPLOYED_IN_OWN_COMMUNE,
        EMPLOYED_IN_ANOTHER_COMMUNE
    }
}
