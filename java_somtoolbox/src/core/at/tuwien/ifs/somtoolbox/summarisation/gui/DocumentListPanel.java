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

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import at.tuwien.ifs.commons.gui.controls.TitledCollapsiblePanel;
import at.tuwien.ifs.somtoolbox.util.GridBagConstraintsIFS;

/**
 * @author Julius Penaranda
 * @author Rudolf Mayer
 * @version $Id: DocumentListPanel.java 4355 2015-03-10 16:47:40Z mayer $
 */
public class DocumentListPanel extends TitledCollapsiblePanel {
    private static final long serialVersionUID = 1L;

    private NavigationPanel navP = null;

    private JLabel numbSelected = new JLabel();

    private JLabel numbSentence = new JLabel();

    public DocumentListPanel(NavigationPanel nav) {
        super("Documents", new GridBagLayout());
        this.navP = nav;

        JList<String> doclist = new JList<String>(navP.getData());
        doclist.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                JList list = (JList) e.getSource();
                int index = ((String) list.getSelectedValue()).indexOf(" ", 0);
                String name = ((String) list.getSelectedValue()).substring(0, index);
                navP.selectedDocument = navP.getIndexofFileName(name);
                updateSentenceInfo(list.getSelectedIndices());
            }
        });
        updateSentenceInfo(doclist.getSelectedIndices());

        JScrollPane scrollP = new JScrollPane(doclist);

        GridBagConstraintsIFS gc = new GridBagConstraintsIFS().setInsets(new Insets(5, 10, 5, 10));

        add(scrollP, gc.setGridHeight(2));
        add(numbSelected, gc.nextCol().setGridHeight(1));
        add(numbSentence, gc.nextRow().nextCol());
    }

    private void updateSentenceInfo(int[] indices) {
        numbSelected.setText("# sel: " + indices.length);
        int number = 0;
        for (int indice : indices) {
            number += this.navP.getNumbOfSent(indice);
        }
        numbSentence.setText("# sent:" + number);
    }

}
