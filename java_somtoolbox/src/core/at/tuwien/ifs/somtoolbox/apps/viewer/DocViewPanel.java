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
package at.tuwien.ifs.somtoolbox.apps.viewer;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Highlighter;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.TermAttribute;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;
import org.apache.lucene.util.Version;
import org.jdesktop.swingx.VerticalLayout;

import at.tuwien.ifs.commons.gui.util.DocumentAdapter;
import at.tuwien.ifs.somtoolbox.apps.viewer.controls.PaletteSelectionPanel;
import at.tuwien.ifs.somtoolbox.data.InputData;
import at.tuwien.ifs.somtoolbox.data.InputDatum;
import at.tuwien.ifs.somtoolbox.data.SOMLibTemplateVector;
import at.tuwien.ifs.somtoolbox.data.SharedSOMVisualisationData;
import at.tuwien.ifs.somtoolbox.util.FileUtils;
import at.tuwien.ifs.somtoolbox.util.GridBagConstraintsIFS;
import at.tuwien.ifs.somtoolbox.util.UiUtils;
import at.tuwien.ifs.somtoolbox.visualization.Palette;
import at.tuwien.ifs.somtoolbox.visualization.Palettes;

/**
 * This class represents the panel for the viewing documents.<br/>
 * It has an EditorPane for viewing HTML documents.
 * 
 * @author Rudolf Mayer
 * @version $Id: DocViewPanel.java 4363 2015-03-11 17:30:06Z mayer $
 */
public class DocViewPanel extends JPanel implements ItemSelectionListener {
    private static final long serialVersionUID = 1L;

    private Log log = LogFactory.getLog(this.getClass());

    private JEditorPane textPane = new JEditorPane();

    private Highlighter highlighter = textPane.getHighlighter();

    private JTextField txtFieldSearch = new JTextField();

    private ArrayList<Object> searchResultHighLights = new ArrayList<Object>();

    private JCheckBox weightHighlightBox = new JCheckBox("Highlight term weights", true);

    private ArrayList<Object> weightingHighLights = new ArrayList<Object>();

    private PaletteSelectionPanel paletteSelectionPanel;

    private String documentPath;

    private String documentSuffix = ".html";

    private SharedSOMVisualisationData inputDataObjects;

    private DefaultHighlighter.DefaultHighlightPainter[] weightPaints;

    private String currentInput;

    public String getDocumentSuffix() {
        return documentSuffix;
    }

    public void setDocumentSuffix(String documentSuffix) {
        this.documentSuffix = documentSuffix;
    }

    public String getDocumentPath() {
        return documentPath;
    }

    public void setDocumentPath(String filePath) {
        if (!filePath.equals("") && new File(filePath).isDirectory() && !filePath.endsWith(File.separator)) {
            filePath += File.separator;
        }
        this.documentPath = filePath;
    }

    /**
     * displays the document with the provided name in the docviewer. If not existent, the text is cleared.<br/>
     * NOTE: we are only caring about the first item, because we can only display one document at a time. We assume that
     * the listselectionode in the panel is set to singleselection
     */
    @Override
    public void itemSelected(Object[] items) {
        if (items == null || items.length == 0) {
            textPane.setText("");
        } else {
            if (documentPath.endsWith(".zip")) {
                // read from a zip file
                try {
                    ZipFile zipFile = new ZipFile(documentPath);
                    ZipEntry entry = zipFile.getEntry(items[0] + documentSuffix);
                    InputStream in = zipFile.getInputStream(entry);
                    textPane.setText(FileUtils.readFromReader(new InputStreamReader(in)));
                    zipFile.close();
                } catch (Exception e) {
                    log.error(e);
                    textPane.setText("Problem loading text from ZIP file: " + e.getMessage());
                    return;
                }
            } else {
                String absoluteDocumentName = documentPath + items[0] + documentSuffix;
                try {
                    textPane.setPage(absoluteDocumentName);
                } catch (IOException e) {
                    try {
                        textPane.setPage("file://" + absoluteDocumentName);
                    } catch (IOException e2) {
                        log.error(e);
                        textPane.setText("Problem loading text: " + e.getMessage());
                        return;
                    }
                }
            }

            updateWeightHighlighting(items);
        }
    }

