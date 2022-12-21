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

package pl.edu.icm.board.geography.gis;

import org.assertj.core.api.*;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.geometry.Envelope2D;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.GeometryDescriptor;
import org.opengis.feature.type.Name;
import org.opengis.filter.FilterFactory2;
import org.opengis.filter.expression.PropertyName;
import org.opengis.filter.spatial.BBOX;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.cs.AxisDirection;
import org.opengis.referencing.cs.CoordinateSystem;
import org.opengis.referencing.cs.CoordinateSystemAxis;
import pl.edu.icm.board.geography.KilometerGridCell;

import java.io.IOException;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GisUtilsServiceTest {

    @Mock
    SimpleFeatureType schema;

    @Mock
    FilterFactory2 filterFactory;

    @Mock
    Name geometryName;

    @Mock
    CoordinateReferenceSystem coordinateReferenceSystem;

    @Mock
    GeometryDescriptor geometryDescriptor;

    @Mock
    CoordinateSystem coordinateSystem;

    @Mock
    SimpleFeatureSource features;

    @Mock
    SimpleFeatureCollection filteredFeatures;

    @Mock
    SimpleFeatureIterator iterator;

    @Mock
    SimpleFeature featureA;

    @Mock
    SimpleFeature featureB;

    @Mock
    PropertyName propertyName;

    @InjectMocks
    GisUtilsService gisUtilsService;

    @Mock
    CoordinateSystemAxis n;

    @Mock
    CoordinateSystemAxis e;

    @Mock
    BBOX bbox;

    @Captor
    ArgumentCaptor<Envelope2D> envelopeCaptor;

    @BeforeEach
    void before() {
        when(e.getDirection()).thenReturn(AxisDirection.EAST);
    }


    @Test
    void findFeaturesInEnvelope__one_feature() throws IOException {

        // given
        when(schema.getCoordinateReferenceSystem()).thenReturn(coordinateReferenceSystem);
        when(coordinateReferenceSystem.getCoordinateSystem()).thenReturn(coordinateSystem);
        when(coordinateSystem.getAxis(0)).thenReturn(n);
        when(coordinateSystem.getAxis(1)).thenReturn(e);
        when(n.getDirection()).thenReturn(AxisDirection.NORTH);
        when(coordinateSystem.getDimension()).thenReturn(2);
        when(features.getSchema()).thenReturn(schema);
        when(geometryDescriptor.getName()).thenReturn(geometryName);
        when(schema.getGeometryDescriptor()).thenReturn(geometryDescriptor);

        when(filterFactory.property(geometryName)).thenReturn(propertyName);
        when(filterFactory.bbox(eq(propertyName), envelopeCaptor.capture())).thenReturn(bbox);
        when(features.getFeatures(bbox)).thenReturn(filteredFeatures);
        when(filteredFeatures.features()).thenReturn(iterator);
        when(iterator.hasNext()).thenReturn(true, false);
        when(iterator.next()).thenReturn(featureA);

        // execute
        var res = gisUtilsService.findFeaturesInCell(
                features,
                KilometerGridCell.fromLegacyPdynCoordinates(0, 0));

        // assert
        assertThat(res.getFeatureWithMostArea()).isEqualTo(featureA);
    }

    @Test
    @DisplayName("Should throw upon an invalid CRS")
    void findFeaturesInEnvelope__invalid_crs() {
        // given
        when(features.getSchema()).thenReturn(schema);
        when(schema.getCoordinateReferenceSystem()).thenReturn(coordinateReferenceSystem);
        when(coordinateReferenceSystem.getCoordinateSystem()).thenReturn(coordinateSystem);
        when(coordinateSystem.getAxis(0)).thenReturn(e);

        // execute
        Assertions.assertThatThrownBy(() -> gisUtilsService.findFeaturesInCell(
                features,
                KilometerGridCell.fromLegacyPdynCoordinates(0, 0)))
                .isInstanceOf(IllegalStateException.class);
    }

}
