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
package at.tuwien.ifs.somtoolbox.output.labeling;

import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * Contains all UnitWords for the map and can be used to calculate goodnesses of words and select the best words from an
 * unit.
 * 
 * @author Hauke Schuldt
 * @author Alois Wollersberger
 * @version $Id: UnitWordsMap.java 4185 2011-03-01 14:18:52Z mayer $
 */
public class UnitWordsMap {

    private UnitWords[][][] unitWords;

    public int A0_RADIUS = 1;

    public int A1_RADIUS = 2;

    /**
     * Generates a new empty UnitWordsMap with the specified dimensions
     * 
     * @param x the size of the x-axis
     * @param y the size of the y-axis
     * @param z the size of the z-axis
     */
    public UnitWordsMap(int x, int y, int z) {
        super();
        unitWords = new UnitWords[x][y][z];
    }

    /**
     * Sets a UnitWords to the specified position
     * 
     * @param unitWords the UnitWords to set
     * @param x the position on the x-axis
     * @param y the position on the y-axis
     * @param z the position on the z-axis
     */
    public void setUnitWords(UnitWords unitWords, int x, int y, int z) {
        this.unitWords[x][y][z] = unitWords;
    }

    /**
     * Returns the UnitWords at the specified position
     * 
     * @param x the position on the x-axis
     * @param y the position on the y-axis
     * @param z the position on the z-axis
     * @return the UnitWords at the specified position
     */
    public UnitWords getUnitWords(int x, int y, int z) {
        return unitWords[x][y][z];
    }

    /**
     * Creates and returns a new, empty UnitWords at the specified position
     * 
     * @param x the position on the x-axis
     * @param y the position on the y-axis
     * @param z the position on the z-axis *
     * @return a newly created UnitWords which contains no words yet.
     */
    public UnitWords newUnitWords(int x, int y, int z) {
        this.unitWords[x][y][z] = new UnitWords(x, y, z);
        return unitWords[x][y][z];
    }

    /**
     * Returns for a UnitWords at a specified postion all UnitWords which are outside its radius
     * 
     * @param x the position of the unit on the x-axis
     * @param y the position of the unit on the y-axis
     * @param z the position of the unit on the z-axis
     * @param radius the distance the units have to be away
     * @return all UnitWords which are outside the radius
     */
    public List<UnitWords> getUnitWordsOutsideRadius(int x, int y, int z, int radius) {
        LinkedList<UnitWords> outside = new LinkedList<UnitWords>();
        int distance = 0;
        for (int ux = 0; ux < unitWords.length; ux++) {
            for (int uy = 0; uy < unitWords[ux].length; uy++) {
                for (int uz = 0; uz < unitWords[ux][uy].length; uz++) {
                    if (unitWords[ux][uy][uz] != null) {
                        distance = 0;
                        distance += Math.abs(x - ux);
                        distance += Math.abs(y - uy);
                        distance += Math.abs(z - uz);
                        if (distance > radius) {
                            outside.add(unitWords[ux][uy][uz]);
                        }
                    }
                }
            }
        }
        return outside;
    }

    /**
     * Returns for a UnitWords at a specified postion all UnitWords which are inside its radius
     * 
     * @param x the position of the unit on the x-axis
     * @param y the position of the unit on the y-axis
     * @param z the position of the unit on the z-axis
     * @param radius the distance in which the units have to be
     * @return all UnitWords which are inside the radius
     */
    public List<UnitWords> getUnitWordsInsideRadius(int x, int y, int z, int radius) {
        LinkedList<UnitWords> outside = new LinkedList<UnitWords>();
        int distance = 0;
        for (int ux = 0; ux < unitWords.length; ux++) {
            for (int uy = 0; uy < unitWords[ux].length; uy++) {
                for (int uz = 0; uz < unitWords[ux][uy].length; uz++) {
                    if (unitWords[ux][uy][uz] != null) {
                        distance = 0;
                        distance += Math.abs(x - ux);
                        distance += Math.abs(y - uy);
                        distance += Math.abs(z - uz);
                        if (distance <= radius) {
                            outside.add(unitWords[ux][uy][uz]);
                        }
                    }
                }
            }
        }
        return outside;
    }

