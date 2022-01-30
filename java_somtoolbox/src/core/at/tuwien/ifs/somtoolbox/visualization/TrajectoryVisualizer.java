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
package at.tuwien.ifs.somtoolbox.visualization;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Stroke;
import java.awt.Transparency;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;

import at.tuwien.ifs.somtoolbox.SOMToolboxException;
import at.tuwien.ifs.somtoolbox.apps.viewer.CommonSOMViewerStateData;
import at.tuwien.ifs.somtoolbox.data.InputData;
import at.tuwien.ifs.somtoolbox.data.InputDataFactory;
import at.tuwien.ifs.somtoolbox.layers.Unit;
import at.tuwien.ifs.somtoolbox.models.GrowingSOM;
import at.tuwien.ifs.somtoolbox.util.VisualisationUtils;

/**
 * @author Botond Fazekas - 0925351
 * @version $Id: TrajectoryVisualizer.java 4313 2014-01-10 15:17:19Z mayer $
 * @see at.tuwien.ifs.somtoolbox.visualization.AbstractBackgroundImageVisualizer#createVisualization(int,
 *      at.tuwien.ifs.somtoolbox.models.GrowingSOM, int, int)
 */
public class TrajectoryVisualizer extends AbstractBackgroundImageVisualizer implements BackgroundImageVisualizer {

    boolean doXYVisualization = false;

    boolean doInputNameVisualization = false;

    boolean doVectorVisualization = false;

    boolean differentStartMarker = true;

    boolean differentEndMarker = false;

    boolean frequencySize = true;

    String XYFile = "";

    String NameFile = "";

    String VecFile = "";

    public TrajectoryVisualizer() {
        NUM_VISUALIZATIONS = 1;
        VISUALIZATION_NAMES = new String[] { "Trajectory visualization" };
        VISUALIZATION_SHORT_NAMES = new String[] { "TRVis" };
        VISUALIZATION_DESCRIPTIONS = new String[] { "Visualizes a trajectory on the map." };
        neededInputObjects = new String[] {};

        if (!GraphicsEnvironment.isHeadless()) {
            controlPanel = new TrajectoryVisualizerControlPanel();
        }
        preferredScaleFactor = getPreferredScaleFactor();
    }

    private void drawArrow(Graphics2D g, Unit u1, Unit u2, int unitWidth, int unitHeight) {

        Point uc1 = VisualisationUtils.getUnitCentreLocation(u1, unitWidth, unitHeight);
        Point uc2 = VisualisationUtils.getUnitCentreLocation(u2, unitWidth, unitHeight);
        int lineWidth = Math.round(unitWidth / 5);
        int arrowLength = lineWidth * 2;
        int arrowWidth = Math.round(lineWidth / 3 * 2);
        Graphics2D g1 = (Graphics2D) g.create();

        double dx = uc2.x - uc1.x, dy = uc2.y - uc1.y;
        double angle = Math.atan2(dy, dx);
        int len = (int) Math.sqrt(dx * dx + dy * dy);
        AffineTransform at = AffineTransform.getTranslateInstance(uc1.x, uc1.y);
        at.concatenate(AffineTransform.getRotateInstance(angle));
        g1.transform(at);

        // Draw horizontal arrow starting in (0, 0)
        Stroke str = g.getStroke();
        g1.setStroke(new BasicStroke(lineWidth / 2));
        g1.drawLine(lineWidth / 2, 0, len - arrowLength, 0);
        g1.fillPolygon(new int[] { len, len - arrowLength, len - arrowLength, len }, new int[] { 0, -arrowWidth,
                arrowWidth, 0 }, 4);
        g1.setStroke(str);
    }

