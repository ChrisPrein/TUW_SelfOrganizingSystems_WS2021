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

/**
 * @author Tatiana Rybnikova
 * @author Tomas Sedivy
 * @version $Id: DendrogramEdge.java 4349 2015-03-10 15:52:38Z mayer $
 */
public class DendrogramEdge {

    private double mergecost;

    private int id;

    public DendrogramEdge(int id, double mergecost) {
        this.id = id;
        this.mergecost = mergecost;
    }

    @Override
    public String toString() {
        return "Ed" + id;
    }

    public Double getMergeCost() {
        return mergecost;
    }
}
