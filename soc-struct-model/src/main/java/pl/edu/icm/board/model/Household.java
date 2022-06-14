package pl.edu.icm.board.model;

import pl.edu.icm.trurl.ecs.Entity;
import pl.edu.icm.trurl.ecs.annotation.WithMapper;

import java.util.ArrayList;
import java.util.List;

@WithMapper
public class Household {
    private List<Entity> members = new ArrayList<>();

    public List<Entity> getMembers() {
        return members;
    }
}
