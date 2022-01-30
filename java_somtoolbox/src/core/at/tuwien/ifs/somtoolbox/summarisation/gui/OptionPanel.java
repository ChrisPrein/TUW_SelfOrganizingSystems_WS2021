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
package at.tuwien.ifs.somtoolbox.summarisation.gui;

import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBox;

import at.tuwien.ifs.commons.gui.controls.TitledCollapsiblePanel;
import at.tuwien.ifs.somtoolbox.util.GridBagConstraintsIFS;

/**
 * @author Julius Penaranda
 * @author Rudolf Mayer
 * @version $Id: OptionPanel.java 4125 2011-02-08 10:26:22Z mayer $
 */
public class OptionPanel extends TitledCollapsiblePanel {
    private static final long serialVersionUID = 1L;

    private NavigationPanel navP = null;

    public JCheckBox scoreCB = new JCheckBox();

    public JCheckBox highlightCB = new JCheckBox();

    public JCheckBox filenameCB = new JCheckBox();

    public JCheckBox wordCB = new JCheckBox();

    public OptionPanel(NavigationPanel nav) {
        super("Options", new GridBagLayout());
        this.navP = nav;

        scoreCB.setText("scores");
        scoreCB.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                navP.updateResults();
            }
        });
        highlightCB.setText("highlight");
        highlightCB.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                if (highlightCB.isSelected()) {
                    wordCB.setSelected(false);
                }
                navP.updateResults();
            }
        });
        filenameCB.setText("Filename");
        filenameCB.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                navP.updateResults();
            }
        });
        wordCB.setText("highl word");
        wordCB.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                if (wordCB.isSelected()) {
                    highlightCB.setSelected(false);
                }
                navP.updateResults();
            }
        });

        GridBagConstraintsIFS gc = new GridBagConstraintsIFS().setInsets(new Insets(5, 10, 5, 10));

        add(scoreCB, gc);
        add(highlightCB, gc.nextCol());
        if (this.navP.itemNames.length > 1) {
            add(filenameCB, gc.nextRow());
            add(wordCB, gc.nextCol());
        } else {
            add(wordCB, gc.nextRow());
        }
    }

}