    private void drawCentreMarkers(BufferedImage image, GrowingSOM gsom, Color dotColor, List<Unit> units) {

        if (units.size() < 1) { // No drawing is needed if there are no coordinates
            return;
        }
        int[][] unitFreq = new int[gsom.getLayer().getXSize()][gsom.getLayer().getYSize()];
        int max = 0;
        Unit last = null;
        for (Unit unit : units) {
            unitFreq[unit.getXPos()][unit.getYPos()]++;
            if (unitFreq[unit.getXPos()][unit.getYPos()] > max) {
                max = unitFreq[unit.getXPos()][unit.getYPos()];
            }
            last = unit;
        }

        int unitWidth = image.getWidth() / gsom.getLayer().getXSize();
        int unitHeight = image.getHeight() / gsom.getLayer().getYSize();

        int unitSize = Math.min(unitWidth, unitHeight);

        int minSize = Math.round(unitSize / 5);

        int maxSize = Math.round(unitSize);

        Graphics2D g = (Graphics2D) image.getGraphics();

        int count = units.size();

        Unit first = null;
        int markerSize;
        for (Unit unit : units) {
            g.setPaint(dotColor);
            if ((first == null || first == unit) && differentStartMarker) {
                g.setPaint(Color.GREEN);
                first = unit;
            }

            if (unit == last && differentEndMarker) {
                g.setPaint(Color.RED);
            }

            if (frequencySize) {
                markerSize = minSize
                        + Math.round((unitFreq[unit.getXPos()][unit.getYPos()] - 1.0f) / count * (maxSize - minSize));
            } else {
                markerSize = minSize;
            }
            VisualisationUtils.drawUnitCentreMarker(g, unit, unitWidth, unitHeight, markerSize, markerSize);

        }
    }

    private void drawTrajectory(BufferedImage image, GrowingSOM gsom, Color lineColor, List<Unit> units)
            throws SOMToolboxException {

        Graphics2D g = (Graphics2D) image.getGraphics();

        int unitWidth = image.getWidth() / gsom.getLayer().getXSize();
        int unitHeight = image.getHeight() / gsom.getLayer().getYSize();

        Unit prevUnit = null;
        for (Unit unit : units) {
            if (prevUnit != null) {
                if (prevUnit.equals(unit)) {
                    continue;
                }
                g.setPaint(lineColor);
                drawArrow(g, prevUnit, unit, unitWidth, unitHeight);
            }
            prevUnit = unit;
        }

    }

    @Override
    protected String getVisualisationSpecificCacheKey(int currentVariant) {
        return XYFile + doXYVisualization + NameFile + doInputNameVisualization + VecFile + doVectorVisualization
                + differentStartMarker + differentEndMarker + frequencySize;
    }

    @Override
    public BufferedImage createVisualization(int variantIndex, GrowingSOM gsom, int width, int height)
            throws SOMToolboxException {

        BufferedImage image = new BufferedImage(width, height, Transparency.TRANSLUCENT);
        if (doXYVisualization && XYFile != "") {
            List<Unit> trajectory = loadXYTrajectory(gsom);
            drawCentreMarkers(image, gsom, Color.BLUE, trajectory);
            drawTrajectory(image, gsom, Color.RED, trajectory);
        }
        if (doInputNameVisualization && NameFile != "") {
            List<Unit> trajectory = loadNameTrajectory(gsom);
            drawCentreMarkers(image, gsom, Color.BLUE, trajectory);
            drawTrajectory(image, gsom, Color.RED, trajectory);
        }
        if (doVectorVisualization && VecFile != "") {
            List<Unit> trajectory = loadVecTrajectory(gsom);
            drawCentreMarkers(image, gsom, Color.BLUE, trajectory);
            drawTrajectory(image, gsom, Color.RED, trajectory);
        }

        return image;
    }

    @Override
    public int getPreferredScaleFactor() {
        return 1;
    }

    public List<Unit> loadXYTrajectory(GrowingSOM gsom) throws SOMToolboxException {
        List<Unit> trajectory = new LinkedList<Unit>();

        File file = new File(XYFile);
        if (!file.exists()) {
            throw new SOMToolboxException(XYFile + " does not exist.");
        }
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(file));
            String line = "";

