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

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPanel;

import at.tuwien.ifs.somtoolbox.apps.viewer.PaletteChangeListener;
import at.tuwien.ifs.somtoolbox.util.GridBagConstraintsIFS;
import at.tuwien.ifs.somtoolbox.visualization.Palette;

/**
 * Provides a {@link JComboBox} and a {@link PaletteDisplayer} to allow selecting a {@link Palette} and previewing it.
 * Upon palette selection changes, registered {@link PaletteChangeListener} will be notified.
 * 
 * @author Rudolf Mayer
 * @version $Id: PaletteSelectionPanel.java 4353 2015-03-10 16:33:57Z mayer $
 */
public class PaletteSelectionPanel extends JPanel {
    private static final long serialVersionUID = 1L;

    private Vector<PaletteChangeListener> paletteChangeListeners = new Vector<PaletteChangeListener>();

    private final Palette[] availablePalettes;

    private PaletteDisplayer paletteDisplayer = null;

    final JComboBox<String> paletteSelectionBox;

    public PaletteSelectionPanel(Palette... palettes) {
        super(new GridBagLayout());
        this.availablePalettes = palettes;
        String[] paletteName = new String[palettes.length];
        for (int i = 0; i < palettes.length; i++) {
            paletteName[i] = palettes[i].getName();
        }
        paletteSelectionBox = new JComboBox<String>(paletteName);
        paletteSelectionBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Palette palette = getSelectedPalette();
                paletteDisplayer.setPalette(palette);
                for (PaletteChangeListener listener : paletteChangeListeners) {
                    listener.paletteChanged(palette);
                }
            }
        });

        paletteDisplayer = new PaletteDisplayer(palettes[0]);

        GridBagConstraintsIFS gc = new GridBagConstraintsIFS(GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL).setWeightX(1.0);
        add(paletteSelectionBox, gc);
        add(paletteDisplayer, gc.nextRow());
    }

    public void addPaletteChangeListener(PaletteChangeListener l) {
        paletteChangeListeners.add(l);
    }

    public void removePaletteChangeListener(PaletteChangeListener l) {
        paletteChangeListeners.remove(l);
    }

    public int getSelectedPaletteIndex() {
        int paletteIndex = -1;
        for (int i = 0; i < availablePalettes.length; i++) {
            if (availablePalettes[i].getName().equals(paletteSelectionBox.getSelectedItem())) {
                paletteIndex = i;
                break;
            }
        }
        return paletteIndex;
    }

    public Palette getSelectedPalette() {
        return availablePalettes[getSelectedPaletteIndex()];
    }

    /** This method also calls {@link JComponent#setEnabled(boolean)} for all its components. */
    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        for (Component c : getComponents()) {
            c.setEnabled(enabled);
        }
    }

}
