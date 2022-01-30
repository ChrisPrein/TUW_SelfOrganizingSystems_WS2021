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
 * @author Rudolf Mayer
 * @version $Id: Quadruple.java 4265 2012-04-03 14:50:30Z mayer $
 */
public class Quadruple<S, T, U, V> extends Triple<S, T, U> {

    protected V fourth;

    public Quadruple(S first, T second, U third, V fourth) {
        super(first, second, third);
        this.fourth = fourth;
    }

    /**
     * @return Returns the fourth element
     */
    public V getFourth() {
        return fourth;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Quadruple) {
            return super.equals(obj) && fourth.equals(((Quadruple<?, ?, ?, ?>) obj).fourth);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return super.hashCode() + fourth.hashCode();
    }

    public void setFourth(V fourth) {
        this.fourth = fourth;
    }

}