    public DocViewPanel(SharedSOMVisualisationData inputDataObjects) {
        super(new BorderLayout());
        setPreferredSize(new java.awt.Dimension(216, 260));
        this.inputDataObjects = inputDataObjects;

        // search panel
        JPanel searchPanel = UiUtils.makeBorderedPanel(new GridBagLayout(), "Search");
        GridBagConstraintsIFS cSearch = new GridBagConstraintsIFS(GridBagConstraints.WEST,
                GridBagConstraints.HORIZONTAL);
        txtFieldSearch.getDocument().addDocumentListener(new DocumentAdapter() {
            @Override
            protected void performAction(DocumentEvent e) {
                updateSearchResults();
            }
        });
        JButton btnClearFilter = new JButton("x");
        btnClearFilter.setMargin(new Insets(0, 0, 0, 0));
        btnClearFilter.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                txtFieldSearch.setText("");
            }
        });

        searchPanel.add(txtFieldSearch, cSearch.setWeightX(1.0));
        searchPanel.add(btnClearFilter, cSearch.nextCol().resetWeights());
        // end search panel

        // highlight panel
        JPanel highlightPanel = UiUtils.makeBorderedPanel(new GridBagLayout(), "Weights highlighting");
        GridBagConstraintsIFS cHighLight = new GridBagConstraintsIFS();
        weightHighlightBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateWeightHighlighting();
            }
        });
        weightHighlightBox.setSelected(inputDataObjects.getTemplateVector() != null
                && inputDataObjects.getInputData() != null);

        Palette[] availablePalettes = Palettes.getAvailablePalettes();
        paletteSelectionPanel = new PaletteSelectionPanel(availablePalettes);
        paletteSelectionPanel.setEnabled(weightHighlightBox.isSelected());
        paletteSelectionPanel.addPaletteChangeListener(new PaletteChangeListener() {
            @Override
            public void paletteChanged(Palette palette) {
                updateWeightHighlighting();
            }
        });
        highlightPanel.add(weightHighlightBox, cHighLight);
        highlightPanel.add(paletteSelectionPanel, cHighLight.nextRow());
        // end highlight panel

        JPanel controlPanel = new JPanel(new VerticalLayout());
        controlPanel.add(searchPanel);
        controlPanel.add(highlightPanel);

        textPane.setEditable(false);
        JScrollPane docScroller = new JScrollPane(textPane);
        docScroller.setBorder(BorderFactory.createTitledBorder("Text"));

        add(controlPanel, BorderLayout.NORTH);
        add(docScroller, BorderLayout.CENTER);
    }

    private void removeHighLights(ArrayList<Object> highLights) {
        for (Object tag : highLights) {
            highlighter.removeHighlight(tag);
        }
        highLights.clear();
    }

    private void updateSearchResults() {
        try {
            // remove previous highlighting
            removeHighLights(searchResultHighLights);

            // add new highlighting
            if (StringUtils.isNotEmpty(txtFieldSearch.getText())) {
                Pattern pattern = Pattern.compile(txtFieldSearch.getText(), Pattern.CASE_INSENSITIVE);
                Matcher matcher = pattern.matcher(textPane.getText());

                DefaultHighlighter.DefaultHighlightPainter highlightPaint = new DefaultHighlighter.DefaultHighlightPainter(
                        Color.YELLOW);
                while (matcher.find()) {
                    Object tag = highlighter.addHighlight(matcher.start(), matcher.end(), highlightPaint);
                    searchResultHighLights.add(tag);
                }

            }
        } catch (PatternSyntaxException ex) {
            ex.printStackTrace();
            return;
        } catch (BadLocationException ex) {
            ex.printStackTrace();
        }
    }

    private void updateWeightHighlighting(Object[] items) {
        currentInput = (String) items[0];
        updateWeightHighlighting();
    }

    private void updateWeightHighlighting() {
        // remove previous highlighting
        removeHighLights(weightingHighLights);
        if (weightHighlightBox.isSelected()) {
            if (inputDataObjects.getTemplateVector() == null) {
                Logger.getLogger("at.tuwien.ifs.somtoolbox").severe(
                        "Template vector file needed for displaying weights. Load from the File->Data files menu");
                weightHighlightBox.setSelected(false);
                return;
            }
            if (inputDataObjects.getInputData() == null) {
                Logger.getLogger("at.tuwien.ifs.somtoolbox").severe(
                        "Input data file needed for displaying weights. Load from the File->Data files menu");
                weightHighlightBox.setSelected(false);
                return;
            }

            SOMLibTemplateVector tv = inputDataObjects.getTemplateVector();
            InputData data = inputDataObjects.getInputData();
            InputDatum input = data.getInputDatum(currentInput);

            double maxValue = data.getMaxValue();
            double minValue = data.getMinValue();
            double span = maxValue - minValue;

            // init paints
            Palette p = paletteSelectionPanel.getSelectedPalette();
            int paletteLength = p.getNumberOfColours();
            weightPaints = new DefaultHighlighter.DefaultHighlightPainter[paletteLength];
            for (int i = 0; i < weightPaints.length; i++) {
                weightPaints[i] = new DefaultHighlighter.DefaultHighlightPainter(p.getColor(i));
            }

            String text = textPane.getText();
            StandardAnalyzer analyzer = new StandardAnalyzer(Version.LUCENE_30);
            TokenStream stream = analyzer.tokenStream("contents", new StringReader(text));
            try {
                while (stream.incrementToken()) {
                    TypeAttribute typeAttribute = stream.getAttribute(TypeAttribute.class);
                    if (!at.tuwien.ifs.somtoolbox.util.StringUtils.equalsAny(typeAttribute.type(), "<APOSTROPHE>")) {
                        TermAttribute termAttribute = stream.getAttribute(TermAttribute.class);
                        String term = termAttribute.term();
                        if (tv.containsLabel(term)) {
                            int index = tv.getIndex(term);
                            double value = input.getVector().getQuick(index);
                            int colorIndex = (int) (paletteLength / 4d + relativeValue(minValue, span, value)
                                    * paletteLength / 2d);
                            OffsetAttribute offsetAttribute = stream.getAttribute(OffsetAttribute.class);
                            offsetAttribute.startOffset();
                            Object tag = highlighter.addHighlight(offsetAttribute.startOffset(),
                                    offsetAttribute.endOffset(), weightPaints[colorIndex]);
                            weightingHighLights.add(tag);
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (BadLocationException e) {
                e.printStackTrace();
            }
        }
    }

    private double relativeValue(double minValue, double span, double value) {
        return (value - minValue) / span;
    }
}
