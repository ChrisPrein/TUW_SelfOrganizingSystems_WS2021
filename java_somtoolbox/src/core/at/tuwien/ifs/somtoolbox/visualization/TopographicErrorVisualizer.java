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
package at.tuwien.ifs.somtoolbox.visualization;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.StringTokenizer;
import java.util.logging.Logger;

import at.tuwien.ifs.somtoolbox.SOMToolboxException;
import at.tuwien.ifs.somtoolbox.data.SOMVisualisationData;
import at.tuwien.ifs.somtoolbox.layers.LayerAccessException;
import at.tuwien.ifs.somtoolbox.layers.Unit;
import at.tuwien.ifs.somtoolbox.layers.quality.QualityMeasureNotFoundException;
import at.tuwien.ifs.somtoolbox.layers.quality.TopographicError;
import at.tuwien.ifs.somtoolbox.models.GrowingSOM;

/**
 * Visualization of some aspects of the Topographic Error Quality Measure, computation in {@link TopographicError}<br>
 * Notes: Only the measures relating to the Units will be drawn (Unit_QE, Unit_MQE)
 * 
 * @author Gerd Platzgummer
 * @author Rudolf Mayer
 * @version $Id: TopographicErrorVisualizer.java 4340 2015-03-06 15:25:36Z mayer $
 */
public class TopographicErrorVisualizer extends AbstractBackgroundImageVisualizer implements QualityMeasureVisualizer {

    private TopographicError topographicError = null;

    public TopographicErrorVisualizer() {
        NUM_VISUALIZATIONS = 2;
        VISUALIZATION_NAMES = new String[] { "Topographic Error Copy neighbourhood - 4 units",
                "Topographic Error Copy neighborhood - 8 units" };
        VISUALIZATION_SHORT_NAMES = new String[] { "TopographicError4Units Copy", "TopographicError8units Copy" };
        VISUALIZATION_DESCRIPTIONS = new String[] {
                "Topographic Error, 4 nearest neighbors on the map defined to be adjacent",
                "Topographic Error, 8 nearest neighbors on the map defined to be adjacent" };
        neededInputObjects = new String[] { SOMVisualisationData.INPUT_VECTOR };
    }

    @Override
    public BufferedImage createVisualization(int index, GrowingSOM gsom, int width, int height)
            throws SOMToolboxException {
        String cachefile = null; // FIXME: actually read from cache file..
        if (topographicError == null) {
            if (gsom.getSharedInputObjects().getData(neededInputObjects[0]) == null) {
                throw new SOMToolboxException("You need to specify " + neededInputObjects[0]);
            }
            topographicError = new TopographicError(gsom.getLayer(), gsom.getSharedInputObjects().getInputData());
        }

        switch (index) {
            case 0: {
                return createQEImage(gsom, width, height, cachefile);
            }
            case 1: {
                return createMQEImage(gsom, width, height, cachefile);
            }
            default: {
                return null;
            }
        }
    }

    private BufferedImage createQEImage(GrowingSOM gsom, int width, int height, String cachefile)
            throws LayerAccessException {
        return createImage(gsom, width, height, cachefile, TopographicError.TE_UNIT);
    }

    private BufferedImage createMQEImage(GrowingSOM gsom, int width, int height, String cachefile)
            throws LayerAccessException {
        return createImage(gsom, width, height, cachefile, TopographicError.TE8_UNIT);
    }

    public BufferedImage createImage(GrowingSOM gsom, int width, int height, String cachefile, final String qualityName)
            throws LayerAccessException {
        double maxQualityValue = Double.MIN_VALUE;
        double minQualityValue = Double.MAX_VALUE;
        double[][] unitquals = null;
        if (cachefile == null) {
            try {
                unitquals = topographicError.getUnitQualities(qualityName);
            } catch (QualityMeasureNotFoundException e) {
            }
        } else {
            unitquals = new double[gsom.getLayer().getXSize()][gsom.getLayer().getYSize()];
            // read from cache file
            try {
                BufferedReader br = new BufferedReader(new FileReader(cachefile));
                String line = null;
                int y = 0;
                while ((line = br.readLine()) != null) {
                    if (line.trim() != "") {
                        StringTokenizer st = new StringTokenizer(line);
                        int x = 0;
                        while (st.hasMoreTokens()) {
                            unitquals[x][y] = Double.parseDouble(st.nextToken());
                            x++;
                        }
                        y++;
                    }
                }
                br.close();
            } catch (Exception ex) {
            }

        }

        // getting the maxTE- and the minTE- Value -kind of Normalization relating to the colour palette
        for (int j = 0; j < gsom.getLayer().getYSize(); j++) {
            for (int i = 0; i < gsom.getLayer().getXSize(); i++) {
                try {
                    Unit u = gsom.getLayer().getUnit(i, j);
                    if (u.getNumberOfMappedInputs() > 0) {
                        double unitQualityValue = unitquals[u.getXPos()][u.getYPos()];
                        if (unitQualityValue > maxQualityValue) {
                            maxQualityValue = unitQualityValue;
                        }
                        if (unitQualityValue < minQualityValue) {
                            minQualityValue = unitQualityValue;
                        }
                    }
                } catch (LayerAccessException e) {
                    Logger.getLogger("at.tuwien.ifs.somtoolbox").severe(e.getMessage());
                    throw e;
                }
            }
        }
        double qualityValueRange = maxQualityValue - minQualityValue;

        BufferedImage res = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = (Graphics2D) res.getGraphics();

        int unitWidth = width / gsom.getLayer().getXSize();
        int unitHeight = height / gsom.getLayer().getYSize();

        for (int y = 0; y < gsom.getLayer().getYSize(); y++) {
            for (int x = 0; x < gsom.getLayer().getXSize(); x++) {
                try {
                    Unit u = gsom.getLayer().getUnit(x, y);
                    if (u.getNumberOfMappedInputs() > 0) {
                        // old approach for computing ci values
                        // ci =
                        // (int)Math.round(((unitquals[u.getXPos()][u.getYPos()]-minTE)/(maxTE-minTE))*(double)(paletteSize-1));
                        // g.setPaint(palette[ci]); //mapping of the Value to the colour of the visualization

                        // new approach of getting the colours
                        double unitQualityValue = unitquals[u.getXPos()][u.getYPos()];
                        if (unitQualityValue == 0.0) {
                            g.setPaint(Color.WHITE);
                        } else {
                            double ci = 1.0 - ((unitQualityValue - minQualityValue) / qualityValueRange * 0.6 + 0.2);
                            g.setPaint(new Color(Color.HSBtoRGB((float) 0.0, (float) 0.5, (float) ci))); // H-value==color==red
                        }

                    } else {
                        g.setPaint(Color.WHITE);
                        // g.setPaint(new Color(Color.HSBtoRGB((float)0.0, (float)0.5, (float)1.0)));
                        // //H-value==color==red
                    }
                    g.setColor(null);
                    g.fill(new Rectangle(x * unitWidth, y * unitHeight, unitWidth, unitHeight));
                } catch (LayerAccessException e) {
                    Logger.getLogger("at.tuwien.ifs.somtoolbox").severe(e.getMessage());
                    throw e;
                }
            }
        }
        return res;
    }
}
