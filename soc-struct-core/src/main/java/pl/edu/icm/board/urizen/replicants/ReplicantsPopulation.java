package pl.edu.icm.board.urizen.replicants;

import net.snowyhollows.bento2.annotation.WithFactory;
import pl.edu.icm.board.urizen.population.Population;
import pl.edu.icm.board.urizen.population.gm.GusModelCountyPopulationLoader;

/**
 * <p>
 * Replicants are people outside of the basic GUS model.
 *
 * <p>
 * They are created alongside their households / workplaces and have
 * some special properties (like never working or studying at the same university).
 *
 * <p>
 * To help keep the population statistics correct, all replicants are created from one common pool
 * of "people" - which itself is based on Wrocław's population from our GUS data (for no special reason).
 */
public class ReplicantsPopulation {
    private final GusModelCountyPopulationLoader gusModelCountyPopulationLoader;
    private final Population population;

    @WithFactory
    public ReplicantsPopulation(GusModelCountyPopulationLoader gusModelCountyPopulationLoader) {
        this.gusModelCountyPopulationLoader = gusModelCountyPopulationLoader;
        population = gusModelCountyPopulationLoader.createCountyBinPools().get("0264");
    }

    public Population getPopulation() {
        return population;
    }
}
