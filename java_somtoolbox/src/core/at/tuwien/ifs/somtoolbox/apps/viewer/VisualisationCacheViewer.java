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
package at.tuwien.ifs.somtoolbox.apps.viewer;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Map.Entry;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import at.tuwien.ifs.commons.gui.util.MultiLineJLabel;
import at.tuwien.ifs.commons.util.io.ExtensionFileFilterSwing;
import at.tuwien.ifs.somtoolbox.SOMToolboxException;
import at.tuwien.ifs.somtoolbox.apps.viewer.fileutils.ExportUtils;
import at.tuwien.ifs.somtoolbox.util.CentredDialog;
import at.tuwien.ifs.somtoolbox.util.GridBagConstraintsIFS;
import at.tuwien.ifs.somtoolbox.util.ImageUtils;
import at.tuwien.ifs.somtoolbox.util.LeastRecentelyUsedImageCache;
import at.tuwien.ifs.somtoolbox.util.StringUtils;
import at.tuwien.ifs.somtoolbox.util.UiUtils;
import at.tuwien.ifs.somtoolbox.visualization.AbstractBackgroundImageVisualizer;
import at.tuwien.ifs.somtoolbox.visualization.BackgroundImageVisualizer;

/**
 * Views the contents of the visualisation cache, and allows to delete and save individual visualisations.
 * 
 * @author Rudolf Mayer
 * @version $Id: VisualisationCacheViewer.java 4166 2011-02-13 12:49:02Z mayer $
 */
public class VisualisationCacheViewer extends CentredDialog {

    private static final long serialVersionUID = 1L;

    private SOMViewer somViewer;

    private LeastRecentelyUsedImageCache cache = AbstractBackgroundImageVisualizer.getCache();

    private SpinnerNumberModel modelMaxCacheSize = new SpinnerNumberModel(cache.getMaxCacheSizeInMBit(), 100, 8 * 1024,
            1);

    private JLabel labelCurrentCacheSize = new JLabel();

    private JPanel contentsPanel = new JPanel(new GridBagLayout());

    public VisualisationCacheViewer(SOMViewer somViewer) {
        super(somViewer, "Visualisation Cache", false);
        this.somViewer = somViewer;

        setLayout(new BorderLayout(5, 5));

        // top panel, showing current cache size
        JSpinner spinnerCacheSize = new JSpinner(modelMaxCacheSize);
        spinnerCacheSize.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                cache.setMaxCacheSizeInMBit(modelMaxCacheSize.getNumber().intValue());
            }
        });
        setCurrentCacheSizeValues();
        setCacheContents();

        JPanel cacheInfoPanel = UiUtils.makeBorderedPanel(new GridBagLayout(), "Cache Size");
        GridBagConstraintsIFS c = new GridBagConstraintsIFS().setInsets(5, 5).setAnchor(GridBagConstraints.EAST);
        cacheInfoPanel.add(new JLabel("Maximum cache size"), c);
        cacheInfoPanel.add(spinnerCacheSize, c.nextCol());
        cacheInfoPanel.add(new JLabel("Mbit"), c.nextCol());
        cacheInfoPanel.add(new JLabel("Current cache used"), c.nextRow());
        cacheInfoPanel.add(labelCurrentCacheSize, c.nextCol());
        cacheInfoPanel.add(new JLabel("Mbit"), c.nextCol());

        // centre panel, showing vis images
        JScrollPane scrollPaneContents = new JScrollPane(contentsPanel);
        scrollPaneContents.setBorder(BorderFactory.createTitledBorder("Cache Contents"));

        // bottom panel - control buttons
        JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setCacheContents();
                setCurrentCacheSizeValues();
            }
        });
        JButton clearButton = new JButton("Clear cache");
        clearButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // clear the visualisation cache
                AbstractBackgroundImageVisualizer.clearVisualisationCache();
                setCacheContents();
                setCurrentCacheSizeValues();
            }
        });

        JPanel buttonsPanel = new JPanel();
        buttonsPanel.add(clearButton);
        buttonsPanel.add(refreshButton);

        // final layout
        add(cacheInfoPanel, BorderLayout.NORTH);
        add(scrollPaneContents, BorderLayout.CENTER);
        add(buttonsPanel, BorderLayout.SOUTH);

        pack();
        setVisible(true);
    }

    private void setCacheContents() {
        contentsPanel.removeAll();

        GridBagConstraintsIFS cContents = new GridBagConstraintsIFS();
        cContents.setInsets(10);
        for (Entry<String, BufferedImage> entry : cache.entrySet()) {
            final String cacheKey = entry.getKey();
            String label = cacheKey.replace(BackgroundImageVisualizer.CACHE_KEY_SECTION_SEPARATOR, "\n\t"
                    + BackgroundImageVisualizer.CACHE_KEY_SECTION_SEPARATOR);
            label += "\n\n" + StringUtils.format(ImageUtils.getSizeOfImageInMBit(entry.getValue()), 2) + " Mbit";

            final MultiLineJLabel cacheKeyLabel = new MultiLineJLabel(label);
            BufferedImage value = ImageUtils.scaleImage(entry.getValue(), 100);
            final JLabel visLabel = new JLabel(new ImageIcon(value));
            final JButton deleteButton = new JButton("Delete");
            final JButton saveButton = new JButton("Save");
            deleteButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    cache.remove(cacheKey);
                    setCurrentCacheSizeValues();
                    contentsPanel.remove(cacheKeyLabel);
                    contentsPanel.remove(visLabel);
                    contentsPanel.remove(deleteButton);
                    contentsPanel.remove(saveButton);
                }
            });
            saveButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    JFileChooser fileChooser = somViewer.getSOMViewerState().getFileChooser();
                    String defaultFileName = AbstractBackgroundImageVisualizer.getDefaultVisualisationFileName(cacheKey)
                            + ".png";
                    File filePath = ExportUtils.getFilePath(somViewer, fileChooser, "Save Visualization as PNG",
                            defaultFileName, new ExtensionFileFilterSwing(false, "png"));
                    if (filePath != null) {
                        try {
                            ExportUtils.saveVisualizationAsImage(somViewer.getSOMViewerState(), -1,
                                    filePath.getAbsolutePath());
                            JOptionPane.showMessageDialog(somViewer, "Export to file finished!");
                        } catch (SOMToolboxException ex) {
                            ex.printStackTrace();
                            JOptionPane.showMessageDialog(getParent(), ex.getMessage(), "Error saving",
                                    JOptionPane.ERROR_MESSAGE);
                        }
                    }
                }
            });

            contentsPanel.add(cacheKeyLabel, cContents.nextRow());
            contentsPanel.add(visLabel, cContents.nextCol());
            contentsPanel.add(deleteButton, cContents.nextCol());
            contentsPanel.add(saveButton, cContents.nextCol());
        }
    }

    private void setCurrentCacheSizeValues() {
        modelMaxCacheSize.setValue(cache.getMaxCacheSizeInMBit());
        labelCurrentCacheSize.setText(StringUtils.format(cache.getCurrentSizeInMBit(), 2));
    }

}
