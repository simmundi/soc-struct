package pl.edu.icm.board.urizen.household.model;

import pl.edu.icm.board.urizen.household.fileformat.RcbCovidDane02;
import pl.edu.icm.trurl.ecs.annotation.WithMapper;

@WithMapper
public class RcbCovidDane02Aggregate {
    private RcbCovidDane02 rcbCovidDane02;
    private int occurences;

    public RcbCovidDane02Aggregate() {
    }

    public RcbCovidDane02Aggregate(RcbCovidDane02 rcbCovidDane02, int occurences) {
        this.rcbCovidDane02 = rcbCovidDane02;
        this.occurences = occurences;
    }

    public RcbCovidDane02 getRcbCovidDane02() {
        return rcbCovidDane02;
    }

    public void setRcbCovidDane02(RcbCovidDane02 rcbCovidDane02) {
        this.rcbCovidDane02 = rcbCovidDane02;
    }

    public int getOccurences() {
        return occurences;
    }

    public void setOccurences(int occurences) {
        this.occurences = occurences;
    }
}
