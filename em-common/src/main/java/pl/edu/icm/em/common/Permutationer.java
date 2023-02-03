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

package pl.edu.icm.em.common;

import java.util.*;

public class Permutationer<T> {
    private final List<T> itemsArray;
    private int mask = 0;
    private final int[] result;
    private final List<T> ordered;
    private final List<T> wrappedOrdered;

    private Permutationer(Collection<T> items) {
        itemsArray = new ArrayList<>(items);
        ordered = new ArrayList<>(items);
        result = new int[items.size()];
        wrappedOrdered = Collections.unmodifiableList(ordered);
    }

    public List<T> permutation(long index) {
        int size = itemsArray.size();
        
        for (int i = 0; i < size; i++) {
            long base = i + 1;
            long reminder = index % base;
            index /= base;
            result[result.length - i - 1] = (int)reminder;
        }

        for (int i = 0; i < size; i++) {
            ordered.set(i, itemsArray.get(i));
        }

        for (int i = 0; i < size; i++) {
            int swapTargetIdx = i + result[i];
            Collections.swap(ordered, i, swapTargetIdx);
        }

        return wrappedOrdered;
    }

    public static <T> Permutationer<T> of(Collection<T> items) {
        if (items.size() > 20) throw new IllegalArgumentException("Size must be 20 or less, was " + items.size());
        return new Permutationer<>(items);
    }

}
