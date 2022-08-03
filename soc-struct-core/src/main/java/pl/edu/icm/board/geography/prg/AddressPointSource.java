package pl.edu.icm.board.geography.prg;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;
import net.snowyhollows.bento.annotation.WithFactory;
import net.snowyhollows.bento.config.WorkDir;
import pl.edu.icm.board.geography.KilometerGridCell;
import pl.edu.icm.board.geography.StreetNameNormalizer;
import pl.edu.icm.board.geography.prg.model.IndexedAddressPoint;
import pl.edu.icm.trurl.ecs.mapper.Mappers;
import pl.edu.icm.trurl.store.Store;
import pl.edu.icm.trurl.util.Status;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.atomic.AtomicInteger;

public class AddressPointSource {

    private final XMLInputFactory xmlInputFactory = XMLInputFactory.newDefaultFactory();
    private StreetNameNormalizer streetNameNormalizer;
    private final String prgInputDir;
    private final WorkDir filesystem;
    private final SetMultimap<String, KilometerGridCell> postalCodesToLocations = HashMultimap.create();

    @WithFactory
    public AddressPointSource(StreetNameNormalizer streetNameNormalizer, String prgInputDir, WorkDir filesystem) {
        this.streetNameNormalizer = streetNameNormalizer;
        this.prgInputDir = prgInputDir;
        this.filesystem = filesystem;
    }

    public void load(Store addressPointStore) {
        var mapper = Mappers.create(IndexedAddressPoint.class);
        mapper.configureStore(addressPointStore);
        mapper.attachStore(addressPointStore);

        File dir = new File(prgInputDir);

        AtomicInteger id = new AtomicInteger();

        for (File source : filesystem.listFiles(dir, f -> f.getName().endsWith("xml"))) {
            var status = Status.of("Processing " + source, 50000);
            try (InputStream stream = new BufferedInputStream(filesystem.openForReading(source))) {
                new PrgParser(xmlInputFactory.createXMLEventReader(stream))
                        .parse(addressPoint -> {
                            String normalizedStreet = streetNameNormalizer.normalizeStreet(addressPoint.getStreet());
                            mapper.save(new IndexedAddressPoint(
                                    streetNameNormalizer.indexize(addressPoint.getPostalCode(), addressPoint.getLocality(), normalizedStreet, addressPoint.getNumber()),
                                    streetNameNormalizer.normalizeStreet(addressPoint.getStreet()),
                                    addressPoint
                            ), id.getAndIncrement());
                            postalCodesToLocations.put(
                                    addressPoint.getPostalCode(),
                                    KilometerGridCell.fromPl1992ENMeters(addressPoint.getEasting(), addressPoint.getNorthing()));
                            status.tick();
                        });
            } catch (XMLStreamException | IOException e) {
                throw new IllegalStateException("Error creating address point database from PRG", e);
            }
            status.done();
        }
        addressPointStore.fireUnderlyingDataChanged(0, id.get());
    }
}
