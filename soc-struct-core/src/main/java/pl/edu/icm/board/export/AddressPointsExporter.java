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

package pl.edu.icm.board.export;

import net.snowyhollows.bento.annotation.WithFactory;
import pl.edu.icm.board.geography.prg.AddressPointManager;
import pl.edu.icm.trurl.util.Status;
import pl.edu.icm.trurl.visnow.VnPointsExporter;

import java.io.IOException;

public class AddressPointsExporter {
    private final AddressPointManager addressPointManager;

    @WithFactory
    public AddressPointsExporter(AddressPointManager addressPointManager) {
        this.addressPointManager = addressPointManager;
    }

    public void export() throws IOException {
        var exporter = VnPointsExporter.create(
                ExportedAddressPoint.class,
                "output/vn/address_points");

        ExportedAddressPoint exported = new ExportedAddressPoint();
        var statusBar = Status.of("Outputing addressPoints", 1000000);
        addressPointManager.streamAddressPoints()
                .forEach(ap -> {
                    try {
                        exported.setX(ap.getEasting() / 1000);
                        exported.setY(ap.getNorthing() / 1000);
                        exported.setPostalCode(Integer.parseInt(ap.getPostalCode().replaceAll("[-=]", "").strip(), 10));
                        exporter.append(exported);
                        statusBar.tick();
                    } catch (NumberFormatException nfe) {
                        System.out.println("error in postal code: [" + ap.getPostalCode() + "]");
                    }
                });
        exporter.close();
        statusBar.done();
    }

}
