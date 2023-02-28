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

package pl.edu.icm.board.geography.graph;

import com.graphhopper.GraphHopper;
import com.graphhopper.config.Profile;
import com.graphhopper.storage.GraphBuilder;
import com.graphhopper.storage.GraphHopperStorage;
import com.graphhopper.storage.NodeAccess;
import net.snowyhollows.bento.annotation.WithFactory;
import pl.edu.icm.trurl.ecs.mapper.Mapper;
import pl.edu.icm.trurl.ecs.mapper.Mappers;
import pl.edu.icm.trurl.store.Store;

import java.util.List;
import java.util.Optional;

public class GraphSource {

    private final String osmFilename;

    @WithFactory
    GraphSource(String osmFilename) {
        this.osmFilename = osmFilename;
    }

    public void load(Store store) {
        GraphHopper hopper = new GraphHopper();
        hopper.setOSMFile(osmFilename);
        hopper.setGraphHopperLocation("output/graph");
        hopper.setProfiles(List.of(new Profile("car").setVehicle("car").setWeighting("shortest")));
        hopper.importOrLoad();
        var em = hopper.getEncodingManager();
        hopper.close();
        GraphHopperStorage graph = new GraphBuilder(em).setRAM("output/graph", true).build();
        graph.loadExisting();
        var baseGraph = graph.getBaseGraph();

        Mapper<GraphStoreItem> graphItemMapper = new Mappers().create(GraphStoreItem.class);
        graphItemMapper.configureStore(store);
        graphItemMapper.attachStore(store);

        int idx = 0;
        var edges = baseGraph.getAllEdges();
        GraphStoreItem item = graphItemMapper.create();
        while (edges.next()) {
            item.setBaseNode(edges.getBaseNode());
            item.setAdjNode(edges.getAdjNode());
            item.setDistance(edges.getDistance());
            item.setId(edges.getEdge());
            item.setNode(false);
            graphItemMapper.save(item, idx++);
        }

        item = graphItemMapper.create();
        NodeAccess nodes = baseGraph.getNodeAccess();
        for (int i = 0; i < baseGraph.getNodes(); i++) {
            item.setLon(nodes.getLon(i));
            item.setLat(nodes.getLat(i));
            item.setId(i);
            item.setNode(true);
            graphItemMapper.save(item, idx++);
        }
        store.fireUnderlyingDataChanged(0, idx);
        graph.close();
        hopper.clean();
    }
}
