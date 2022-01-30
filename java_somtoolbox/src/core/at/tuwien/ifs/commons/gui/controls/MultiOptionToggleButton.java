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
package at.tuwien.ifs.commons.gui.controls;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JToggleButton;

import org.apache.commons.lang.ArrayUtils;

/**
 * This class provides a control similar to a {@link JToggleButton}, but with multiple options popping out. It is
 * different from a {@link JComboBox}, as it provides a nicer visual appearance.
 * 
 * @author Rudolf Mayer
 * @author Jakob Frank
 * @version $Id: MultiOptionToggleButton.java 4179 2011-02-18 12:44:39Z mayer $
 */
public class MultiOptionToggleButton extends JButton {
    private JPopupMenu menu = new JPopupMenu();

    private static final long serialVersionUID = 1L;

    private int selectedIndex = 0;

    public MultiOptionToggleButton(final ImageIcon[] icons, final String[] buttonTexts, final String tooltip,
            final MultiOptionToggleListener listener) {
        super(icons[0]);
        this.setToolTipText(tooltip);
        this.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                menu.show(MultiOptionToggleButton.this, 0, MultiOptionToggleButton.this.getHeight());

                // highlight current selection
                final Component[] components = menu.getComponents();
                for (Component c : components) {
                    c.setBackground(null);
                }
                menu.getComponent(selectedIndex).setBackground(Color.GRAY);
            }
        });

        for (int i = 0; i < buttonTexts.length; i++) {
            JMenuItem jMenuItem = new JMenuItem(buttonTexts[i], icons[i]);
            jMenuItem.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    listener.performAction(e.getActionCommand());
                    selectedIndex = ArrayUtils.indexOf(buttonTexts, e.getActionCommand());
                    setIcon(icons[selectedIndex]);
                }
            });
            menu.add(jMenuItem);
        }

        JMenuBar menuBar = new JMenuBar();
        menuBar.setBorderPainted(false);
        menuBar.add(menu);
    }

    public JPopupMenu getMenu() {
        return menu;
    }

}
