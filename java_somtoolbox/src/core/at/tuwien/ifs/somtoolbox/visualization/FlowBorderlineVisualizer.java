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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.GridBagConstraints;
import java.awt.Stroke;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import javax.swing.AbstractCellEditor;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.Border;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

import org.apache.commons.math.util.MathUtils;

import at.tuwien.ifs.somtoolbox.SOMToolboxException;
import at.tuwien.ifs.somtoolbox.data.SOMLibTemplateVector;
import at.tuwien.ifs.somtoolbox.data.SOMVisualisationData;
import at.tuwien.ifs.somtoolbox.layers.LayerAccessException;
import at.tuwien.ifs.somtoolbox.layers.Unit;
import at.tuwien.ifs.somtoolbox.layers.metrics.MetricException;
import at.tuwien.ifs.somtoolbox.models.GrowingSOM;
import at.tuwien.ifs.somtoolbox.util.ImageUtils;
import at.tuwien.ifs.somtoolbox.util.StdErrProgressWriter;
import at.tuwien.ifs.somtoolbox.util.StringUtils;

/**
 * This class provides two visualizations:
 * <ul>
 * <li>Flow visualization</li>
 * <li>Borderline visualizations.</li>
 * </ul>
 * both described in:<br>
 * <i><b>Georg Poelzlbauer, Michael Dittenbach, Andreas Rauber</b>. Advanced visualization of Self-Organizing Maps with
 * vector fields. <a href=
 * "http://www.sciencedirect.com/science?_ob=GatewayURL&_method=citationSearch&_urlVersion=4&_origin=SDVIALERTHTML&_version=1&_uoikey=B6T08-4K7166Y-4&md5=f8260f66027afdb1d445893049cad9d6"
 * >Neural Networks, 19(6-7):911-922</a>, July-August 2006.</i>
 * 
 * @author Dominik Schnitzer
 * @author Peter Widhalm
 * @version $Id: FlowBorderlineVisualizer.java 4314 2014-01-17 12:25:40Z mayer $
 */
public class FlowBorderlineVisualizer extends AbstractBackgroundImageVisualizer {

    public static final String[] FLOWBORDER_SHORT_NAMES = new String[] { "Flow-E", "Border-E", "FlowBorder-E" };

    // Visualization Parameters
    private double sigma = 1.5;

    private double stretchConst = 0.7;

    private GrowingSOM gsom;

    private ArrayList<FlowGroup> flowGroups = new ArrayList<FlowGroup>();

    private boolean normalize = true; // edge normalization turned on or off (temporay?)

    int groups = 1; // number off feature sets

    private SOMLibTemplateVector templateVector = null;

    private FeatureXGroupTableModel featureTableModel;

    private GroupXVisualizationTableModel groupTableModel;

    public FlowBorderlineVisualizer() {
        NUM_VISUALIZATIONS = 3;
        VISUALIZATION_NAMES = new String[] { "Flow-E", "Borderline-E", "Flow & Borderline-E" };
        VISUALIZATION_SHORT_NAMES = FLOWBORDER_SHORT_NAMES;
        VISUALIZATION_DESCRIPTIONS = new String[] {
                "Inplementation of Flow Visualization as described in \""
                        + "G. Poelzlbauer, M. Dittenbach, A. Rauber. \n Advanced "
                        + "visualization of Self-Organizing Maps with vector fields.\n"
                        + "Neural Networks, 19(6-7):911-922, July-August 2006.\"", "Borderline Visualization Variant",
                "Combined Flow/Borderline Visualization Variant" };

        featureTableModel = new FeatureXGroupTableModel(groups);
        groupTableModel = new GroupXVisualizationTableModel(groups);

        neededInputObjects = new String[] { SOMVisualisationData.TEMPLATE_VECTOR };

        // the initial group
        flowGroups.add(new FlowGroup());

        if (!GraphicsEnvironment.isHeadless()) {
            controlPanel = new FlowBorderlineControlPanel();
        }
        // Scale for the FlowBorderlineVisualizer needs to be smaller, as the
        // visualisation is made of lines, which cannot be scaled too much.
        preferredScaleFactor = 2;
    }

    protected class FlowBorderlineControlPanel extends VisualizationControlPanel {
        private static final long serialVersionUID = 1L;

        protected JPanel sigmaPanel;

        public JSpinner sigmaSpinner;

        protected JPanel groupPanel;

