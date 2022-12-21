/*
 * Copyright (c) 2022 ICM Epidemiological Model Team at Interdisciplinary Centre for Mathematical and Computational Modelling, University of Warsaw.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *
 */

package pl.edu.icm.board.util;

import net.snowyhollows.bento.annotation.WithFactory;
import net.snowyhollows.bento.config.WorkDir;
import pl.edu.icm.trurl.io.orc.OrcStoreService;
import pl.edu.icm.trurl.store.Store;

import java.io.File;
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
    private final String rootPath;

    public FileCacheService(OrcStoreService orcStoreService, String rootPath) {
        this.orcStoreService = orcStoreService;
        this.rootPath = rootPath;
    }

    @WithFactory
    public FileCacheService(String rootPath) {
        this(new OrcStoreService(), rootPath);
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
            String fileName = new File(rootPath, String.format("%s/%s.%s", cacheDirName, name, "orc"))
                    .getAbsolutePath();
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
