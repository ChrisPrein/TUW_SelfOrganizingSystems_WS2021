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
package at.tuwien.ifs.somtoolbox.clustering.functions;

import at.tuwien.ifs.somtoolbox.apps.viewer.GeneralUnitPNode;
import at.tuwien.ifs.somtoolbox.clustering.Cluster;
import at.tuwien.ifs.somtoolbox.layers.Unit;
import at.tuwien.ifs.somtoolbox.layers.metrics.DistanceMetric;
import at.tuwien.ifs.somtoolbox.layers.metrics.L2Metric;
import at.tuwien.ifs.somtoolbox.layers.metrics.MetricException;

/**
 * @author Rudolf Mayer
 * @version $Id: UnitClusteringFunction.java 3927 2010-11-09 12:04:54Z mayer $
 */
public class GeneralUnitPNodeClusteringFunction implements ClusterElementFunctions<GeneralUnitPNode> {
    protected DistanceMetric metric;

    public GeneralUnitPNodeClusteringFunction() {
        this(new L2Metric());
    }

    public GeneralUnitPNodeClusteringFunction(DistanceMetric metric) {
        this.metric = metric;
    }

    @Override
    public double distance(GeneralUnitPNode u1, GeneralUnitPNode u2) {
        return distance(u1.getUnit().getWeightVector(), u2.getUnit().getWeightVector());
    }

    public double distance(double[] vector1, double[] vector2) {
        try {
            return metric.distance(vector1, vector2);
        } catch (MetricException e) {
            return 0; // doesn't happen
        }
    }

    @Override
    public GeneralUnitPNode meanObject(Cluster<? extends GeneralUnitPNode> elements) {
        if (elements.size() == 1) {
            return elements.get(0);
        }
        GeneralUnitPNode firstNode = elements.get(0);
        double[] meanVector = new double[firstNode.getUnit().getWeightVector().length];
        double meanX = 0;
        double meanY = 0;
        for (int i = 0; i < meanVector.length; i++) {
            double sum = 0;
            for (int j = 0; j < elements.size(); j++) {
                sum += elements.get(j).getUnit().getWeightVector()[i];
            }
            meanVector[i] = sum / elements.size();
        }
        Unit unit = new Unit(firstNode.getUnit().getLayer(), (int) Math.round(meanX), (int) Math.round(meanY), meanVector);
        return new GeneralUnitPNode(unit, firstNode);
    }

    @Override
    public String toString(Cluster<? extends GeneralUnitPNode> elements) {
        GeneralUnitPNode mean = meanObject(elements);
        return getClass().getSimpleName() + " # vectors: " + elements.size() + ", mean unit: " + mean;
    }

}
