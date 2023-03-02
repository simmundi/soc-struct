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

package pl.edu.icm.em.common.math.histogram;

import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import com.google.common.collect.Multimaps;
import pl.edu.icm.trurl.util.Pair;

import java.util.Map;
import java.util.function.Function;
import java.util.function.ToIntFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Represents a set of bins and slices. A slice is a Histogram which only
 * contains a subset of the bins.
 *
 * The purpose of such structure is sampling specific subsets of the dataset,
 * instead of int entirety. For example a dataset describing a social structure,
 * binned by age range and sex (e.g. bins contain 'males between 10-20') can
 * be sliced into 'males only', 'females only', 'people between 0-20' etc.The
 *
 *
 * The slices don't have to cover all the bins and they can (and usually do) overlap.
 *
 * @param <SLICE> type of identifier for a subset of Bins
 * @param <LABEL> type of identifier for a single Bin
 */
public final class BinnedDataset<SLICE, LABEL> {
    private final Histogram<LABEL> allBins;
    private final Map<SLICE, Histogram<LABEL>> slices;

    public BinnedDataset(Histogram<LABEL> allBins, Map<SLICE, Histogram<LABEL>> slices) {
        this.allBins = allBins;
        this.slices = slices;
    }

    public Histogram<LABEL> all() {
        return allBins;
    }

    public Map<SLICE, Histogram<LABEL>> getSlices() {
        return slices;
    }

    public Histogram<LABEL> slice(SLICE slice) {
        return slices.get(slice);
    }

    public static <SLICE, LABEL> BinnedDataset<SLICE, LABEL> binAndSlice(
            Stream<LABEL> labels,
            ToIntFunction<LABEL> countFromLabel,
            Function<LABEL, Stream<SLICE>> slicesFromLabel) {

        Histogram<LABEL> allBins = new Histogram<>();

        Multimap<SLICE, Bin<LABEL>> bins = labels
                .flatMap(entity -> {
                    int count = countFromLabel.applyAsInt(entity);
                    if (count <= 0) {
                        return Stream.empty();
                    } else {
                        Bin<LABEL> bin = allBins.add(entity, count);
                        return slicesFromLabel.apply(entity)
                                .map(s -> Pair.of(s, bin));
                    }
                }).collect(Multimaps.<Pair<SLICE, Bin<LABEL>>, SLICE, Bin<LABEL>, Multimap<SLICE, Bin<LABEL>>>toMultimap(
                        pair -> pair.first,
                        pair -> pair.second,
                        MultimapBuilder.hashKeys().arrayListValues()::build
                ));

        Map<SLICE, Histogram<LABEL>> slices = bins.keySet().stream()
                .collect(Collectors.toMap(
                        Function.identity(),
                        slice -> {
                            Histogram<LABEL> histogram = new Histogram<>();
                            bins.get(slice).forEach(histogram::addBin);
                            return histogram;
                        }
                ));

        return new BinnedDataset<>(allBins, slices);
    }
}
