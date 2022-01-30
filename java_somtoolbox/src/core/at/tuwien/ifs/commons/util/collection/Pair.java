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

/**
 * A class that holds two typed objects together; mostly useful to return multiple values from a method, without having
 * to make a dedicated class, or using an untyped collection or array that would require casting the contents in the
 * calling method.
 * 
 * @author Rudolf Mayer
 * @version $Id: Pair.java 4264 2012-04-03 14:50:18Z mayer $
 */
public class Pair<S, T> {
    private S first;

    private T second;

    public Pair(S first, T second) {
        this.first = first;
        this.second = second;
    }

    /**
     * @return Returns the first element.
     */
    public S getFirst() {
        return first;
    }

    /**
     * @return Returns the second element.
     */
    public T getSecond() {
        return second;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Pair) {
            return first.equals(((Pair<?, ?>) obj).first) && second.equals(((Pair<?, ?>) obj).second);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return first.hashCode() + second.hashCode();
    }

    public void setFirst(S first) {
        this.first = first;
    }

    public void setSecond(T second) {
        this.second = second;
    }

}
