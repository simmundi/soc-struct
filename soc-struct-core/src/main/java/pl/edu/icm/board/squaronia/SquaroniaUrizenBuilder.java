package pl.edu.icm.board.squaronia;

import net.snowyhollows.bento2.annotation.WithFactory;
import pl.edu.icm.board.Board;
import pl.edu.icm.board.util.RandomProvider;

public class SquaroniaUrizenBuilder {
    private final Board board;
    private final int familySize;
    private final int populationSize;
    private final int borderLength;
    private final int numberOfWoman;

    @WithFactory
    public SquaroniaUrizenBuilder(Board board,
                                  int defaultFamilySize,
                                  int defaultPopulationSize,
                                  int defaultBorderLength,
                                  float defaultPercentOfWoman) {
        this.board = board;
        this.familySize = defaultFamilySize;
        this.populationSize = defaultPopulationSize;
        this.borderLength = defaultBorderLength;
        this.numberOfWoman = (int) (this.populationSize * defaultPercentOfWoman / 100);
    }

    public SquaroniaUrizen build(RandomProvider randomProvider){
        return new SquaroniaUrizen(board, familySize, populationSize, borderLength, numberOfWoman, randomProvider);
    }
}
