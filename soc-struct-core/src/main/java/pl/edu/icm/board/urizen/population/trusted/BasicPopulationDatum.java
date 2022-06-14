package pl.edu.icm.board.urizen.population.trusted;

import pl.edu.icm.trurl.ecs.annotation.WithMapper;

@WithMapper
class BasicPopulationDatum {
    private String teryt;
    private int total;

    public String getTeryt() {
        return teryt;
    }

    public void setTeryt(String teryt) {
        this.teryt = teryt;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }
}
