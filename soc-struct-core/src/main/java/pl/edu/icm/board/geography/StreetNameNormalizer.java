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
