package pl.edu.icm.board.geography.graph;

import net.snowyhollows.bento.annotation.WithFactory;
import pl.edu.icm.board.util.FileCacheService;
import pl.edu.icm.trurl.ecs.mapper.Mappers;
import pl.edu.icm.trurl.store.Store;
import pl.edu.icm.trurl.store.array.ArrayStore;

public class GraphManager {

    @WithFactory
    GraphManager(FileCacheService fileCacheService, GraphSource graphSource) {
        Store store = new ArrayStore();
        var graphItemMapper = Mappers.create(GraphStoreItem.class);
        graphItemMapper.configureStore(store);
        fileCacheService
                .computeIfAbsent("graph", store, graphSource::load);
    }

}
