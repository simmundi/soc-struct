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

package pl.edu.icm.board.urizen.place;

import com.google.common.collect.ListMultimap;
import com.google.common.collect.MultimapBuilder;
import com.google.common.collect.Multimaps;
import net.snowyhollows.bento.annotation.WithFactory;
import org.apache.commons.math3.random.RandomGenerator;
import pl.edu.icm.board.Board;
import pl.edu.icm.board.geography.KilometerGridCell;
import pl.edu.icm.board.geography.commune.CommuneManager;
import pl.edu.icm.board.geography.prg.AddressPointManager;
import pl.edu.icm.board.geography.prg.model.AddressPoint;
import pl.edu.icm.board.model.AdministrationUnit;
import pl.edu.icm.board.model.Location;
import pl.edu.icm.board.model.Workplace;
import pl.edu.icm.board.util.RandomProvider;
import pl.edu.icm.em.common.DebugTextFile;
import pl.edu.icm.em.common.DebugTextFileService;
import pl.edu.icm.trurl.ecs.util.Selectors;
import pl.edu.icm.trurl.util.Status;

import java.io.IOException;

import static pl.edu.icm.trurl.ecs.util.EntityIterator.select;

public class WorkplacesFromCommunesToAddressPointsUrizen {
    private final AddressPointManager addressPointsManager;
    private final CommuneManager communeManager;
    private final Board board;
    private final RandomGenerator random;
    private ListMultimap<String, AddressPoint> map;
    private final Selectors selectors;
    private final DebugTextFileService debugTextFileService;
    private int failures;

    @WithFactory
    public WorkplacesFromCommunesToAddressPointsUrizen(AddressPointManager addressPointsManager,
                                                       CommuneManager communeManager, Board board,
                                                       RandomProvider randomProvider, Selectors selectors, DebugTextFileService debugTextFileService) {
        this.addressPointsManager = addressPointsManager;
        this.communeManager = communeManager;
        this.board = board;
        this.random = randomProvider.getRandomGenerator(WorkplacesFromCommunesToAddressPointsUrizen.class);
        this.selectors = selectors;
        this.debugTextFileService = debugTextFileService;
    }

    public int assignWorkplaces() {
        var status = Status.of("collecting address points", 1_000_000);
        map = addressPointsManager.streamAddressPoints()
                .peek(ap -> status.tick())
                .collect(Multimaps.toMultimap(
                        ap -> communeManager
                                .communeAt(KilometerGridCell.fromPl1992ENMeters(ap.getEasting(), ap.getNorthing()))
                                .getTeryt(),
                        ap -> ap,
                        MultimapBuilder.hashKeys(240000).arrayListValues()::build));
        status.done();
        try (DebugTextFile debugFile = debugTextFileService.createTextFile("output/debug_address_points.csv")) {
            debugFile.println("teryt,apCount");
            var keyset = map.keys();
            for (String s : map.keySet()) {
                debugFile.printf("%s,%d\n", s, keyset.count(s));
            }
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
        var status2 = Status.of("Adjusting workplaces", 500000);
        board.getEngine().execute(select(selectors.allWithComponents(Workplace.class, AdministrationUnit.class)).forEach(Workplace.class, AdministrationUnit.class, (entity, h, u) -> {
            var options = map.get(u.getTeryt());
            if (options.size() == 0) {
                status2.problem("No address point in " + u.getTeryt());
                throw new IllegalStateException("No address point in " + u.getTeryt());
            }
            var target = options.get((int) (random.nextDouble() * options.size()));
            var location = entity.add(new Location());
            location.setE((int) target.getEasting());
            location.setN((int) target.getNorthing());
            status2.tick();
        }));
        status2.done();
        return failures;
    }

}
