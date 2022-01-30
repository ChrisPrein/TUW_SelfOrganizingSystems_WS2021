/*
 * Copyright 2004-2010 Information & Software Engineering Group (188/1)
 *                     Institute of Software Technology and Interactive Systems
 *                     Vienna University of Technology, Austria
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.ifs.tuwien.ac.at/dm/somtoolbox/license.html
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package at.tuwien.ifs.somtoolbox.visualization.clustering;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;
import java.util.logging.Logger;

import at.tuwien.ifs.somtoolbox.apps.viewer.GeneralUnitPNode;
import at.tuwien.ifs.somtoolbox.layers.metrics.L2Metric;
import at.tuwien.ifs.somtoolbox.layers.metrics.MetricException;

/**
 * Complete Linkage Clustering Algorithm. This class is not compatible with mnemonic SOMs (and probably also not
 * compatible with hierarchical SOMs) The updating uf the distances can probabla be optimised (see
 * {@link WardsLinkageTreeBuilderAll} - lazyUpdate)
 * 
 * @author Angela Roiger
 * @version $Id: CompleteLinkageTreeBuilder.java 4207 2011-03-29 16:07:02Z mayer $
 */
public class CompleteLinkageTreeBuilder extends TreeBuilder {

    /**
     * Calculation of the Clustering. This code is only compatible with rectangular, non hierarchical SOMs!
     * 
     * @param units the GeneralUnitPNode Array containing all the units of the SOM
     * @return the ClusteringTree (i.e. the top node of the tree)
     */
    @Override
    public ClusteringTree createTree(GeneralUnitPNode[][] units) throws ClusteringAbortedException {
        Logger.getLogger("at.tuwien.ifs.somtoolbox").info("Start Clustering ");
        this.level = units.length * units[0].length;

        // initialize monitor
        resetMonitor(2 * level);

        TreeSet<NodeDistance> dists = calculateAllDistances(units);

        NodeDistance tmpDist;
        ClusterNode newNode = null;

        while (dists.size() > 0) {
            tmpDist = dists.first();
            // Logger.getLogger("at.tuwien.ifs.somtoolbox").info("Shortest: "+tmpDist.dist);

            dists.remove(tmpDist);
            level--;
            incrementMonitor();
            allowAborting();
            Logger.getLogger("at.tuwien.ifs.somtoolbox").info("Level: " + level);
            newNode = new ClusterNode(tmpDist.n1, tmpDist.n2, level, tmpDist.dist);
            HashMap<List<ClusterNode>, NodeDistance> duplicateEliminator = new HashMap<List<ClusterNode>, NodeDistance>();
            List<ClusterNode> pair;

            // remove not needed connections and change distances from n1,n2 to newNode
            for (Iterator<NodeDistance> i = dists.iterator(); i.hasNext();) {
                NodeDistance x = i.next();
                if (x.n1 == tmpDist.n1 || x.n1 == tmpDist.n2) {
                    x.n1 = newNode;
                }
                if (x.n2 == tmpDist.n1 || x.n2 == tmpDist.n2) {
                    x.n2 = newNode;
                }
                if (x.n1 == x.n2) {
                    i.remove();
                } else if (x.n1 == newNode || x.n2 == newNode) {
                    // & keep only the longest distance for each connection
                    if (x.n1 == newNode) { // make pair where new node is first
                        pair = Arrays.asList(new ClusterNode[] { x.n1, x.n2 });
                    } else {
                        pair = Arrays.asList(new ClusterNode[] { x.n2, x.n1 });
                    }
                    // replaces any existing entry with the same pair
                    duplicateEliminator.put(pair, x);
                    i.remove();
                    Logger.getLogger("at.tuwien.ifs.somtoolbox").finest("Removing @ " + level);
                }
            }

            // now dists does not contain distances between the two merged clusters and any other clusters
            // these are now in duplicateLiminator
            dists.addAll(duplicateEliminator.values());

        }
        finishMonitor();
        Logger.getLogger("at.tuwien.ifs.somtoolbox").info("Finished Clustering - Complete Linkage");

        return new ClusteringTree(newNode, units.length);
    }

    /**
     * Calculates all distances between all units.
     * 
     * @param units A GeneralUnitPNode[][] containing the Units of the som
     * @return TreeSet of NodeDistances containing the distances between the units starting with the smallest.
     * @throws ClusteringAbortedException when the clustering was aborted.
     */
    protected TreeSet<NodeDistance> calculateAllDistances(GeneralUnitPNode[][] units) throws ClusteringAbortedException {
        int xdim = units.length;
        int ydim = units[0].length;

        // Angela: Pfusch :)
        L2Metric l1 = new L2Metric();

        // int count=0;
        ClusterNode[] tmp = new ClusterNode[xdim * ydim];
        TreeSet<NodeDistance> dists = new TreeSet<NodeDistance>();

        // create all basic Nodes
        for (int x = 0; x < xdim; x++) {
            for (int y = 0; y < ydim; y++) {
                tmp[x + y * ydim] = new ClusterNode(units[x][y], level);
            }
        }

        // create all distances, avoiding duplicates (i.e. store u1 -> u2, but not u2 -> u1 as well)
        for (int i = 0; i < tmp.length; i++) {
            double[] weightVector = tmp[i].getNodes()[0].getUnit().getWeightVector();
            for (int j = i + 1; j < tmp.length; j++) {
                incrementMonitor();
                allowAborting();
                try {
                    dists.add(new NodeDistance(tmp[i], tmp[j], l1.distance(weightVector,
                            tmp[j].getNodes()[0].getUnit().getWeightVector())));
                } catch (MetricException e) {
                    Logger.getLogger("at.tuwien.ifs.somtoolbox").severe("Cannot create clustering: " + e.getMessage());
                }
            }
        }
        return dists;
    }

    @Override
    public String getClusteringAlgName() {
        return "CompleteLinkage";
    }
}
