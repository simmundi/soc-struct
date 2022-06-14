package pl.edu.icm.board.workplace;

import java.util.ArrayList;
import java.util.List;

public class EmploymentInCommune {
    int potentialEmployees;
    int workplaces;
    int localEmployees;
    List<Flow> travelingEmployees = new ArrayList<>();

    public int getPotentialEmployees() {
        return potentialEmployees;
    }

    public int getWorkplaces() {
        return workplaces;
    }

    public int getLocalEmployees() {
        return localEmployees;
    }

    public List<Flow> getTravelingEmployees() {
        return travelingEmployees;
    }
}
