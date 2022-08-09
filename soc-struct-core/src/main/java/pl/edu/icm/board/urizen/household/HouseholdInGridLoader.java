package pl.edu.icm.board.urizen.household;

import com.google.common.collect.ConcurrentHashMultiset;
import net.snowyhollows.bento.annotation.WithFactory;
import net.snowyhollows.bento.config.WorkDir;
import pl.edu.icm.board.urizen.household.fileformat.RcbCovidDane02;
import pl.edu.icm.board.urizen.household.model.RcbCovidDane02Aggregate;
import pl.edu.icm.trurl.csv.CsvReader;
import pl.edu.icm.trurl.ecs.mapper.Mapper;
import pl.edu.icm.trurl.ecs.mapper.Mappers;
import pl.edu.icm.trurl.store.Store;
import pl.edu.icm.trurl.store.array.ArrayStore;
import pl.edu.icm.trurl.util.Status;

import java.io.File;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class HouseholdInGridLoader {
    private final String gmHouseholdGridFilename;
    private final CsvReader csvReader;
    private final WorkDir workDir;

    @WithFactory
    public HouseholdInGridLoader(String gmHouseholdGridFilename,
                                 CsvReader csvReader, WorkDir workDir) {
        this.csvReader = csvReader;
        this.gmHouseholdGridFilename = gmHouseholdGridFilename;
        this.workDir = workDir;
    }

    public void load(Store store) {
        Mapper<RcbCovidDane02> csvMapper = Mappers.create(RcbCovidDane02.class);
        Mapper<RcbCovidDane02Aggregate> aggregateMapper = Mappers.create(RcbCovidDane02Aggregate.class);
        aggregateMapper.configureStore(store);
        aggregateMapper.attachStore(store);

        Store tmpStore = new ArrayStore(12_000_000);
        Status sts = Status.of("Loading household data from " + gmHouseholdGridFilename);
        csvMapper.configureStore(tmpStore);
        csvReader.load(workDir.openForReading(new File(gmHouseholdGridFilename)), tmpStore);
        csvMapper.attachStore(tmpStore);

        AtomicInteger idx = new AtomicInteger();
        Mappers.stream(csvMapper).parallel().collect(Collectors.toCollection(ConcurrentHashMultiset::create))
                .entrySet().stream()
                .map(entry -> new RcbCovidDane02Aggregate(entry.getElement(), entry.getCount()))
                .forEach(aggregate -> aggregateMapper.save(aggregate, idx.getAndIncrement()));

        store.fireUnderlyingDataChanged(0, idx.get());
        sts.done();
    }
}

