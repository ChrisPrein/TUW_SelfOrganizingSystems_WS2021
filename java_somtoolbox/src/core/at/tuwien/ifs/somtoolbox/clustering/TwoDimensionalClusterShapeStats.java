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
package at.tuwien.ifs.somtoolbox.clustering;

/**
 * This class collects stats about the shape of 2-dimensional clusters
 * 
 * @author Rudolf Mayer
 * @version $Id: TwoDimensionalClusterShapeStats.java 4202 2011-03-28 16:21:18Z mayer $
 */
public class TwoDimensionalClusterShapeStats {
    int maxX = -Integer.MAX_VALUE;

    int maxY = -Integer.MAX_VALUE;

    int minX = Integer.MAX_VALUE;

    int minY = Integer.MAX_VALUE;

    int unitCount = 0;

    /** Add one point to the cluster stats */
    public void addPoint(int x, int y) {
        if (x > maxX) {
            maxX = x;
        }
        if (x < minX) {
            minX = x;
        }
        if (y > maxY) {
            maxY = y;
        }
        if (y < minY) {
            minY = y;
        }
        unitCount++;
    }

    /** Get the ratio of {@link #getStretchX()} and {@link #getStretchY()} */
    public double getBoundingBoxRatio() {
        double stretchX = getStretchX();
        double stretchY = getStretchY();
        if (stretchX < stretchY) {
            return stretchX / stretchY;
        } else {
            return stretchY / stretchX;
        }
    }

    /** @return The stretch along the vertical axis. */
    private int getStretchY() {
        return maxY - minY + 1;
    }

    /** @return The stretch along the horizontal axis */
    private int getStretchX() {
        return maxX - minX + 1;
    }

    public double getFilledRatio() {
        return unitCount / (double) (getStretchX() * getStretchY());
    }

    /** @return Returns the minX point, i.e. the X-coordinate of the upper-left corner */
    public int getMinX() {
        return minX;
    }

    /** @return Returns the minY point, i.e. the Y-coordinate of the upper-left corner */
    public int getMinY() {
        return minY;
    }

    /** @return Returns the maxX point, i.e. X-coordinate of the lower-right corner */
    public int getMaxX() {
        return maxX;
    }

    /** @return Returns the maxY point, i.e. Y-coordinate of the lower-right corner */
    public int getMaxY() {
        return maxY;
    }

}
