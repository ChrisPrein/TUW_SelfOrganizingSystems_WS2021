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
package at.tuwien.ifs.somtoolbox.apps.viewer.controls;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.logging.Logger;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.ListSelectionModel;

import at.tuwien.ifs.somtoolbox.SOMToolboxException;
import at.tuwien.ifs.somtoolbox.apps.viewer.CommonSOMViewerStateData;
import at.tuwien.ifs.somtoolbox.apps.viewer.DocViewPanel;
import at.tuwien.ifs.somtoolbox.data.SOMVisualisationData;
import at.tuwien.ifs.somtoolbox.summarisation.SummariserGUI;

/**
 * This class provides the link to the {@link DocViewPanel}
 * 
 * @author Christoph Becker
 * @author Rudolf Mayer
 * @see at.tuwien.ifs.somtoolbox.apps.viewer.controls.AbstractSelectionPanel
 * @version $Id: DocSOMPanel.java 4123 2011-02-06 16:47:41Z mayer $
 */
public class DocSOMPanel extends AbstractSelectionPanel {
    private static final long serialVersionUID = 1L;

    /**
     * creates a new DocSOMPanel with the provided state, containing a simple list, nothing more, and inits the
     * selection listener
     */
    public DocSOMPanel(CommonSOMViewerStateData state) {
        super(new BorderLayout(), state, "DocSOM Control", 1);
        playlists[0].setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        initGui();
        setVisible(true);
    }

    private void initGui() {
        addSingleListScrollPanel(BorderLayout.CENTER);

        // Summarise button
        JButton buttonSummarise = new JButton("Summarise");
        buttonSummarise.setMargin(SMALL_INSETS);
        buttonSummarise.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                // require input vector file
                if (state.inputDataObjects.getInputData() == null) { // not loaded
                    SOMVisualisationData inputObject = state.inputDataObjects.getObject(SOMVisualisationData.INPUT_VECTOR);
                    try { // try to load it
                        inputObject.loadFromFile(state.fileChooser, state.parentFrame);
                    } catch (SOMToolboxException exception) {
                        Logger.getLogger("at.tuwien.ifs.somtoolbox").severe(exception.getMessage());
                    }
                    if (state.inputDataObjects.getInputData() == null) { // not loaded
                        Logger.getLogger("at.tuwien.ifs.somtoolbox").severe(
                                "Input vector file needed for summarisation!");
                        return;
                    }
                }

                Object[] items;

                if (playlists[0].isSelectionEmpty()) {
                    items = new Object[playlists[0].getModel().getSize()];
                    for (int i = 0; i < playlists[0].getModel().getSize(); i++) {
                        items[i] = playlists[0].getModel().getElementAt(i);
                    }
                } else {
                    items = playlists[0].getSelectedValues();
                }
                JFrame parent = state.parentFrame;
                new SummariserGUI(parent, state, items);

            }
        });
        add(buttonSummarise, BorderLayout.SOUTH);
        // END: Summarise
    }
}
