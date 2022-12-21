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

package pl.edu.icm.board.geography;

import com.google.common.base.Joiner;
import net.snowyhollows.bento.annotation.WithFactory;

import java.util.Arrays;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class StreetNameNormalizer {

    private final Joiner JOINER = Joiner.on('|');
    private final Pattern REPLACEMENTS = Pattern.compile("[\",./\\-]");
    private final Pattern SPLIT = Pattern.compile("\\s+");
    private final static Set<String> SPAM = Set.of(
            "ul",
            "aleja",
            "ulica",
            "gen",
            "generała",
            "mjr",
            "prof",
            "profesora",
            "dr",
            "dra",
            "doktora",
            "prymasa",
            "ks",
            "księdza",
            "marszałka",
            "harcmistrza",
            "pułkownika",
            "świętego",
            "świętej",
            "ojca",
            "króla",
            "królowej",
            "majora",
            "kaprala",
            "podchorążego",
            "chorążego",
            "papieża",
            "księcia",
            "księżnej",
            "św",
            "bł",
            "błogosławionej",
            "błogosławionego"
    );

    @WithFactory
    public StreetNameNormalizer() {
    }

    public String normalizeStreet(String streetName) {
        String lowerCase = streetName.toLowerCase(Locale.ROOT);
        String punctuationRemoved = REPLACEMENTS.matcher(lowerCase).replaceAll(" ");

        return Arrays.stream(SPLIT.split(punctuationRemoved))
                .filter(segment -> !SPAM.contains(segment))
                .sorted()
                .collect(Collectors.joining(" "));
    }

    public String indexize(String postalCode, String locality, String street, String streetNumber) {
        return JOINER.join (
                postalCode,
                locality,
                normalizeStreet(street),
                streetNumber
        );
    }
}
