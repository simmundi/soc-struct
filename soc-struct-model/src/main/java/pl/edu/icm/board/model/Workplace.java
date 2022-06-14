package pl.edu.icm.board.model;

import pl.edu.icm.trurl.ecs.annotation.WithMapper;

@WithMapper
public class Workplace {
    private short employees;

    public Workplace() {
    }

    public Workplace(short employees) {
        this.employees = employees;
    }

    public short getEmployees() {
        return employees;
    }

    public void setEmployees(short employees) {
        this.employees = employees;
    }
}
