package pl.edu.icm.board.pdyn1;

import pl.edu.icm.trurl.io.orc.OrcStoreService;
import pl.edu.icm.trurl.store.Store;
import pl.edu.icm.trurl.store.array.ArrayStore;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

public class PdynIdExporter {

    private final ExportedIdMapper mapper;
    private final AtomicInteger counter;
    private final Store store;

    public PdynIdExporter (int capacity) {
        counter = new AtomicInteger(0);
        mapper = new ExportedIdMapper();
        store = new ArrayStore(capacity);
        mapper.configureAndAttach(store);
    }

    public void export(String dir) {
        try {
            store.fireUnderlyingDataChanged(0, counter.get());
            OrcStoreService orcStoreService = new OrcStoreService();
            orcStoreService.write(store, dir);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    public void saveIdMapping(int pdyn1Id, int pdyn2Id) {
        var exportedId = new ExportedId();
        exportedId.setPdyn1Id(pdyn1Id);
        exportedId.setPdyn2Id(pdyn2Id);
        mapper.save(exportedId, counter.getAndIncrement());
    }
}
