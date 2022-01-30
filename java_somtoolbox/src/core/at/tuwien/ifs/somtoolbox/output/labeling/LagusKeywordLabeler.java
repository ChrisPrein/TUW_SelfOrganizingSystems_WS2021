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
package at.tuwien.ifs.somtoolbox.output.labeling;

import java.io.File;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.logging.Logger;

import cern.colt.matrix.DoubleMatrix1D;

import com.martiansoftware.jsap.JSAPResult;
import com.martiansoftware.jsap.Parameter;

import at.tuwien.ifs.somtoolbox.apps.SOMToolboxApp;
import at.tuwien.ifs.somtoolbox.apps.config.OptionFactory;
import at.tuwien.ifs.somtoolbox.data.InputData;
import at.tuwien.ifs.somtoolbox.data.InputDataFactory;
import at.tuwien.ifs.somtoolbox.data.InputDatum;
import at.tuwien.ifs.somtoolbox.data.TemplateVector;
import at.tuwien.ifs.somtoolbox.input.SOMLibFormatInputReader;
import at.tuwien.ifs.somtoolbox.layers.Label;
import at.tuwien.ifs.somtoolbox.layers.Unit;
import at.tuwien.ifs.somtoolbox.models.GHSOM;
import at.tuwien.ifs.somtoolbox.models.GrowingSOM;

/**
 * Implements the <code>Keyword selection</code> labelling method, as described in <i><b>Lagus, K. and Kaski,
 * S.</b>:Keyword selection method for characterizing text document maps. Proceedings of ICANN99, 9th International
 * Conference on Artificial Neural Networks, volume 1, pages 371-376, IEEE, London. </i><br/>
 * 
 * @author Rudolf Mayer
 * @author Hauke Schuldt
 * @author Alois Wollersberger
 * @version $Id: LagusKeywordLabeler.java 4195 2011-03-10 14:55:02Z frank $
 */
public class LagusKeywordLabeler extends AbstractLabeler implements Labeler, SOMToolboxApp {
    public static Type APPLICATION_TYPE = LabelSOM.APPLICATION_TYPE;

    public static String DESCRIPTION = "Implements the LagusKeyword labelling method";

    public static String LONG_DESCRIPTION = DESCRIPTION;

    public static final Parameter[] OPTIONS = { OptionFactory.getOptInputVectorFile(true),
            OptionFactory.getOptTemplateVectorFile(true), OptionFactory.getOptWeightVectorFile(true),
            OptionFactory.getOptUnitDescriptionFile(true), OptionFactory.getOptNumberLabels(false, "5"),
            OptionFactory.getSwitchIsDenseData(), OptionFactory.getOptMapDescriptionFile(false),
            OptionFactory.getOptInputDirectory(true) };

    String path;

    private int innerRadius;

    private int outerRadius;

    public static void main(String[] args) {
        JSAPResult config = OptionFactory.parseResults(args, OPTIONS);

        int numLabels = config.getInt("numberLabels", 5);
        int innerRadiusLabels = 1;
        int outerRadiusLabels = 2;
        String inputVectorFilename = config.getString("inputVectorFile");
        boolean denseData = config.getBoolean("denseData", false);
        String templateVectorFilename = config.getString("templateVectorFile", null);
        String unitDescriptionFilename = config.getString("unitDescriptionFile", null);
        String weightVectorFilename = config.getString("weightVectorFile");
        String mapDescriptionFilename = config.getString("mapDescriptionFile", null);

        GrowingSOM gsom = null;
        try {
            gsom = new GrowingSOM(new SOMLibFormatInputReader(weightVectorFilename, unitDescriptionFilename,
                    mapDescriptionFilename));
        } catch (Exception e) {
            Logger.getLogger("at.tuwien.ifs.somtoolbox").severe(e.getMessage() + " Aborting.");
            e.printStackTrace();
            System.exit(-1);
        }

        InputData data = InputDataFactory.open(inputVectorFilename, templateVectorFilename, !denseData, true, 1, 7);
        Labeler labeler = new LagusKeywordLabeler(config.getString("inputDir"), innerRadiusLabels, outerRadiusLabels);
        labeler.label(gsom, data, numLabels);
    }

