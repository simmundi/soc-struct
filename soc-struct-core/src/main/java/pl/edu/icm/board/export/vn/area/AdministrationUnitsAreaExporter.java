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

package pl.edu.icm.board.export.vn.area;

import net.snowyhollows.bento.annotation.ByName;
import net.snowyhollows.bento.annotation.WithFactory;
import pl.edu.icm.board.geography.KilometerGridCell;
import pl.edu.icm.board.geography.commune.Commune;
import pl.edu.icm.board.geography.commune.CommuneManager;
import pl.edu.icm.trurl.visnow.VnAreaExporter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class AdministrationUnitsAreaExporter {
    private final String administrationMapFilename;
    private final CommuneManager communeManager;
    private Map<Commune, Short> communeToShortMap;

    @WithFactory
    public AdministrationUnitsAreaExporter(@ByName("soc-struct.export.visnow.administration-map-filename") String administrationMapFilename,
                                           CommuneManager communeManager) {
        this.administrationMapFilename = administrationMapFilename;
        this.communeManager = communeManager;
    }

    public void generateReports() throws IOException {
        int height = communeManager.getGridRows();
        int width = communeManager.getGridCols();
        KilometerGridCell cornerA = KilometerGridCell.fromLegacyPdynCoordinates(0, 0);
        KilometerGridCell cornerB = KilometerGridCell.fromLegacyPdynCoordinates(width, height);

        communeToShortMap = communeManager.getCommunes().stream()
                .collect(Collectors.toMap(
                        commune -> commune,
                        commune -> Short.parseShort(commune.getTeryt().substring(0, 2))));
        communeToShortMap.put(
                communeManager.communeAt(KilometerGridCell.fromLegacyPdynCoordinates(0, 0)), (short)0);

        int fromX = Math.min(cornerA.getE(), cornerB.getE());
        int fromY = Math.min(cornerA.getN(), cornerB.getN());

        var exporter = VnAreaExporter.create(
                        AdministrationUnitAreaItem.class,
                        administrationMapFilename,
                        fromX,
                        width,
                        fromY,
                        height);

        var item = new AdministrationUnitAreaItem();

        Short noCommune = 0;

        for (int x = fromX; x < fromX + width; x++) {
            for (int y = fromY; y < fromY + height; y++) {
                Commune commune = communeManager.communeAt(KilometerGridCell.fromPl1992ENKilometers(x, y));
                item.setCommune(communeToShortMap.getOrDefault(commune, noCommune));
                exporter.append(x, y, item);
            }
        }

        exporter.close();
    }
}
