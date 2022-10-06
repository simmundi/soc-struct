package pl.edu.icm.board.pdyn1;

import net.snowyhollows.bento.annotation.WithFactory;
import pl.edu.icm.trurl.io.orc.OrcStoreService;
import pl.edu.icm.trurl.store.Store;
import pl.edu.icm.trurl.store.array.ArrayStore;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

public class PdynIdExporter {

    private ExportedIdMapper mapper;
    private AtomicInteger counter;
    private Store store;

    @WithFactory
    public PdynIdExporter () {
    }

    public void create(int capacity) {
        counter = new AtomicInteger(0);
        mapper = new ExportedIdMapper();
        store = new ArrayStore(capacity);
        mapper.configureAndAttach(store);
    }

    public void saveToFile(String dir) {
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