        protected JPanel normalizePanel;

        protected JCheckBox normalizeCheckBox;

        public JSpinner groupSpinner;

        protected JTable groupTable;

        protected JTable featureTable;

        public FlowBorderlineControlPanel() {
            super("Flow/Borderline Control");

            ChangeListener updateChangeListener = new ChangeListener() {
                @Override
                public void stateChanged(ChangeEvent e) {
                    if (visualizationUpdateListener != null) {
                        clearAllFlows();
                        visualizationUpdateListener.updateVisualization();
                    }
                }
            };

            sigmaSpinner = new JSpinner(new SpinnerNumberModel(sigma, 0.5, 30.0, 0.1));
            sigmaSpinner.addChangeListener(new ChangeListener() {
                @Override
                public void stateChanged(ChangeEvent e) {
                    sigma = ((Double) ((JSpinner) e.getSource()).getValue()).doubleValue();
                }
            });
            sigmaSpinner.addChangeListener(updateChangeListener);

            c.anchor = GridBagConstraints.WEST;
            normalizePanel = new JPanel();
            normalizeCheckBox = new JCheckBox();
            normalizeCheckBox.setSelected(true);
            normalizeCheckBox.addChangeListener(new ChangeListener() {
                @Override
                public void stateChanged(ChangeEvent e) {
                    normalize = ((JCheckBox) e.getSource()).isSelected();
                }
            });
            normalizeCheckBox.addChangeListener(updateChangeListener);
            normalizePanel.add(new JLabel("Norm.Edges: "));
            normalizePanel.add(normalizeCheckBox);
            add(normalizePanel, c);
            c.gridy++;

            c.anchor = GridBagConstraints.WEST;
            sigmaPanel = new JPanel();
            sigmaPanel.add(new JLabel("Sigma: "));
            sigmaPanel.add(sigmaSpinner);
            add(sigmaPanel, c);
            c.gridy++;

            groupSpinner = new JSpinner(new SpinnerNumberModel(groups, 1, 5, 1));
            groupSpinner.addChangeListener(new ChangeListener() {
                // Eventhandling seem scleaner here than putting it in MyTableModel

                @Override
                public void stateChanged(ChangeEvent e) {
                    groups = Math.max(1, (Integer) ((JSpinner) e.getSource()).getValue());
                    while (flowGroups.size() != groups) {
                        if (flowGroups.size() > groups) {
                            flowGroups.remove(flowGroups.size() - 1);
                        } else {
                            flowGroups.add(new FlowGroup());
                        }
                    }
                    featureTableModel.setGroups(groups);
                    groupTableModel.setGroupSize(groups);
                }
            });

            groupSpinner.setEnabled(false);
            groupPanel = new JPanel();
            groupPanel.add(new JLabel("Groups: "));
            groupPanel.add(groupSpinner);
            add(groupPanel, c);
            c.gridy++;

            groupTable = new JTable(groupTableModel);
            groupTable.setDefaultRenderer(Color.class, new ColorRenderer(true));
            groupTable.setDefaultEditor(Color.class, new ColorEditor());

            groupTable.getColumnModel().getColumn(0).setMaxWidth(28);
            groupTable.getColumnModel().getColumn(0).setMaxWidth(28);

            groupTableModel.addTableModelListener(new TableModelListener() {
                @Override
                public void tableChanged(TableModelEvent arg0) {

                    if (visualizationUpdateListener != null && arg0.getType() == TableModelEvent.UPDATE) {
                        visualizationUpdateListener.updateVisualization();
                    }
                }
            });
            JScrollPane scrollPane = new JScrollPane(groupTable);
            scrollPane.setViewportView(groupTable);
            scrollPane.setPreferredSize(new Dimension(90, 54));
            add(scrollPane, c);
            c.gridy++;

            featureTable = new JTable(featureTableModel);
            featureTable.setFillsViewportHeight(true);

            featureTableModel.addTableModelListener(new TableModelListener() {
                @Override
                public void tableChanged(TableModelEvent arg0) {
                    int column = arg0.getColumn();
                    if (column > 0 && visualizationUpdateListener != null && arg0.getType() == TableModelEvent.UPDATE) {
                        flowGroups.get(column - 1).setInvalid();
                        visualizationUpdateListener.updateVisualization();
                    }
                }
            });
            scrollPane = new JScrollPane(featureTable);
            scrollPane.setViewportView(featureTable);
            featureTable.setVisible(false);
            scrollPane.setPreferredSize(new Dimension(90, 260));
            add(scrollPane, c);

        }

    }

