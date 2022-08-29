package pl.edu.icm.board.pdyn1;

import com.google.common.base.Preconditions;
import pl.edu.icm.trurl.io.orc.OrcStoreService;
import pl.edu.icm.trurl.store.Store;
import pl.edu.icm.trurl.store.array.ArrayStore;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

public class PdynIdExporter {

    private ExportedIdMapper mapper;
    private AtomicInteger counter;
    private ArrayStore store;

    public PdynIdExporter (int capacity) {
        counter = new AtomicInteger(0);
        mapper = new ExportedIdMapper();
        Store store = new ArrayStore(capacity);
        mapper.configureAndAttach(store);
    }

    public void export(String dir){
        try {
            OrcStoreService orcStoreService = new OrcStoreService();
            orcStoreService.write(store, dir);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    public void saveIdMapping(int pdyn1Id, int pdyn2Id) {
        var exportedId = new ExportedId();
        mapper.save(exportedId, counter.getAndIncrement());
    }
}