    /**
     * Calculates goodness G1 for map units
     * 
     * @param x the position of the unit on the x-axis
     * @param y the position of the unit on the y-axis
     * @param z the position of the unit on the z-axis
     * @param word the word to process
     * @return the goodness of the word for this unit
     */
    public double calcGoodness(int x, int y, int z, String word) {

        UnitWords unit = this.getUnitWords(x, y, z);
        double f = unit.getRelativeFrequency(word);
        double sum = 0;
        for (UnitWords element : this.getUnitWordsOutsideRadius(x, y, z, A1_RADIUS)) {
            sum = sum + element.getRelativeFrequency(word);
        }
        double ret = f * f / (f + sum);

        return ret;
    }

    /**
     * get number of count Words in hashtable with G1 goodness value
     * 
     * @param x the position of the unit on the x-axis
     * @param y the position of the unit on the y-axis
     * @param z the position of the unit on the z-axis
     * @param num the number how many best words to return
     * @return a sorted hashtable with the best words
     */
    public Hashtable<String, Double> getBestWords(int x, int y, int z, int num) {
        Hashtable<String, Double> goodness = new Hashtable<String, Double>();
        UnitWords self = this.getUnitWords(x, y, z);
        if (self == null) {
            return null;
        }

        Set<String> words = self.getWordSet();

        LinkedList<Double> bestValue = new LinkedList<Double>();
        LinkedList<String> bestName = new LinkedList<String>();

        for (String word : words) {
            double curr = this.calcGoodness(x, y, z, word);
            boolean inserted = false;
            if (bestValue.size() == 0) {
                bestValue.add(curr);
                bestName.add(word);
                inserted = true;
            }
            if (!inserted) {
                for (int i = 0; i < bestValue.size(); i++) {
                    if (curr > bestValue.get(i)) {
                        bestValue.add(i, curr);
                        bestName.add(i, word);
                        if (bestValue.size() > num) {
                            bestValue.removeLast();
                            bestName.removeLast();
                        }
                        break;
                    }
                }
            }
            if (!inserted) {
                if (bestValue.size() < num) {
                    bestValue.add(curr);
                    bestName.add(word);
                }
            }
        }
        for (int i = 0; i < num; i++) {
            goodness.put(bestName.pop(), bestValue.pop());
        }

        return goodness;
    }

    /**
     * Calculates goodness G1 for map areas
     * 
     * @param x the position of the unit on the x-axis
     * @param y the position of the unit on the y-axis
     * @param z the position of the unit on the z-axis
     * @param word the word to process
     * @return the goodness of the word for this unit
     */
    public double mapAreaGoodness(int x, int y, int z, String word) {
        double f = 0;
        for (UnitWords element : this.getUnitWordsInsideRadius(x, y, z, A0_RADIUS)) {
            f = f + element.getRelativeFrequency(word);
        }
        double sum = 0;
        for (UnitWords element : this.getUnitWordsOutsideRadius(x, y, z, A1_RADIUS)) {
            sum = sum + element.getRelativeFrequency(word);
        }
        return f * f / (f + sum);
    }

    /**
     * Choose word from hashmap based on G2 Goodness
     * 
     * @param x the position of the unit on the x-axis
     * @param y the position of the unit on the y-axis
     * @param z the position of the unit on the z-axis
     * @param top top
     * @return the best word for the map area. It is returned in an hashmap with size = 1.
     */
    public Hashtable<String, Double> chooseBestWord(int x, int y, int z, Hashtable<String, Double> top) {
        LinkedList<String> ranking = new LinkedList<String>();
        LinkedList<Double> value = new LinkedList<Double>();
        if (top == null) {
            return null;
        }
        for (String word : top.keySet()) {
            double curr = this.mapAreaGoodness(x, y, z, word);
            for (int i = 0; i < top.keySet().size(); i++) {
                if (i == value.size() || value.get(i) < curr) {
                    value.add(i, curr);
                    ranking.add(i, word);
                    break;
                }
            }
        }
        Hashtable<String, Double> ret = new Hashtable<String, Double>();
        ret.put(ranking.getFirst(), value.getFirst());
        return ret;
    }
}