    // My intention is to have a JTable looking for example like this:
    // Features G1 G2 G2 ...
    // Petal-Length x x
    // Petal-Width x x x
    // Petal-Curve x x
    protected class FeatureXGroupTableModel extends AbstractTableModel {
        private static final long serialVersionUID = -1822320695826644634L;

        // yeah an object array would have been simpler, but i kinda dislike a
        // potentially large array of Boolean classes
        private boolean labelsInitialized = false;

        private String[] labels;

        private boolean[][] selection;

        public FeatureXGroupTableModel(int groups) {
            labels = new String[0];
            selection = new boolean[0][];
            setGroups(groups);
        }

        public void setLabels(String[] _labels) {
            if (labelsInitialized) {
                return;
            }
            if (_labels == null) {
                labels = new String[0];
            } else {
                this.labels = _labels;
            }
            setData(labels.length);
            labelsInitialized = true;
        }

        // upon initialization we try to get fed the #-of features and their
        // names
        public void setData(int features) {

            if (features < 0) {
                return;
            }

            selection = new boolean[features][];

            for (int i = 0; i < features; i++) {
                selection[i] = new boolean[groups];
                for (int j = 0; j < groups; j++) {
                    selection[i][j] = true;
                }
            }
            this.fireTableStructureChanged();
        }

        public void setGroups(int newGroups) {
            for (int i = 0; i < labels.length; i++) // keep previous selections
            // intact
            {
                boolean[] newselection = new boolean[newGroups];
                for (int j = 0; j < newGroups && j < selection[i].length; j++) {
                    newselection[j] = selection[i][j];
                }
                selection[i] = newselection;
            }
            this.fireTableStructureChanged();
            this.fireTableDataChanged();
        }

        @Override
        public int getColumnCount() {
            return groups + 1;
        }

        @Override
        public int getRowCount() {
            return labels.length;
        }

        @Override
        public String getColumnName(int col) {
            if (col == 0) {
                return "Feature";
            }
            return "G" + col;
        }

        @Override
        public Object getValueAt(int row, int col) {
            if (col == 0) {
                return labels[row];
            }
            return selection[row][col - 1];
        }

        @Override
        public Class getColumnClass(int c) {
            if (c == 0) {
                return String.class;
            }
            return Boolean.class;
        }

        @Override
        public boolean isCellEditable(int row, int col) {
            return col != 0;
        }

        @Override
        public void setValueAt(Object value, int row, int col) {
            selection[row][col - 1] = (Boolean) value;
            fireTableCellUpdated(row, col);
        }

        public Boolean isSelected(int group, int feature) {
            return selection[feature][group];
        }
    }

    // My intention is to have a JTable looking for example like this:
    // Group-# Flow-Color Border-Color Opacity
    // 1 RED YELLOW 1.0
    // 2 BLUE BlACK 0.3
    // 3 GREEN GRAY 0.7
    protected class GroupXVisualizationTableModel extends AbstractTableModel {
        private static final long serialVersionUID = 838274140587521320L;

        private String[] columnNames = { "", "Flow", "Border", "Opacity" };

        private ArrayList<Group> groups = new ArrayList<Group>();

        @Override
        public int getColumnCount() {
            return columnNames.length;
        }

        @Override
        public int getRowCount() {
            return getGroups();
        }

        public int getGroups() {
            return groups.size();
        }

        private Group[] defaultGroups = new Group[] { new Group("G1", Color.BLUE, Color.RED, new Double(1.0)),
                new Group("G2", Color.CYAN, Color.ORANGE, new Double(1.0)),
                new Group("G3", Color.MAGENTA, Color.GRAY, new Double(1.0)),
                new Group("G4", Color.BLACK, Color.YELLOW, new Double(1.0)),
                new Group("G5", Color.GREEN, Color.PINK, new Double(1.0)) };

        public GroupXVisualizationTableModel(int groupSize) {
            setGroupSize(groupSize);
        }

        public void setGroupSize(int groupSize) {

            while (groups.size() != groupSize) {
                if (groupSize > groups.size()) {
                    // on purpose - we will alter default groups, so changes
                    // will persist
                    groups.add(defaultGroups[groups.size()]);
                } else {
                    groups.remove(groups.size() - 1);
                }
            }
            this.fireTableDataChanged();
        }

