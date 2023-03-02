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

import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Stream;

/**
 * Collection of discrete-valued Bins with the same Label type, which can be sampled.
 *
 * It is meant to describe counts of uniform items, e.g. a Histogram labelled by AgeRange is
 * a good representation of social structure.
 *
 * <p>
 * The bins within a single Histogram can be accessed by a uniform value <0..1), weighted
 * by the current count of the bins, which allows us to quickly sample the histogram
 * (i.e. pick a semi-random LABEL value weighted by its bin's size) and
 * implement drawing without returning (because after sampling a Bin we can call pick, to decrease its count).
 * <p>
 * For example we can use a Histogram to emulate drawing of 100 random pieces of fruit from a fruit basket:
 *
 * <pre>{@code
 *     Histogram<String> basket = new Histogram<>();
 *     basket.add("Banana", 50);
 *     basket.add("Apple", 50);
 * int i = 0;
 *     while (basket.getTotalCount() > 0) {
 *         var f = basket.sample(Math.random());
 *         String result = (i++) + " " + f.pick();
 *         System.out.println(result);
 *     }
 * }
 * }</pre>
 *
 * @param <Label>
 */
public class Histogram<Label> {
    private final Shelf<Label> shelf;
    private int totalCount;
    private int initialCount;

    public Histogram() {
        this(500);
    }

    public Histogram(int shelfSize) {
        shelf = new Shelf<>(shelfSize);
    }

    /**
     * Creates and adds a Bin
     *
     * @param label
     * @param count
     * @return the freshly created bin
     */
    public Bin<Label> add(Label label, int count) {
        Bin<Label> bin = new Bin<>(label, count);
        addBin(bin);
        return bin;
    }

    /**
     * Adds any given Bin to the Histogram
     *
     * @param bin
     */
    public void addBin(Bin<Label> bin) {
        shelf.add(bin);
        bin.addListener((difference) -> totalCount += difference);
        totalCount += bin.getCount();
        initialCount += bin.getInitialCount();
    }

    /**
     * Returns the bin which would contain the argument if all
     * the bins of the were represented by segments of length <i>count</i>,
     * joined one to another on the same line and their sum of lengths normalized to one.
     * <p>
     * e.g. if we have a histogram containing bins: 51 apples, 39 bananas and 10 grapes,
     * value 0 will return the bin representing apples, 0.5 will return the bin
     * representing bananas, and values above 0.9 will return the grapes.
     *
     * @param random double from range <0..1)
     * @return Bin
     */
    public Bin<Label> sample(double random) {
        if (totalCount == 0) {
            shelf.reset();
        }

        int index = (int) Math.floor(totalCount * random);
        return shelf.find(index);
    }

    /**
     * Works like {@see sample}, but the number given is not normalized
     * to one.
     * <p>
     * e.g. if we have a histogram containing bins: 51 apples, 39 bananas and 10 grapes,
     * values between 0 and 50 will the bin representing apples, 51-89 will return the bin
     * representing bananas, and values between 90 and 99 will return the grapes.
     *
     * @param index
     * @return
     */
    public Bin<Label> sampleNth(int index) {
        if (initialCount == 0) {
            throw new IllegalStateException("no bins in the histogram");
        }

        if (totalCount == 0) {
            shelf.reset();
        }
        return shelf.find(index);
    }

    /**
     * Creates a subset of the histogram, containing only the bin instances
     * with matching labels. The bins in the new histogram are shared with
     * this one, picking elements from one will remove them also from the other.
     *
     * @param labels - collection of labels
     * @return the new Histogram
     */
    public Histogram<Label> createSubHistogram(Collection<Label> labels) {
        Histogram<Label> subHistogram = new Histogram<>();

        shelf.streamBins()
                .filter(bin -> labels.contains(bin.getLabel()))
                .forEach(bin -> subHistogram.addBin(bin));

        return subHistogram;
    }

    /**
     * Creates a subset of the histogram, containing only the bin instances
     * with matching labels. The bins in the new histogram are shared with
     * this one, picking elements from one will remove them also from the other.
     *
     * @param labels - array of labels
     * @return the new Histogram
     */
    public Histogram<Label> createSubHistogram(Label... labels) {
        return createSubHistogram(Arrays.asList(labels));
    }

    /**
     * returns sum of current counts of all bins
     */
    public int getTotalCount() {
        return totalCount;
    }

    /**
     * Streams all the bins
     *
     * @return
     */
    public Stream<Bin<Label>> streamBins() {
        return this.shelf.streamBins();
    }

    static class Shelf<Label> {
    private int currentTotalInShelf;
    private int lastIndex = 0;
    private final Bin<Label>[] bins;
    private final Shelf<Label> previous;
    private Shelf<Label> next;

    public Shelf(int size) {
        this(null, size);
    }

    private Shelf(Shelf<Label> previous, int size) {
        this.previous = previous;
        this.bins = new Bin[size];
    }

    public Shelf add(Bin<Label> bin) {
        if (lastIndex < bins.length) {
            bins[lastIndex++] = bin;
            currentTotalInShelf += bin.getCount();
            bin.addListener( difference -> {
                currentTotalInShelf += difference;
            });
            return this;
        } else {
            if (next == null) {
                next = new Shelf<>(this, bins.length);
            }
            next.add(bin);
            return next;
        }
    }

    public Bin<Label> find(int count) {
        if (count >= currentTotalInShelf) {
            return next.find(count - currentTotalInShelf);
        }
        int acc = 0;
        for (Bin<?> bin : bins) {
            acc += bin.getCount();
            if (count < acc) {
                return (Bin<Label>) bin;
            }
        }
        throw new IllegalStateException();
    }

    public int getTotal() {
        int nextTotal = next == null ? 0 : next.getTotal();
        return nextTotal + currentTotalInShelf;
    }

    public void reset() {
        for (int i = 0; i < lastIndex; i++) {
            bins[i].reset();
        }
        if (next != null) {
            next.reset();
        }
    }

    public Stream<Bin<Label>> streamBins() {
        Stream<Bin<Label>> myBins = Arrays.stream(bins, 0, lastIndex);
        if (next == null) {
            return myBins;
        } else {
            return Stream.concat(myBins, next.streamBins());
        }
    }
}
}
