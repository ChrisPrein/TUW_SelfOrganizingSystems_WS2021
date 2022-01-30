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

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Rudolf Mayer
 * @version $Id: MapOfLists.java 4277 2012-04-27 14:27:41Z mayer $
 */
public class MapOfLists<E, T> extends AbstractMap<E, List<T>> {

    private Map<E, List<T>> map;

    public MapOfLists(Map<E, List<T>> map) {
        this.map = map;
    }

    @Override
    public Set<java.util.Map.Entry<E, List<T>>> entrySet() {
        return map.entrySet();
    }

    @Override
    public List<T> put(E key, java.util.List<T> value) {
        return map.put(key, value);
    }

    public List<T> add(E key, T singleValue) {
        List<T> list = map.get(key);
        if (list == null) {
            list = new ArrayList<T>();
            map.put(key, list);
        }
        list.add(singleValue);
        return list;
    }

}
