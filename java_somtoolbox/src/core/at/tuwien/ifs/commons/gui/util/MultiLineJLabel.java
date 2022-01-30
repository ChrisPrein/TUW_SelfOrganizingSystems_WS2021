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
package at.tuwien.ifs.commons.gui.util;

import java.awt.TextArea;

import javax.swing.JLabel;
import javax.swing.JTextArea;

/**
 * Simulates a multi-line {@link JLabel} by a {@link TextArea} that is not focusable, and has no border.
 * 
 * @author Rudolf Mayer
 * @version $Id: MultiLineJLabel.java 4158 2011-02-11 15:57:46Z mayer $
 */
public class MultiLineJLabel extends JTextArea {

    private static final long serialVersionUID = 1L;

    public MultiLineJLabel(String text) {
        super(text);
        setOpaque(false);
        setBorder(null);
        setFocusable(false);
    }
}