    /**
     * Constructor in order to initialize without an additional parameter
     */
    public LagusKeywordLabeler() {
        super();
    }

    public LagusKeywordLabeler(String path) {
        super();
        if (!path.endsWith(File.separator)) {
            this.path = path + File.separator;
        } else {
            this.path = path;
        }
    }

    /**
     * Constructor in order to initialize with path and radiuses
     */
    public LagusKeywordLabeler(String path, int innerRadius, int outerRadius) {
        super();
        if (!path.endsWith(File.separator)) {
            this.path = path + File.separator;
        } else {
            this.path = path;
        }
        this.innerRadius = innerRadius;
        this.outerRadius = outerRadius;
    }

    @Override
    public void label(GHSOM ghsom, InputData data, int num) {
        label(ghsom.topLayerMap(), data, num);
    }

    @Override
    public void label(GrowingSOM gsom, InputData data, int num) {
        label(gsom, data, num, false);
    }

    @Override
    public void label(GrowingSOM gsom, InputData data, int num, boolean ignoreLabelsWithZero) {
        // Test parameters and start time measurement
        long start = System.currentTimeMillis();
        num = checkMaxDimensionality(data, num);

        // Get the units and the template vector
        Unit[] units = gsom.getLayer().getAllUnits();
        TemplateVector tv = data.templateVector();

        // Generate the data basis for the algorithm
        UnitWordsMap uwm = generateUnitWordsMap(units, gsom, data, tv);

        // Selecting labels
        selectAreaLabels(units, uwm, num);

        // Time measurement
        long finish = System.currentTimeMillis();
        long runtime = finish - start;
        Logger.getLogger("at.tuwien.ifs.somtoolbox").info("LagusKewordLabeler finished in " + runtime + " ms.");
        System.out.println("LagusKewordLabeler finished in " + runtime + " ms.");
    }

    private UnitWordsMap generateUnitWordsMap(Unit[] units, GrowingSOM gsom, InputData data, TemplateVector tv) {
        UnitWordsMap uwm = new UnitWordsMap(gsom.getLayer().getXSize(), gsom.getLayer().getYSize(),
                gsom.getLayer().getZSize());
        if (innerRadius != 0) {
            uwm.A0_RADIUS = innerRadius;
        }
        if (outerRadius != 0) {
            uwm.A1_RADIUS = outerRadius;
        }
        for (Unit unit : units) {
            if (unit.getNumberOfMappedInputs() != 0) {
                UnitWords uw = uwm.newUnitWords(unit.getXPos(), unit.getYPos(), unit.getZPos());
                InputDatum[] unitData = data.getInputDatum(unit.getMappedInputNames());
                for (InputDatum element : unitData) {
                    DoubleMatrix1D vec = element.getVector();
                    for (int i = 0; i < vec.size(); i++) {
                        if (vec.get(i) != 0) {
                            uw.addWord(tv.getLabel(i), vec.get(i));
                        }
                    }
                }
            }
        }
        return uwm;
    }

