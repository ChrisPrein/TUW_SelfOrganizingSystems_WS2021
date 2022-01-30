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

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JTextField;

import at.tuwien.ifs.commons.gui.controls.TitledCollapsiblePanel;
import at.tuwien.ifs.somtoolbox.util.GridBagConstraintsIFS;

/**
 * @author Julius Penaranda
 * @author Rudolf Mayer
 * @version $Id: SearchPanel.java 4125 2011-02-08 10:26:22Z mayer $
 */
public class SearchPanel extends TitledCollapsiblePanel {
    private static final long serialVersionUID = 1L;

    private NavigationPanel navP = null;

    public SearchPanel(NavigationPanel nav) {
        super("Search", new GridBagLayout());
        this.navP = nav;
        setBorder(BorderFactory.createEtchedBorder());
        JTextField searchField = new JTextField("", 12);
        searchField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                navP.searchActionPerformed(e);
            }
        });

        setLayout(new GridBagLayout());
        GridBagConstraintsIFS gc = new GridBagConstraintsIFS().setInsets(new Insets(5, 10, 5, 10));

        add(new JLabel("Search word"), gc);
        add(searchField, gc.nextCol());
    }

}
