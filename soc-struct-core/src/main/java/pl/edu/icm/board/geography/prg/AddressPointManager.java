package pl.edu.icm.board.geography.prg;

import com.google.common.collect.ImmutableSetMultimap;
import com.google.common.collect.SetMultimap;
import it.unimi.dsi.fastutil.ints.IntArrays;
import net.snowyhollows.bento2.annotation.WithFactory;
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
import pl.edu.icm.board.model.Location;
import pl.edu.icm.board.util.FileCacheService;
import pl.edu.icm.board.util.RandomProvider;
import pl.edu.icm.trurl.store.Store;
import pl.edu.icm.trurl.store.array.ArrayStore;
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
    private final Store addressPointsStore = new ArrayStore();
    private final int[] index;
    private final IndexedAddressPointMapper indexedAddressPointMapper = new IndexedAddressPointMapper();

    @WithFactory
    public AddressPointManager(AddressPointSource addressPointSource,
                               FileCacheService fileCacheService,
                               CommuneManager communeManager,
                               RandomProvider randomProvider,
                               StreetNameNormalizer streetNameNormalizer) {
        this.communeManager = communeManager;
        this.random = randomProvider.getRandomGenerator(AddressPointManager.class);
        this.streetNameNormalizer = streetNameNormalizer;
        indexedAddressPointMapper.configureStore(addressPointsStore);
        fileCacheService.computeIfAbsent("address_points", addressPointsStore, addressPointSource::load);
        indexedAddressPointMapper.attachStore(addressPointsStore);
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
            String normA = indexedAddressPointMapper.getNormalized(a);
            return normA.compareTo(needle);
        });

        if (indexPosition >= 0) {
            int id = index[indexPosition];
            addressLookupResult.setPrecision(LookupPrecision.PERFECT_MATCH);
            IndexedAddressPoint indexed = indexedAddressPointMapper.createAndLoad(id);

            addressLookupResult.setAddressPoint(indexed.getAddressPoint());
            addressLookupResult.setLocation(Location.fromPl1992MeterCoords(
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
                .mapToObj((idx) -> indexedAddressPointMapper.createAndLoad(idx));
    }

    private void index() {
        var status = Status.of("Indexing PRG database");

        IntArrays.parallelQuickSort(index, (a, b) -> {
            String normA = indexedAddressPointMapper.getNormalized(a);
            String normB = indexedAddressPointMapper.getNormalized(b);
            return normA.compareTo(normB);
        });

        status.done();
    }
}