    /**
     * Only labels one unit per map area, dependent on A0 and A1 radius. A0 aka inner radius, A1 aka outer radius.
     * 
     * @param units the units on the map
     * @param uwm the UnitWordsMap with the information about the frequencies of the word occurrences in the maps
     * @param num the number of Labels
     */
    private void selectAreaLabels(Unit[] units, UnitWordsMap uwm, int num) {
        // List of units sorted by G2 value
        LinkedList<Unit> sortedUnits = new LinkedList<Unit>();
        LinkedList<String> g2Label = new LinkedList<String>();
        LinkedList<Double> g2Value = new LinkedList<Double>();
        for (Unit unit : units) {
            Hashtable<String, Double> g1 = uwm.getBestWords(unit.getXPos(), unit.getYPos(), unit.getZPos(), 20);
            Hashtable<String, Double> g2 = uwm.chooseBestWord(unit.getXPos(), unit.getYPos(), unit.getZPos(), g1);
            if (g2 == null) {
                Label[] empty = new Label[1];
                empty[0] = new Label("no label");
                unit.setLabels(empty);
                continue;
            }

            String label = g2.keys().nextElement();
            Double value = g2.get(label);
            for (int i = 0; i < units.length; i++) {
                if (i == sortedUnits.size() || g2Value.get(i) < value) {
                    sortedUnits.add(i, unit);
                    g2Label.add(i, label);
                    g2Value.add(i, value);
                    break;
                }
            }

        }
        double distance = 2.0 * (uwm.A0_RADIUS + ((double) uwm.A1_RADIUS - (double) uwm.A0_RADIUS) / 2);
        while (!sortedUnits.isEmpty()) {
            Unit unit = sortedUnits.pop();
            Label[] labels = new Label[1];
            labels[0] = new Label(g2Label.pop());
            g2Value.pop();
            unit.setLabels(labels);
            int i = 0;
            while (i < sortedUnits.size()) {
                if (calcUnitDistance(unit, sortedUnits.get(i)) < distance) {
                    Unit temp = sortedUnits.remove(i);
                    g2Label.remove(i);
                    g2Value.remove(i);
                    temp.clearLabels();
                } else {
                    i++;
                }
            }
        }
    }

    /**
     * Calculates the distance between Units
     * 
     * @param start start unit
     * @param end end unit
     * @return distance between units
     */
    private double calcUnitDistance(Unit start, Unit end) {
        return Math.sqrt(Math.pow(start.getXPos() - end.getXPos(), 2) + Math.pow(start.getYPos() - end.getYPos(), 2)
                + Math.pow(start.getYPos() - end.getYPos(), 2));
    }

    /**
     * Selects a number of n labels for each unit based on the proposed goodness G1 by Lagus
     * 
     * @param units the units on the map
     * @param uwm the UnitWordsMap with the information about the frequencies of the word occurrences in the maps
     * @param num the number of Labels
     */
    private void selectLabelsG1(Unit[] units, UnitWordsMap uwm, int num) {

        for (Unit unit : units) {
            if (unit.getNumberOfMappedInputs() != 0) {
                Hashtable<String, Double> bestWords = uwm.getBestWords(unit.getXPos(), unit.getYPos(), unit.getZPos(),
                        num);
                Label[] labels = new Label[Math.min(num, bestWords.size())];
                Enumeration<String> words = bestWords.keys();

                int i = 0;
                while (words.hasMoreElements() && i < labels.length) {
                    String word = words.nextElement();
                    labels[i] = new Label(word);
                    i++;
                }

                unit.setLabels(labels);
            } else {
                // Also empty Units need an non-empty label
                // Otherwise the SOMToolboxViewer will abort
                Label[] labels = new Label[1];
                labels[0] = new Label("no label");
                unit.setLabels(labels);
            }
        }
    }

    /**
     * Selects only one best label for units based on the proposed goodness G2 by Lagus
     * 
     * @param units the units on the map
     * @param uwm the UnitWordsMap with the information about the frequencies of the word occurrences in the maps
     */
    private void selectLabelsG2(Unit[] units, UnitWordsMap uwm) {
        for (Unit unit : units) {
            Label[] labels = new Label[1];
            if (unit.getNumberOfMappedInputs() != 0) {
                Hashtable<String, Double> bestWords = uwm.chooseBestWord(unit.getXPos(), unit.getYPos(),
                        unit.getZPos(), uwm.getBestWords(unit.getXPos(), unit.getYPos(), unit.getZPos(), 20));
                Enumeration<String> words = bestWords.keys();
                int i = 0;
                while (words.hasMoreElements() && i < labels.length) {
                    String word = words.nextElement();
                    labels[i] = new Label(word);
                    i++;
                }
            } else {
                // Also empty Units need an non-empty label
                // Otherwise the SOMToolboxViewer will abort
                labels[0] = new Label("no label");
            }
            unit.setLabels(labels);
        }
    }

}
