package pl.edu.icm.board.geography.gis;

import org.geotools.data.simple.SimpleFeatureSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import pl.edu.icm.board.geography.KilometerGridCell;
import pl.edu.icm.trurl.store.Store;
import pl.edu.icm.trurl.store.array.ArrayStore;

import java.io.IOException;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CommuneSourceTest {

    @Mock
    GisUtilsService gisUtilsService;

    @Mock
    CommuneGisReader communeGisReader;

    @Mock
    SimpleFeatureSource featureSource;

    @Mock
    SimpleFeatureType simpleFeatureType;

    @Mock
    FeaturesInEnvelope empty;

    @Mock
    FeaturesInEnvelope featuresA;

    @Mock
    FeaturesInEnvelope featuresB;

    @Spy
    Store store = new ArrayStore();

    @Mock
    SimpleFeature featureA;

    @Mock
    SimpleFeature featureB;

    CommuneSource communeSource;

    @BeforeEach
    void before() throws IOException {

        communeSource = new CommuneSource(gisUtilsService, communeGisReader, 1, 3);

        when(communeGisReader.communes()).thenReturn(featureSource);
        when(empty.isEmpty()).thenReturn(true);
        when(featuresA.getFeatureWithMostArea()).thenReturn(featureA);
        when(featuresB.getFeatureWithMostArea()).thenReturn(featureB);
        when(featureA.getAttribute("JPT_KOD_JE")).thenReturn("a_kod");
        when(featureA.getAttribute("JPT_NAZWA_")).thenReturn("a_name");
        when(featureB.getAttribute("JPT_KOD_JE")).thenReturn("b_kod");
        when(featureB.getAttribute("JPT_NAZWA_")).thenReturn("b_name");

        when(gisUtilsService.findFeaturesInCell(
                featureSource,
                KilometerGridCell.fromLegacyPdynCoordinates(0, 0))).thenReturn(empty);
        when(gisUtilsService.findFeaturesInCell(
                featureSource,
                KilometerGridCell.fromLegacyPdynCoordinates(0, 1))).thenReturn(featuresA);
        when(gisUtilsService.findFeaturesInCell(
                featureSource,
                KilometerGridCell.fromLegacyPdynCoordinates(0, 2))).thenReturn(featuresB);
    }

    @Test
    void load() throws IOException {
        // execute
        communeSource.load(store);

        // assert
        assertThat(store.getCount()).isEqualTo(3);
        assertThat(store.get("n").getString(0)).isEqualTo("875");
        assertThat(store.get("n").getString(1)).isEqualTo("874");
        assertThat(store.get("n").getString(2)).isEqualTo("873");
        assertThat(store.get("e").getString(0)).isEqualTo("71");
        assertThat(store.get("e").getString(1)).isEqualTo("71");
        assertThat(store.get("e").getString(2)).isEqualTo("71");
        assertThat(store.get("teryt").getString(0)).isEqualTo("0000000");
        assertThat(store.get("teryt").getString(1)).isEqualTo("a_kod");
        assertThat(store.get("teryt").getString(2)).isEqualTo("b_kod");
        assertThat(store.get("name").getString(0)).isEqualTo("leones");
        assertThat(store.get("name").getString(1)).isEqualTo("a_name");
        assertThat(store.get("name").getString(2)).isEqualTo("b_name");
    }

}
