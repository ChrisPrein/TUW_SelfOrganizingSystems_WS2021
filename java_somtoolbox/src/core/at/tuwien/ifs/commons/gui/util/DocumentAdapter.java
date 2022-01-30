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

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 * An abstract adapter class for receiving document events; this class exists as convenience for creating listener
 * objects that react in the same way to insert and remove updates.<br/>
 * Therefore, both {@link #insertUpdate(DocumentEvent)} and {@link #removeUpdate(DocumentEvent)} call
 * {@link #performAction(DocumentEvent)}, {@link #changedUpdate(DocumentEvent)} is empty.
 * 
 * @author Rudolf Mayer
 * @version $Id: $
 */
public abstract class DocumentAdapter implements DocumentListener {

    protected abstract void performAction(DocumentEvent e);

    @Override
    public void changedUpdate(DocumentEvent e) {
    }

    @Override
    public void insertUpdate(DocumentEvent e) {
        performAction(e);
    }

    @Override
    public void removeUpdate(DocumentEvent e) {
        performAction(e);
    }

}