        public Color getFlowColor(int group) {
            return groups.get(group).getFlowColor();
        }

        public Color getBorderColor(int group) {
            return groups.get(group).getBorderColor();
        }

        public double getOpacity(int group) {
            return groups.get(group).getOpacity();
        }

        @Override
        public String getColumnName(int col) {
            return columnNames[col];
        }

        @Override
        public Object getValueAt(int row, int col) {
            Group group = groups.get(row);
            switch (col) {
                case 0:
                    return group.getName();
                case 1:
                    return group.getFlowColor();
                case 2:
                    return group.getBorderColor();
                case 3:
                    return group.getOpacity();
            }
            return null;
        }

        @Override
        public Class getColumnClass(int c) {
            return getValueAt(0, c).getClass();
        }

        @Override
        public boolean isCellEditable(int row, int col) {
            return col != 0;
        }

        @Override
        public void setValueAt(Object value, int row, int col) {
            Group group = groups.get(row);
            switch (col) {
                case 0:
                    group.setName((String) value);
                    break;
                case 1:
                    group.setFlowColor((Color) value);
                    break;
                case 2:
                    group.setBorderColor((Color) value);
                    break;
                case 3:
                    group.setOpacity((Double) value);
                    break;
            }
            fireTableCellUpdated(row, col);
        }

        private class Group {

            private String name;

            private Color flowColor;

            private Color borderColor;

            private Double opacity;

            public Group(String name, Color flowColor, Color borderColor, double opacity) {
                super();
                this.name = name;
                this.flowColor = flowColor;
                this.borderColor = borderColor;
                this.opacity = opacity;
            }

            public Color getFlowColor() {
                return flowColor;
            }

            public void setFlowColor(Color flowColor) {
                this.flowColor = flowColor;
            }

            public Color getBorderColor() {
                return borderColor;
            }

            public void setBorderColor(Color borderColor) {
                this.borderColor = borderColor;
            }

            public double getOpacity() {
                return opacity;
            }

            public void setOpacity(double opacity) {
                if (opacity < 0) {
                    opacity = 0;
                } else if (opacity > 1) {
                    opacity = 1;
                } else {
                    this.opacity = opacity;
                }
            }

            public String getName() {
                return name;
            }

            public void setName(String name) {
                this.name = name;
            }

        }
    }

    // next two classes are copied from:
    // http://docs.oracle.com/javase/tutorial/uiswing/components/table.html
    public class ColorRenderer extends JLabel implements TableCellRenderer {
        Border unselectedBorder = null;

        Border selectedBorder = null;

        boolean isBordered = true;

        public ColorRenderer(boolean isBordered) {
            this.isBordered = isBordered;
            setOpaque(true); // MUST do this for background to show up.
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object color, boolean isSelected,
                boolean hasFocus, int row, int column) {
            Color newColor = (Color) color;
            setBackground(newColor);
            if (isBordered) {
                if (isSelected) {
                    if (selectedBorder == null) {
                        selectedBorder = BorderFactory.createMatteBorder(2, 5, 2, 5, table.getSelectionBackground());
                    }
                    setBorder(selectedBorder);
                } else {
                    if (unselectedBorder == null) {
                        unselectedBorder = BorderFactory.createMatteBorder(2, 5, 2, 5, table.getBackground());
                    }
                    setBorder(unselectedBorder);
                }
            }

            setToolTipText("RGB value: " + newColor.getRed() + ", " + newColor.getGreen() + ", " + newColor.getBlue());
            return this;
        }
    }

    public class ColorEditor extends AbstractCellEditor implements TableCellEditor, ActionListener {
        Color currentColor;

        JButton button;

        JColorChooser colorChooser;

        JDialog dialog;

        protected static final String EDIT = "edit";

        public ColorEditor() {
            button = new JButton();
            button.setActionCommand(EDIT);
            button.addActionListener(this);
            button.setBorderPainted(false);

            // Set up the dialog that the button brings up.
            colorChooser = new JColorChooser();
            dialog = JColorChooser.createDialog(button, "Pick a Color", true, // modal
                    colorChooser, this, // OK button handler
                    null); // no CANCEL button handler
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if (EDIT.equals(e.getActionCommand())) {
                // The user has clicked the cell, so
                // bring up the dialog.
                button.setBackground(currentColor);
                colorChooser.setColor(currentColor);
                dialog.setVisible(true);

                fireEditingStopped(); // Make the renderer reappear.

            } else { // Us<er pressed dialog's "OK" button.
                currentColor = colorChooser.getColor();
            }
        }

