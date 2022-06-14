package pl.edu.icm.board.urizen.generic;

import pl.edu.icm.trurl.bin.BinPool;
import pl.edu.icm.trurl.ecs.Entity;

import java.util.Map;

public final class EntityBinsByShape<SHAPE> {
    private final BinPool<Entity> allBins;
    private final Map<SHAPE, BinPool<Entity>> groupedBins;

    public EntityBinsByShape(BinPool<Entity> allBins, Map<SHAPE, BinPool<Entity>> groupedBins) {
        this.allBins = allBins;
        this.groupedBins = groupedBins;
    }

    public BinPool<Entity> getAllBins() {
        return allBins;
    }

    public Map<SHAPE, BinPool<Entity>> getGroupedBins() {
        return groupedBins;
    }
}
