package pl.edu.icm.board.model;

import pl.edu.icm.trurl.ecs.Entity;
import pl.edu.icm.trurl.ecs.annotation.WithMapper;

@WithMapper
public class HouseholdChores {
    private Entity responsibleForWalkingChildren;
    private float totalFamilyCost;

    public Entity getResponsibleForWalkingChildren() {
        return responsibleForWalkingChildren;
    }

    public void setResponsibleForWalkingChildren(Entity responsibleForWalkingChildren) {
        this.responsibleForWalkingChildren = responsibleForWalkingChildren;
    }

    public float getTotalFamilyCost() {
        return totalFamilyCost;
    }

    public void setTotalFamilyCost(float totalFamilyCost) {
        this.totalFamilyCost = totalFamilyCost;
    }
}