        // Implement the one CellEditor method that AbstractCellEditor doesn't.
        @Override
        public Object getCellEditorValue() {
            return currentColor;
        }

        // Implement the one method defined by TableCellEditor.
        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            currentColor = (Color) value;
            return button;
        }
    }

    private void clearFlows(int group) {
        // FIXME: consider having a cache for ax, ay, maxa, instead of always
        // computing it
        this.flowGroups.get(group).setInvalid();
    }

    private void clearAllFlows() {
        for (int i = 0; i < flowGroups.size(); i++) {
            clearFlows(i);
        }
    }

    public void setLabels(SOMLibTemplateVector templateVector) {
        if (templateVector != null) {
            featureTableModel.setLabels(templateVector.getLabels());
        }
    }

    @Override
    public BufferedImage createVisualization(int index, GrowingSOM gsom, int width, int height)
            throws SOMToolboxException {
        checkVariantIndex(index, getClass());

        // max-value for sigma = max diagonal
        double maxSigma = gsom.getLayer().maxNeighbourhoodRadius();
        ((FlowBorderlineControlPanel) controlPanel).sigmaSpinner.setModel(new SpinnerNumberModel(sigma, 0.5, maxSigma,
                0.1));
        ((FlowBorderlineControlPanel) controlPanel).sigmaSpinner.setToolTipText("Value for the neighbourhood radius. Ranges from 0.5 to "
                + maxSigma + ", the maximum value for the diagonal distance.");

        this.gsom = gsom;

        for (int group = 0; group < flowGroups.size(); group++) {
            if (!flowGroups.get(group).isValid()) {
                // only calculate flow when really needed
                try {
                    calculateFlows(group);
                } catch (Exception ex) {
                    Logger.getLogger("at.tuwien.ifs.somtoolbox").severe(ex.getMessage());
                    return null;
                }
            }
        }

        BufferedImage res = ImageUtils.createEmptyImage(width, height);
        Graphics2D g = (Graphics2D) res.getGraphics();
        g.setColor(Color.blue);

        if (templateVector == null) {
            templateVector = gsom.getSharedInputObjects().getTemplateVector();
            /* couldn't do this - is there a method for just a warning ?  
             * if (templateVector == null) {
              throw new SOMToolboxException("For multiple flows/borderlines you'll need to specify the "
                  + neededInputObjects[0]);
            }*/
            ((FlowBorderlineControlPanel) controlPanel).groupSpinner.setToolTipText("For multiple flows/borderlines you'll need to specify the "
                    + neededInputObjects[0]);
        }

        if (templateVector != null) {
            setLabels(templateVector);
            ((FlowBorderlineControlPanel) controlPanel).groupSpinner.setEnabled(true);
            ((FlowBorderlineControlPanel) controlPanel).groupSpinner.setToolTipText("");
            ((FlowBorderlineControlPanel) controlPanel).featureTable.setVisible(true);
        }

        for (int group = 0; group < flowGroups.size(); group++) {
            FlowGroup flowGroup = flowGroups.get(group);
            if (flowGroup.isValid()) { // invalid means no selected components
                double unitWidth = width / gsom.getLayer().getXSize();
                double stretchBorder = unitWidth / flowGroup.getMaxa() * stretchConst;

                // draw thicker lines, but save existing stroke first
                Stroke oldStroke = g.getStroke();
                g.setStroke(new BasicStroke((int) Math.round(unitWidth / 7)));

                Color flowColor = getTransparentColor(groupTableModel.getFlowColor(group),
                        groupTableModel.getOpacity(group));
                Color borderColor = getTransparentColor(groupTableModel.getBorderColor(group),
                        groupTableModel.getOpacity(group));

                for (int x = 0; x < gsom.getLayer().getXSize(); x++) {
                    for (int y = 0; y < gsom.getLayer().getYSize(); y++) {
                        draw(index, g, x, y, unitWidth, stretchBorder, flowColor, borderColor, flowGroup.getAx(),
                                flowGroup.getAy());
                    }
                }
                g.setStroke(oldStroke); // reset to original stroke
            }
        }

        return res;
    }

    private Color getTransparentColor(Color color, double opacity) {
        return new Color(color.getRed(), color.getGreen(), color.getBlue(), (int) (opacity * 255));
    }

    /**
     * Formel 1 distance in feature space
     */
    private double df(int x1, int y1, int x2, int y2, double[][][][] weightVectors) throws LayerAccessException,
            MetricException {
        // Formel
        double distance = gsom.getLayer().getMetric().distance(weightVectors[x1][y1][0], weightVectors[x2][y2][0]);
        return distance;
    }

    private double[] getWeightFactorByComponents(double[] weightVector, boolean[] components, int selected) {
        double[] selectedWeightVector = new double[selected];
        int selectedPos = 0;
        for (int unselectedPos = 0; unselectedPos < weightVector.length; unselectedPos++) {
            if (components[unselectedPos]) {
                selectedWeightVector[selectedPos++] = weightVector[unselectedPos];
            }
            if (selectedPos > selected) {
                throw new RuntimeException("Selection-Error in FlowBorderLoneVisualizer");
            }
        }
        if (selectedPos < selected) {
            throw new RuntimeException("Selection-Error in FlowBorderLoneVisualizer");
        }

        for (int i = 0; i < selectedWeightVector.length; i++) {
            selectedWeightVector[i] = selectedWeightVector[i];
        }

        return selectedWeightVector;
    }

    private double[][][][] getSelectedComponentsWeightVectors(Unit[][][] units, int group) {
        int zSize = units[0][0].length;
        int ySize = units[0].length;
        int xSize = units.length;

        int featureSize = gsom.getLayer().getDim();

        double[][][][] weightVector = new double[xSize][ySize][zSize][featureSize];
        boolean[] selectedComponents = getSelection(featureSize, group);
        int selected = countSelectedComponents(selectedComponents);

        // infeasible is no component selected
        if (selected == 0) {
            return null;
        }

        for (int k = 0; k < zSize; k++) {
            for (int j = 0; j < ySize; j++) {
                for (int i = 0; i < xSize; i++) {
                    ;
                    weightVector[i][j][k] = getWeightFactorByComponents(units[i][j][k].getWeightVector(),
                            selectedComponents, selected);
                }
            }
        }
        return weightVector;
    }

    private int countSelectedComponents(boolean[] data) {
        int selected = 0;
        for (boolean element : data) {
            selected += element ? 1 : 0;
        }
        return selected;
    }

    // returns a boolean vector of selected components for a given group
    private boolean[] getSelection(int featureSize, int group) {
        boolean allVectors = this.templateVector == null; // default

        boolean[] selectedComponents = new boolean[featureSize];
        for (int feature = 0; feature < featureSize; feature++) {
            if (allVectors || featureTableModel.isSelected(group, feature)) {
                selectedComponents[feature] = true;
            }
        }
        return selectedComponents;
    }

    private String getSelectionString(boolean[] data) {
        StringBuffer buffer = new StringBuffer();
        for (boolean element : data) {
            buffer.append(element ? "1" : "0");
        }
        return buffer.toString();
    }

    /**
     * Formel 2 distance in output space
     */
    private double dout(int x1, int y1, int x2, int y2) {
        // Formel 2
        return Math.sqrt((x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2));
    }

    /**
     * Formel 3
     */
    private double kernel(double dout) {
        // Formel 3
        // return Math.exp(dout*dout/(2.0*sigma));
        // FIXME: why not the function above, which is in the paper ?
    	return Math.exp(-1.0 * dout * dout / (2 * sigma * sigma));
    }

    /**
     * Formel 8, 9 10, 11, 12, 13, 14, 15, 16
     */
    private void calculateFlows(int group) throws LayerAccessException, MetricException {

        int xSize = gsom.getLayer().getXSize();
        int ySize = gsom.getLayer().getYSize();
        double[][] ax = new double[xSize][ySize];
        double[][] ay = new double[xSize][ySize];
        double maxa = 0.0;

        StdErrProgressWriter progress = new StdErrProgressWriter(xSize * ySize, "Calculating flows for unit ", xSize
                * ySize / 10);

        Unit[][][] units = gsom.getLayer().getUnits();

        // reduce weight vector to the selected components
        double[][][][] weightVectors = getSelectedComponentsWeightVectors(units, group);

        if (weightVectors != null) {
            for (int x = 0; x < xSize; x++) {
                for (int y = 0; y < ySize; y++) {

                    double roxplus = 0.0;
                    double roxminus = 0.0;
                    double royplus = 0.0;
                    double royminus = 0.0;

                    double omegaxplus = 0.0;
                    double omegaxminus = 0.0;
                    double omegayplus = 0.0;
                    double omegayminus = 0.0;

                    for (int x2 = 0; x2 < xSize; x2++) {
                        for (int y2 = 0; y2 < ySize; y2++) {

                            if (x != x2 || y != y2) {

                                // Formel 8
                                int doutx = x2 - x;
                                int douty = y2 - y;

                                // Formel 9
                                double alpha = Math.atan2(douty, doutx);

                                // Formel 10
                                double h = kernel(dout(x, y, x2, y2));

                                // Formel 11, 12, 13, 14, 15
                                double df = df(x, y, x2, y2, weightVectors);

                                double omegax = Math.cos(alpha) * h;
                                double omegay = Math.sin(alpha) * h;

                                if (omegax > 0.0) {
                                    roxplus += omegax * df;
                                    omegaxplus += omegax;
                                } else {
                                    roxminus += -omegax * df;
                                    omegaxminus += -omegax;
                                }
                                if (omegay > 0.0) {
                                    royplus += omegay * df;
                                    omegayplus += omegay;
                                } else {
                                    royminus += -omegay * df;
                                    omegayminus += -omegay;
                                }

                            }
                        }
                    }

                    if (this.normalize) {
                        // Formel 16
                        ax[x][y] = (-roxminus * omegaxplus + roxplus * omegaxminus) / (roxplus + roxminus);
                        ay[x][y] = (-royminus * omegayplus + royplus * omegayminus) / (royplus + royminus);
                    } else {
                        // FIXME try Formel 16 unnormalized
                        ax[x][y] = -roxminus * omegaxplus + roxplus * omegaxminus;
                        ay[x][y] = -royminus * omegayplus + royplus * omegayminus;
                    }

                    if (ax[x][y] > maxa) {
                        maxa = ax[x][y];
                    }
                    if (ay[x][y] > maxa) {
                        maxa = ay[x][y];
                    }
                    progress.progress();
                }
            }
            flowGroups.set(group, new FlowGroup(ax, ay, maxa));
        } else {
            flowGroups.set(group, new FlowGroup());
        }
    }

    private void draw(int mode, Graphics2D g, int x, int y, double unitWidth, double stretchBorder, Color flowColor,
            Color borderColor, double[][] ax, double[][] ay) {

        if (mode == 1 || mode == 2) {

            int bx1 = (int) (x * unitWidth + unitWidth / 2.0 - ay[x][y] * stretchBorder);
            int by1 = (int) (y * unitWidth + unitWidth / 2.0 + ax[x][y] * stretchBorder);
            int bx2 = (int) (x * unitWidth + unitWidth / 2.0 + ay[x][y] * stretchBorder);
            int by2 = (int) (y * unitWidth + unitWidth / 2.0 - ax[x][y] * stretchBorder);

            float w = (float) ((float) Math.sqrt(Math.pow(bx1 - bx2, 2) + Math.pow(by1 - by2, 2)) * .2 / 3);

            g.setStroke(new BasicStroke(w));
            g.setColor(borderColor);
            g.drawLine(bx1, by1, bx2, by2);
        }

        if (mode == 0 || mode == 2) {

            double lengthx = ax[x][y] * stretchBorder;
            double lengthy = ay[x][y] * stretchBorder;

            int bx1 = (int) (x * unitWidth + unitWidth / 2.0);
            int by1 = (int) (y * unitWidth + unitWidth / 2.0);
            int bx2 = (int) (x * unitWidth + unitWidth / 2.0 - lengthx);
            int by2 = (int) (y * unitWidth + unitWidth / 2.0 - lengthy);

            int deltaX = bx2 - bx1;
            int deltaY = by2 - by1;
            double frac = 0.2;

            int headTipX = bx2 + (int) (deltaX * 0.2);
            int headTipY = by2 + (int) (deltaY * 0.2);

            int headEndX1 = bx1 + (int) ((1 - frac) * deltaX + frac * deltaY);
            int headEndY1 = by1 + (int) ((1 - frac) * deltaY - frac * deltaX);
            int headEndX2 = bx1 + (int) ((1 - frac) * deltaX - frac * deltaY);
            int headEndY2 = by1 + (int) ((1 - frac) * deltaY + frac * deltaX);

            float w = (float) (Math.sqrt(Math.pow(headEndX1 - headEndX2, 2) + Math.pow(headEndY1 - headEndY2, 2)) / 3f);

            g.setStroke(new BasicStroke(w));
            g.setColor(flowColor);

            g.drawLine(bx1, by1, bx2, by2);
            g.fillPolygon(new int[] { headTipX, headEndX1, headEndX2 }, new int[] { headTipY, headEndY1, headEndY2 }, 3);
        }
    }

    /**
     * Scale for the {@link FlowBorderlineVisualizer} needs to be smaller, as the visualisation is made of lines, which
     * cannot be scaled too much.
     */
    @Override
    public int getPreferredScaleFactor() {
        return 2;
    }

    @Override
    protected String getVisualisationSpecificCacheKey(int currentVariant) {
        StringBuffer cacheKey = new StringBuffer();
        cacheKey.append("sigma:");
        cacheKey.append(sigma);
        cacheKey.append(" opacity:");
        cacheKey.append(groupTableModel.getOpacity(0));
        if (gsom != null) {
            for (int group = 0; group < flowGroups.size(); group++) {
                cacheKey.append(" Flow:");
                cacheKey.append(groupTableModel.getFlowColor(group).getRGB());
                cacheKey.append(" Border:");
                cacheKey.append(groupTableModel.getBorderColor(group).getRGB());
                String selectedComponentString = getSelectionString(getSelection(gsom.getLayer().getDim(), group));
                cacheKey.append(" Selection(" + group + "):");
                cacheKey.append(selectedComponentString);
            }
        }
        if (normalize) {
            cacheKey.append(" Normalized");
        }
        return cacheKey.toString();
    }

    @Override
    public HashMap<String, BufferedImage> getVisualizationFlavours(int index, GrowingSOM gsom, int width, int height)
            throws SOMToolboxException {
        return getVisualizationFlavours(index, gsom, width, height, -1);
    }

    @Override
    public HashMap<String, BufferedImage> getVisualizationFlavours(int index, GrowingSOM gsom, int width, int height,
            int maxFlavours) throws SOMToolboxException {
        // create images from sigma = 0.1 to 3.0 in steps of 0.1, then in steps o 0.2 till 10, then in steps of 0.5
        // or until maxFlavours is reached
        HashMap<String, BufferedImage> images = new HashMap<String, BufferedImage>();
        double maxNeighbourhoodRadius = gsom.getLayer().maxNeighbourhoodRadius();
        int numberOfDigits = at.tuwien.ifs.commons.util.MathUtils.numberOfDigits(Math.round(maxNeighbourhoodRadius));

        double currentSigma = sigma; // save old value
        sigma = 0.5;
        while (MathUtils.round(sigma, 1) <= maxNeighbourhoodRadius
                && (maxFlavours == -1 || images.size() <= maxFlavours)) {
            clearAllFlows();
            images.put("_sigma_" + StringUtils.format(sigma, 1, true, numberOfDigits),
                    getVisualization(index, gsom, width, height));
            if (MathUtils.round(sigma, 1) < 1.0) {
                sigma += 0.1;
            } else if (MathUtils.round(sigma, 1) < 30) {
                sigma += 0.5;
            } else {
                sigma += 1;
            }
        }
        sigma = currentSigma;
        return images;
    }

    @Override
    public HashMap<String, BufferedImage> getVisualizationFlavours(int index, GrowingSOM gsom, int width, int height,
            Map<String, String> flavourParameters) throws SOMToolboxException {
        // FIXME: implement this
        return getVisualizationFlavours(index, gsom, width, height);
    }

    private class FlowGroup {
        private double[][] ax; // x-komponente des flow

        private double[][] ay; // y-komponente des flow

        private double maxa; // maximale flow-Komponente (fuer Normalisierung bei

        // Darstellung)
        private boolean valid = true;

        public FlowGroup() {
            valid = false;
        }

        public FlowGroup(double[][] ax, double[][] ay, double maxa) {
            super();
            this.ax = ax;
            this.ay = ay;
            this.maxa = maxa;
        }

        public double[][] getAx() {
            return ax;
        }

        public double[][] getAy() {
            return ay;
        }

        public double getMaxa() {
            return maxa;
        }

        public boolean isValid() {
            return valid;
        }

        public void setInvalid() {
            this.valid = false;
        }

    }
}