package pl.edu.icm.board.urizen.household.cloner;

import pl.edu.icm.trurl.bin.BinPool;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Value object with statistics for sizes of households,
 * @see FamilyShapeStatsService
 */
class FamilyShapeStats {
    final Map<String, BinPool<HouseholdShape>> shapesByTeryt = new HashMap<>();
    final Map<String, AtomicInteger> populationByTeryt = new HashMap<>();
}
