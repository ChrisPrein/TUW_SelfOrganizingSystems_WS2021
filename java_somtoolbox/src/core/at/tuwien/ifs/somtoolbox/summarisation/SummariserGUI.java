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
package at.tuwien.ifs.somtoolbox.summarisation;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.jfree.ui.tabbedui.VerticalLayout;

import at.tuwien.ifs.somtoolbox.apps.viewer.CommonSOMViewerStateData;
import at.tuwien.ifs.somtoolbox.summarisation.gui.NavigationPanel;
import at.tuwien.ifs.somtoolbox.visualization.ColorGradientFactory;
import at.tuwien.ifs.somtoolbox.visualization.Palette;

/**
 * @author Julius Penaranda
 * @author Rudolf Mayer
 * @version $Id: SummariserGUI.java 4131 2011-02-08 12:53:57Z mayer $
 */
public class SummariserGUI extends JDialog implements ActionListener {
    private static final long serialVersionUID = 1L;

    public static final Palette[] palettes = {
            new Palette("Grayscale 9 Colours", "Grayscale9", ColorGradientFactory.GrayscaleGradient(), 9),
            new Palette("RGB 9 Colours", "RGB9", ColorGradientFactory.RGBGradient(), 9),
            new Palette("Cartography 9 Colours", "Cartography9",
                    ColorGradientFactory.CartographyColorGradientLessWater(), 9),
            new Palette("CartographyMountain 9 Colours", "CartographyMountain9",
                    ColorGradientFactory.CartographyMountain2dGradient(), 9) };

    public JScrollPane scrollP = new JScrollPane();

    public SummariserGUI(JFrame parent, CommonSOMViewerStateData state, Object[] itemName) {
        super(parent);
        setSize(new Dimension(850, 690));
        setTitle("Summarizer");

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(scrollP, BorderLayout.CENTER);

        JPanel leftPanel = new JPanel(new VerticalLayout());
        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(this);

        leftPanel.add(new NavigationPanel(this, state, itemName));
        leftPanel.add(closeButton);
        getContentPane().add(leftPanel, BorderLayout.WEST);

        setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        setVisible(false);
        dispose();
    }

}
