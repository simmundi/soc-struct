package pl.edu.icm.board.workplace;

import pl.edu.icm.board.geography.commune.Commune;

public class Flow {
    private final Commune commune;
    private final int numberOfPeople;

    public Flow(Commune commune, int numberOfPeople) {
        this.commune = commune;
        this.numberOfPeople = numberOfPeople;
    }

    public Commune getCommune() {
        return commune;
    }

    public int getNumberOfPeople() {
        return numberOfPeople;
    }
}
