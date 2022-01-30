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
 * A class that holds three typed objects together, similar to the {@link Pair}.
 * 
 * @author Rudolf Mayer
 * @version $Id: Triple.java 4263 2012-04-02 11:45:05Z mayer $
 */
public class Triple<S, T, U> extends Pair<S, T> {

    private U third;

    public Triple(S first, T second, U third) {
        super(first, second);
        this.third = third;
    }

    /**
     * @return Returns the third element.
     */
    public U getThird() {
        return third;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Triple) {
            return super.equals(obj) && third.equals(((Triple<?, ?, ?>) obj).third);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return super.hashCode() + third.hashCode();
    }

    public void setThird(U third) {
        this.third = third;
    }
}
