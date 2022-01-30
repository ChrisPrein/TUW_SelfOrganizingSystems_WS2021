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
package at.tuwien.ifs.commons.util.collection;

import java.util.Comparator;
import java.util.Map.Entry;

/**
 * A comparator that sorts {@link Entry} by their values.
 * 
 * @author Rudolf Mayer
 * @version $Id: $
 */
public class EntryValueComparator<K, V extends Comparable<V>> implements Comparator<Entry<K, V>> {

    private boolean descending;

    public EntryValueComparator() {
        this(false);
    }

    public EntryValueComparator(boolean descending) {
        this.descending = descending;
    }

    public int compare(Entry<K, V> e1, Entry<K, V> e2) {
        return descending ? e2.getValue().compareTo(e1.getValue()) : e1.getValue().compareTo(e2.getValue());
    }

}
