package pl.edu.icm.board.urizen.person;

import net.snowyhollows.bento.annotation.WithFactory;
import org.apache.commons.math3.random.RandomGenerator;
import pl.edu.icm.board.Board;
import pl.edu.icm.board.model.Named;
import pl.edu.icm.board.model.Household;
import pl.edu.icm.board.model.Person;
import pl.edu.icm.board.util.RandomProvider;
import pl.edu.icm.trurl.bin.Bin;
import pl.edu.icm.trurl.ecs.Entity;
import pl.edu.icm.trurl.ecs.selector.Selector;
import pl.edu.icm.trurl.ecs.util.Selectors;
import pl.edu.icm.trurl.util.Status;

import static pl.edu.icm.trurl.ecs.util.EntityIterator.select;

public class PersonNameUrizen {
    private final Board board;
    private final NamesSource namesSource;
    private final RandomGenerator random;
    private final Selectors selectors;

    @WithFactory
    public PersonNameUrizen(Board board,
                            NamesSource namesSource,
                            RandomProvider randomProvider,
                            Selectors selectors) {
        this.board = board;
        this.namesSource = namesSource;
        this.selectors = selectors;
        board.require(Named.class, Household.class, Person.class);
        this.random = randomProvider.getRandomGenerator(PersonNameUrizen.class);
    }

    public void giveNames() {
        Status stats = Status.of("Giving names", 1_000_000);
        NamePools names = namesSource.load();

        board.getEngine().execute(select(createHouseholdSelector()).forEach(entity -> {
            Bin<String> lastNameBin = names.surnames.sample(random.nextDouble());
            Household household = entity.get(Household.class);
            for (Entity memberEntity : household.getMembers()) {
                if (lastNameBin.getCount() <= 0 || random.nextDouble() < 0.15) {
                    lastNameBin = names.surnames.sample(random.nextDouble());
                }
                String lastName = lastNameBin.pick();
                Person person = memberEntity.get(Person.class);
                String firstName = person.getSex() ==
                        Person.Sex.K
                        ? names.femaleNames.sample(random.nextDouble()).pick()
                        : names.maleNames.sample(random.nextDouble()).pick();
                if ((lastName.endsWith("SKI") || lastName.endsWith("CKI")) && person.getSex() == Person.Sex.K) {
                    lastName = lastName.substring(0, lastName.length() - 1) + "A";
                }
                Named named = new Named();
                named.setName(firstName + " " + lastName);
                memberEntity.add(named);
                stats.tick();
            }
        }));

        stats.done();
    }

    private Selector createHouseholdSelector() {
        return selectors.filtered(selectors.allEntities(10_000), selectors.hasComponents(Household.class));
    }
}
