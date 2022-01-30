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

import java.util.Collection;
import java.util.Comparator;
import java.util.TreeSet;

/**
 * A subclass of {@link TreeSet} that keeps only the smallest elements, up to a specified number of maximum elements.
 * 
 * @author Rudolf Mayer
 * @version $Id: SmallestElementSet.java 4211 2011-05-26 11:56:56Z mayer $
 */
public class SmallestElementSet<E extends Comparable<E>> extends TreeSet<E> {
    static final long serialVersionUID = 1l;

    private final int maxElementCount;

    /**
     * @param maxElementCount Maximum number of elements the set will keep
     */
    public SmallestElementSet(int maxElementCount) {
        this.maxElementCount = maxElementCount;
    }

    /**
     * @return {@code true} if this set was modified by the operation
     */
    @Override
    public boolean add(E element) {
        if (size() < maxElementCount) { // if we are not yet at the limit, just add
            return super.add(element);
        }
        Comparator<? super E> comparator = comparator();
        E last = last();
        int compareTo;
        if (comparator == null) {
            compareTo = element.compareTo(last);
        } else {
            compareTo = comparator.compare(element, last);
        }
        if (compareTo < 0) { // if this element is smaller than the largest one
            remove(last); // remove the last
            return super.add(element); // and add this; sorting will be ensured by TreeSet
        } else { // otherwise, no changes done
            return false;
        }
    }

    @Override
    public boolean addAll(Collection<? extends E> collection) {
        boolean changed = false;
        for (E element : collection) {
            changed = changed || add(element);
        }
        return changed;
    }

}
