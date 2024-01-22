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

package pl.edu.icm.em.common.math.pdf;

import net.snowyhollows.bento.category.Category;
import net.snowyhollows.bento.category.CategoryManager;

import java.util.Arrays;

/**
 * Represents a mapping from a closed number of items (labels, outcomes, bins, ranges) to floats,
 * along with methods to set, get, sample, shift, scale and normalize the values.
 *
 * The labels are represented by instances of a single Enum.
 *
 * SoftEnumDiscretePDF can be used to build and represent a discrete probability distribution.
 *
 * @param <Label>
 */
public class SoftEnumDiscretePDF<Label extends Category> {
    private final float[] values;
    private final CategoryManager<Label> softEnumManager;
    private boolean normalized;

    public SoftEnumDiscretePDF(CategoryManager<Label> softEnumManager) {
        values = new float[softEnumManager.values().size()];
        this.softEnumManager = softEnumManager;
        normalized = false;
    }

    /**
     * Changes the value associated with the label by an offset.
     *
     * @param label
     * @param offset
     */
    public void shift(Label label, float offset) {
        values[label.ordinal()] += offset;
        normalized = false;
    }

    /**
     * Sets the value associated with the label.
     *
     * @param label
     * @param value
     */
    public void set(Label label, float value) {
        values[label.ordinal()] = value;
        normalized = false;
    }

    /**
     * Sets all the values to zero.
     *
     */
    public void clear() {
        Arrays.fill(values, 0);
    }

    /**
     * Scales down the value associated with the label by a given ratio
     * and increases the value of the second one, so that DiscretePDF remains normalized (if was normalized before).
     *
     * @param ratio float from range <0..1>
     * @return the value after scaling
     */
    public float scaleAndCompensate(Label toBeScaled, float ratio, Label toBeCompensated) {
        if (ratio < 0 || ratio > 1) {
            throw new IllegalArgumentException("ratio = " + ratio + " is out of range <0..1>");
        }
        if (values[toBeScaled.ordinal()] != 0.0f && values[toBeCompensated.ordinal()] != 0.0f) {
            float oldValue = values[toBeScaled.ordinal()];
            float newValue = oldValue * ratio;
            values[toBeScaled.ordinal()] = newValue;
            values[toBeCompensated.ordinal()] += (oldValue - newValue);
            return newValue;
        } else {
            return 0;
        }
    }

    /**
     * Normalizes the values, so that their sum equals 1
     */
    public void normalize() {
        float sum = total();
        if (sum == 0.0f) throw new IllegalStateException("Cannot normalize an empty DiscretePDF");
        sum = (float) Math.round(sum * 1000000f) / 1000000f;
        if (sum == 1.0f) {
            normalized = true;
            return;
        }
        float factor = 1.0f / sum;
        for (int i = 0; i < values.length; i++) {
            values[i] *= factor;
        }
        normalized = true;
    }

    /**
     * Returns the total of all the values.
     *
     * @return sum of probabilities
     */
    public float total() {
        float sum = 0.0f;
        for (int i = 0; i < values.length; i++) {
            sum += values[i];
        }
        return sum;
    }


    /**
     * Samples a label based on a semi-random number between <0..1). The sampling
     * is weighted by the values associated with the labels (i.e. a label with twice
     * the value will be associated with twice as many float inputs).
     *
     * Before sampling the DiscretePDF must be explicitly normalized.
     *
     * @param random value from range <0..1)
     * @return Label of an value
     */
    public Label sample(double random) {
        if (!normalized) {
            throw new IllegalStateException("Cannot sample without normalization");
        }
        if (random >= 1 || random < 0) throw new IllegalArgumentException("Random should be a value from range <0..1)");
        float cumulativeProbability = 0.0f;
        Label value = null;
        for (int i = 0; i < values.length; i++) {
            if (values[i] != 0.0f) {
                value = softEnumManager.getByOrdinal(i);
                cumulativeProbability += values[i];
                if (random < cumulativeProbability) break;
            }
        }
        return value;
    }

    /**
     * Samples an outcome from EnumSampleSpace.
     * EnumSampleSpace does not have to be normalized before.
     * Returns defaultOutcome when nothing else matches given random value.
     *
     * @param random value
     * @return Label of an outcome
     */
    public Label sampleUnnormalized(double random) {
        float cumulativeProbability = 0.0f;
        Label value = null;
        for (int i = 0; i < values.length; i++) {
            if (values[i] != 0.0f) {
                value = softEnumManager.getByOrdinal(i);
                cumulativeProbability += values[i];
                if (random < cumulativeProbability) break;
            }
        }
        return value;
    }

    /**
     * Checks if EnumSampleSpace is normalized.
     *
     * @return true if EnumSampleSpace is normalized
     */
    public boolean isNormalized() {
        float sum = total();
        sum = (float) Math.round(sum * 1000000f) / 1000000f;
        if (sum == 1.0f) {
            normalized = true;
        }
        return normalized;
    }

    /**
     * Checks if EnumSampleSpace is empty.
     *
     * @return true if EnumSampleSpace is empty
     */
    public boolean isEmpty() {
        for (int i = 0; i < values.length; i++) {
            if (values[i] != 0.0f) {
                return false;
            }
        }
        return true;
    }

    /**
     * Checks if DiscretePDF contains non-zero value for a given label.
     *
     * @return true if EnumSampleSpace contains non-zero probability value for a given outcome
     */
    public boolean isNonZero(Label label) {
        return values[label.ordinal()] != 0.0f;
    }

    /**
     * Returns the value associated with the given label.
     *
     * @param label
     * @return probability
     */
    public float get(Label label) {
        return values[label.ordinal()];
    }

    /**
     * Performs multiplication of each value by its counterpart (sharing the same label)
     * from the other discretePDF
     *
     * @param discretePDF
     */
    public void multiply(SoftEnumDiscretePDF<Label> discretePDF) {
        for (int i = 0; i < values.length; i++) {
            values[i] *= discretePDF.values[i];
        }
        normalized = false;
    }

    /**
     * Becomes a copy of the given discretePDF.
     *
     * @param discretePDF
     */
    public void copy(SoftEnumDiscretePDF<Label> discretePDF) {
        for (int i = 0; i < values.length; i++) {
            values[i] = discretePDF.values[i];
        }
        normalized = discretePDF.normalized;
    }

    @Override
    public String toString() {
        return "discretePDF{" +
                "values=" + Arrays.toString(values) +
                '}';
    }
}