            while ((line = br.readLine()) != null) {

                String[] columns = line.split(",");
                if (columns.length != 2) {
                    continue;
                }
                int x = Integer.parseInt(columns[0].trim());
                int y = Integer.parseInt(columns[1].trim());

                Unit unit = gsom.getLayer().getUnit(x, y);
                trajectory.add(unit);
            }

        } catch (Exception e) {
            throw new SOMToolboxException(e.getMessage());
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    throw new SOMToolboxException(e.getMessage());
                }
            }
        }
        return trajectory;
    }

    public List<Unit> loadNameTrajectory(GrowingSOM gsom) throws SOMToolboxException {
        List<Unit> trajectory = new LinkedList<Unit>();

        File file = new File(NameFile);
        if (!file.exists()) {
            throw new SOMToolboxException(NameFile + " does not exist.");
        }
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(file));
            String line = "";

            while ((line = br.readLine()) != null) {
                if (line.trim().equals("")) {
                    continue;
                }
                Unit unit = gsom.getLayer().getUnitForDatum(line.trim());
                trajectory.add(unit);
            }

        } catch (Exception e) {
            throw new SOMToolboxException(e.getMessage());
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    throw new SOMToolboxException(e.getMessage());
                }
            }
        }
        return trajectory;
    }

    public List<Unit> loadVecTrajectory(GrowingSOM gsom) throws SOMToolboxException {
        List<Unit> trajectory = new LinkedList<Unit>();

        File file = new File(VecFile);
        if (!file.exists()) {
            throw new SOMToolboxException(VecFile + " does not exist.");
        }
        try {
            InputData data = InputDataFactory.open(VecFile);

            int numVectors = data.numVectors();

            for (int i = 0; i < numVectors; i++) {
                Unit unit = gsom.getLayer().mapDatum(data.getInputDatum(i));
                trajectory.add(unit);
            }

        } catch (Exception e) {
            throw new SOMToolboxException(e.getMessage());
        }
        return trajectory;
    }

    private class TrajectoryVisualizerControlPanel extends VisualizationControlPanel {
        private static final long serialVersionUID = 1L;

        JLabel lblXYFname = new JLabel("Select a file...");

        JCheckBox xyVisualize = new JCheckBox("X/Y coordinates sequence", doXYVisualization);

        JLabel lblNameFname = new JLabel("Select a file...");

        JCheckBox nameVisualize = new JCheckBox("Input name sequence", doInputNameVisualization);

        JLabel lblVecFname = new JLabel("Select a file...");

        JCheckBox vecVisualize = new JCheckBox("Input vector sequence", doVectorVisualization);

        JCheckBox diffStartMarker = new JCheckBox("Different color for start nodes", differentStartMarker);

        JCheckBox diffEndMarker = new JCheckBox("Different color for end nodes", differentEndMarker);

        JCheckBox diffMarker = new JCheckBox("Denote node density", frequencySize);

        public TrajectoryVisualizerControlPanel() {
            super("TrajectoryVisualizer Control");
            c.insets = new Insets(1, 4, 1, 4);
            // ------------------------------------------------------
            // X-Y
            // ------------------------------------------------------
            xyVisualize.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (xyVisualize.isSelected()) {
                        int returnVal = CommonSOMViewerStateData.getInstance().getFileChooser().showOpenDialog(
                                TrajectoryVisualizerControlPanel.this);
                        if (returnVal == JFileChooser.APPROVE_OPTION) {
                            File file = CommonSOMViewerStateData.getInstance().getFileChooser().getSelectedFile();
                            XYFile = file.getAbsolutePath();
                            lblXYFname.setText(file.getAbsolutePath());
                            if (visualizationUpdateListener != null) {
                                visualizationUpdateListener.updateVisualization();
                            }
                        } else {
                            xyVisualize.setSelected(false);
                        }
                    }

                    doXYVisualization = xyVisualize.isSelected();
                    if (visualizationUpdateListener != null) {
                        visualizationUpdateListener.updateVisualization();
                    }
                }
            });
            add(xyVisualize, c);
            c.gridy++;
            JPanel xypanel = new JPanel();

            xypanel.add(lblXYFname);
            lblXYFname.setToolTipText("Filename of the file containing the list of coordinates to be visualized");
            JButton xySelect = new JButton("Open");
            xySelect.setToolTipText("Select file to visualize");
            xypanel.add(xySelect);
            xySelect.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    int returnVal = CommonSOMViewerStateData.getInstance().getFileChooser().showOpenDialog(
                            TrajectoryVisualizerControlPanel.this);
                    if (returnVal == JFileChooser.APPROVE_OPTION) {
                        File file = CommonSOMViewerStateData.getInstance().getFileChooser().getSelectedFile();
                        XYFile = file.getAbsolutePath();
                        lblXYFname.setText(file.getAbsolutePath());
                        if (visualizationUpdateListener != null) {
                            visualizationUpdateListener.updateVisualization();
                        }
                    }
                }
            });
            add(xypanel, c);
            c.gridy++;

            // ------------------------------------------------------
            // Named inputs
            // ------------------------------------------------------
            nameVisualize.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (nameVisualize.isSelected()) {
                        int returnVal = CommonSOMViewerStateData.getInstance().getFileChooser().showOpenDialog(
                                TrajectoryVisualizerControlPanel.this);
                        if (returnVal == JFileChooser.APPROVE_OPTION) {
                            File file = CommonSOMViewerStateData.getInstance().getFileChooser().getSelectedFile();
                            NameFile = file.getAbsolutePath();
                            lblNameFname.setText(file.getAbsolutePath());
                            if (visualizationUpdateListener != null) {
                                visualizationUpdateListener.updateVisualization();
                            }
                        } else {
                            nameVisualize.setSelected(false);
                        }
                    }
                    doInputNameVisualization = nameVisualize.isSelected();
                    if (visualizationUpdateListener != null) {
                        visualizationUpdateListener.updateVisualization();
                    }
                }
            });
            add(nameVisualize, c);
            c.gridy++;
            JPanel namepanel = new JPanel();

            namepanel.add(lblNameFname);
            lblNameFname.setToolTipText("Filename of the file containing the list of input vector names to be visualized");
            JButton nameSelect = new JButton("Open");
            nameSelect.setToolTipText("Select file to visualize");
            namepanel.add(nameSelect);
            nameSelect.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    int returnVal = CommonSOMViewerStateData.getInstance().getFileChooser().showOpenDialog(
                            TrajectoryVisualizerControlPanel.this);
                    if (returnVal == JFileChooser.APPROVE_OPTION) {
                        File file = CommonSOMViewerStateData.getInstance().getFileChooser().getSelectedFile();
                        lblNameFname.setText(file.getAbsolutePath());
                        NameFile = file.getAbsolutePath();
                        if (visualizationUpdateListener != null) {
                            visualizationUpdateListener.updateVisualization();
                        }
                    }
                }
            });
            add(namepanel, c);
            c.gridy++;

            // ------------------------------------------------------
            // Input vectors
            // ------------------------------------------------------
            vecVisualize.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (vecVisualize.isSelected()) {
                        int returnVal = CommonSOMViewerStateData.getInstance().getFileChooser().showOpenDialog(
                                TrajectoryVisualizerControlPanel.this);
                        if (returnVal == JFileChooser.APPROVE_OPTION) {
                            File file = CommonSOMViewerStateData.getInstance().getFileChooser().getSelectedFile();
                            VecFile = file.getAbsolutePath();
                            lblVecFname.setText(file.getAbsolutePath());
                            if (visualizationUpdateListener != null) {
                                visualizationUpdateListener.updateVisualization();
                            }
                        } else {
                            vecVisualize.setSelected(false);
                        }
                    }
                    doVectorVisualization = vecVisualize.isSelected();
                    if (visualizationUpdateListener != null) {
                        visualizationUpdateListener.updateVisualization();
                    }
                }
            });
            add(vecVisualize, c);
            c.gridy++;
            JPanel vecpanel = new JPanel();

            vecpanel.add(lblVecFname);
            lblVecFname.setToolTipText("Filename of the file containing the list of input vector names to be visualized");
            JButton vecSelect = new JButton("Open");
            vecSelect.setToolTipText("Select file to visualize");
            vecpanel.add(vecSelect);
            vecSelect.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    int returnVal = CommonSOMViewerStateData.getInstance().getFileChooser().showOpenDialog(
                            TrajectoryVisualizerControlPanel.this);
                    if (returnVal == JFileChooser.APPROVE_OPTION) {
                        File file = CommonSOMViewerStateData.getInstance().getFileChooser().getSelectedFile();
                        lblVecFname.setText(file.getAbsolutePath());
                        VecFile = file.getAbsolutePath();
                        if (visualizationUpdateListener != null) {
                            visualizationUpdateListener.updateVisualization();
                        }
                    }
                }
            });
            add(vecpanel, c);
            c.gridy++;

            diffStartMarker.setToolTipText("Color the starting node green");

            diffStartMarker.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    differentStartMarker = diffStartMarker.isSelected();
                    if (visualizationUpdateListener != null) {
                        visualizationUpdateListener.updateVisualization();
                    }
                }
            });
            add(diffStartMarker, c);
            c.gridy++;

            diffEndMarker.setToolTipText("Color the starting node green");

            diffEndMarker.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    differentEndMarker = diffEndMarker.isSelected();
                    if (visualizationUpdateListener != null) {
                        visualizationUpdateListener.updateVisualization();
                    }
                }
            });
            add(diffEndMarker, c);
            c.gridy++;

            diffMarker.setToolTipText("Enlarge the nodes according to their densities");

            diffMarker.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    frequencySize = diffMarker.isSelected();
                    if (visualizationUpdateListener != null) {
                        visualizationUpdateListener.updateVisualization();
                    }
                }
            });
            add(diffMarker, c);
            c.gridy++;

        }
    }
}
