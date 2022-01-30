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

import java.awt.Dimension;
import java.io.File;
import java.net.URL;

import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JScrollPane;

/**
 * @author Rudolf Mayer
 * @version $Id: $
 */
public class HelpDialog extends JDialog {

    private static final long serialVersionUID = 1L;

    public HelpDialog(SOMViewer somViewer) {
        super(somViewer);
        setTitle("Java SOMToolbox Help");
        JEditorPane helpDisplay = new JEditorPane();
        JScrollPane scrollPane = new JScrollPane(helpDisplay);
        add(scrollPane);
        try {
            helpDisplay.setPage(new URL("file:///usr/share/somtoolbox/doc/index.html"));
        } catch (Exception e) {
            helpDisplay.setText("Could not load help index: " + e.getMessage());
        }
        pack();
    }

    /** set a frame location on the right edge of the owner frame */
    private void alignRight() {
        setLocation(getOwner().getSize().width + getOwner().getLocationOnScreen().x - getWidth(),
                getOwner().getLocationOnScreen().y);
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(600, getOwner().getHeight() - 10);
    }

    @Override
    public Dimension getMinimumSize() {
        return new Dimension(400, getOwner().getHeight() - 10);
    }

    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);
        if (visible) {
            alignRight();
        }
    }

}
