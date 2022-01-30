/*
 * Copyright 2004-2010 Institute of Software Technology and Interactive Systems, Vienna University of Technology
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

import java.awt.Paint;

/**
 * @author Tatiana Rybnikova
 * @author Tomas Sedivy
 * @version $Id: DendrogramChild.java 4349 2015-03-10 15:52:38Z mayer $
 */
public class DendrogramChild {
    private int id;

    private static int leafCount = 0;

    private int leafId = -1;

    private ClusterNode n;

    public DendrogramChild(ClusterNode n) {
        this.n = n;
        if (isLeafNode()) {// is leaf node
            leafId = leafCount;
            leafCount++;
        }

    }

    private boolean isLeafNode() {

        return n.getChild1() == null & n.getChild2() == null;
    }

    @Override
    public String toString() {
        String s = n.toString().replace("Unit", "").replace("[", "").replace("]", "").replace(" ", "");
        if (s.length() > 18) {
            return s.substring(0, Math.min(7, s.length())) + "...";
        } else {
            return s;
        }

    }

    public String getTooltip() {
        return n.toString().replace("Unit", "").replace("[", "").replace("]", "");
    }

    public ClusterNode getNode() {
        return n;
    }

    public Double getMergeCostSum() {
        return n.getMergeCostSum();
    }

    public Paint getPaint() {

        return n.getColoredCluster().getPaint();
    }
}
