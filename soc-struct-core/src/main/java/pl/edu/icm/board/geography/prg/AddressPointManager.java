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

package pl.edu.icm.board.geography.prg;

import com.google.common.collect.ImmutableSetMultimap;
import com.google.common.collect.SetMultimap;
import it.unimi.dsi.fastutil.ints.IntArrays;
import net.snowyhollows.bento.annotation.WithFactory;
import org.apache.commons.math3.random.RandomGenerator;
import pl.edu.icm.board.geography.KilometerGridCell;
import pl.edu.icm.board.geography.StreetNameNormalizer;
import pl.edu.icm.board.geography.commune.Commune;
import pl.edu.icm.board.geography.commune.CommuneManager;
import pl.edu.icm.board.geography.prg.model.AddressLookupResult;
import pl.edu.icm.board.geography.prg.model.AddressPoint;
import pl.edu.icm.board.geography.prg.model.IndexedAddressPoint;
import pl.edu.icm.board.geography.prg.model.IndexedAddressPointMapper;
import pl.edu.icm.board.geography.prg.model.LookupPrecision;
import pl.edu.icm.em.socstruct.component.geo.Location;
import pl.edu.icm.board.util.FileCacheService;
import pl.edu.icm.board.util.RandomProvider;
import pl.edu.icm.trurl.store.Store;
import pl.edu.icm.trurl.store.array.ArrayStore;
import pl.edu.icm.trurl.store.basic.BasicAttributeFactory;
import pl.edu.icm.trurl.util.Status;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class AddressPointManager {
    private final SetMultimap<String, KilometerGridCell> postalCodesToGrid;
    private final CommuneManager communeManager;
    private final RandomGenerator random;
    private final StreetNameNormalizer streetNameNormalizer;
    private final Store addressPointsStore = new Store(new BasicAttributeFactory(), 1024);
    private final int[] index;
    private final IndexedAddressPointDao indexedAddressPointDao = new IndexedAddressPointDao(null);

    @WithFactory
    public AddressPointManager(AddressPointSource addressPointSource,
                               FileCacheService fileCacheService,
                               CommuneManager communeManager,
                               RandomProvider randomProvider,
                               StreetNameNormalizer streetNameNormalizer) {
        this.communeManager = communeManager;
        this.random = randomProvider.getRandomGenerator(AddressPointManager.class);
        this.streetNameNormalizer = streetNameNormalizer;
        indexedAddressPointDao.configureStore(addressPointsStore);
        fileCacheService.computeIfAbsent("address_points", addressPointsStore, addressPointSource::load);
        indexedAddressPointDao.attachStore(addressPointsStore);
        index = IntStream.range(0, addressPointsStore.getCount()).toArray();
        postalCodesToGrid = extractPostalCodes();
        index();
    }

    private SetMultimap<String, KilometerGridCell> extractPostalCodes() {
        Status status = Status.of("extracting postal codes", index.length / 50000);
        var postalCodesToGrid = streamAddressPoints()
                .filter(ap -> ap != null)
                .filter(ap -> ap.getPostalCode() != null)
                .collect(ImmutableSetMultimap.toImmutableSetMultimap(
                        AddressPoint::getPostalCode,
                        ap -> KilometerGridCell.fromPl1992ENMeters(ap.getEasting(), ap.getNorthing())));
        status.done();
        return postalCodesToGrid;
    }

    public AddressLookupResult lookup(String communeTeryt, String postalCode, String locality, String street, String streetNumber) {
        String needle = streetNameNormalizer.indexize(postalCode, locality, street, streetNumber);
        AddressLookupResult addressLookupResult = new AddressLookupResult();
        int indexPosition = IntArrays.binarySearch(index, -1, (a, unused) -> {  // -1 is unused
            String normA = indexedAddressPointDao.getNormalized(a);
            return normA.compareTo(needle);
        });

        if (indexPosition >= 0) {
            int id = index[indexPosition];
            addressLookupResult.setPrecision(LookupPrecision.PERFECT_MATCH);
            IndexedAddressPoint indexed = indexedAddressPointDao.createAndLoad(id);

            addressLookupResult.setAddressPoint(indexed.getAddressPoint());
            addressLookupResult.setLocation(Location.fromEquiarealENMeters(
                    indexed.getAddressPoint().getEasting(), indexed.getAddressPoint().getNorthing()));

            return addressLookupResult;
        } else {
            addressLookupResult.setPrecision(LookupPrecision.POSTAL_CODE);
            List<KilometerGridCell> cells = new ArrayList<>(postalCodesToGrid.get(postalCode));
            if (cells.size() == 0) {
                addressLookupResult.setPrecision(LookupPrecision.COMMUNE);
                cells
                        .addAll(communeManager.communeForTeryt(communeTeryt).map(Commune::getCells).orElse(Collections.emptySet()));
            }
            if (cells.size() == 0) {
                addressLookupResult.setPrecision(LookupPrecision.FAILURE);
                addressLookupResult.setLocation(KilometerGridCell.fromLegacyPdynCoordinates(200, 200).toLocation());
                return addressLookupResult;
            }
            int idx = (int) (random.nextDouble() * cells.size());
            addressLookupResult.setLocation(cells.get(idx).toLocation());
            return addressLookupResult;
        }
    }

    public Stream<AddressPoint> streamAddressPoints() {
        return streamIndexedAddressPoints()
                .map(IndexedAddressPoint::getAddressPoint);
    }

    public Stream<IndexedAddressPoint> streamIndexedAddressPoints() {
        return IntStream.range(0, addressPointsStore.getCount())
                .mapToObj((idx) -> indexedAddressPointDao.createAndLoad(idx));
    }

    private void index() {
        var status = Status.of("Indexing PRG database");

        IntArrays.parallelQuickSort(index, (a, b) -> {
            String normA = indexedAddressPointDao.getNormalized(a);
            String normB = indexedAddressPointDao.getNormalized(b);
            return normA.compareTo(normB);
        });

        status.done();
    }
}
