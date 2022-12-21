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
