package pl.edu.icm.board.util;

import net.snowyhollows.bento2.annotation.WithFactory;
import pl.edu.icm.trurl.io.orc.OrcStoreService;
import pl.edu.icm.trurl.store.Store;

import java.io.IOException;
import java.util.function.Consumer;

/**
 * Service meant to use as a common pattern, wherever columnar data is
 * eligible to caching, i.e. costly to create and deterministic, based on input.
 *
 */
public class FileCacheService {

    private final OrcStoreService orcStoreService;
    private final String cacheDirName = "output/cache";

    public FileCacheService(OrcStoreService orcStoreService) {
        this.orcStoreService = orcStoreService;
    }

    @WithFactory
    public FileCacheService() {
        this(new OrcStoreService());
    }

    /**
     * If file with the given name already exists, the contents is read back into the store.
     * If not, the data is fetched using the given provider function and, before returning,
     * stored for future use.
     *
     * @param name identifier for the cached value. It will be used as the name of the created cache,
     *             but this should be considered an implementation detail.
     * @param store store to fill with data
     * @param storeConsumer function which will be used to fill the store with data if no cached data is found
     */
    public void computeIfAbsent(String name, Store store, Consumer<Store> storeConsumer) {
        try {
            String fileName = String.format("%s/%s.%s", cacheDirName, name, "orc");
            if (orcStoreService.fileExists(fileName)) {
                orcStoreService.read(store, fileName);
            } else {
                storeConsumer.accept(store);
                orcStoreService.write(store, fileName);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
