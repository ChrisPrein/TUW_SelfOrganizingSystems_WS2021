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

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Set;

/**
 * This class is used for saving the word frequencies of word cocurrences. It also calculates the words' relative
 * frequency
 * 
 * @author Hauke Schuldt
 * @author Alois Wollersberger
 * @version $Id: UnitWords.java 4185 2011-03-01 14:18:52Z mayer $
 */
public class UnitWords {

    private int x;

    private int y;

    private int z;

    private double numberOfWords;

    private boolean recalcNecessary;

    private Hashtable<String, Double> wordFreq;

    /**
     * @param x Position of the unit on the x-axis
     * @param y Position of the unit on the y-axis
     * @param z Position of the unit on the z-axis
     */
    public UnitWords(int x, int y, int z) {
        super();
        this.x = x;
        this.y = y;
        this.z = z;
        numberOfWords = 0;
        recalcNecessary = false;
        wordFreq = new Hashtable<String, Double>();
    }

    /**
     * @param x The x to set.
     */
    public void setX(int x) {
        this.x = x;
    }

    /**
     * @return Returns the x.
     */
    public int getX() {
        return x;
    }

    /**
     * @param y The y to set.
     */
    public void setY(int y) {
        this.y = y;
    }

    /**
     * @return Returns the y.
     */
    public int getY() {
        return y;
    }

    /**
     * @param z The z to set.
     */
    public void setZ(int z) {
        this.z = z;
    }

    /**
     * @return Returns the z.
     */
    public int getZ() {
        return z;
    }

    /**
     * @param numberOfWords The numberOfWords to set.
     */
    public void setNumberOfWords(double numberOfWords) {
        this.numberOfWords = numberOfWords;
    }

    /**
     * @return Returns the numberOfWords.
     */
    public double getNumberOfWords() {
        return numberOfWords;
    }

    /**
     * Calculates the relative frequency of a word in a unit
     * 
     * @return Returns the relative frequency of the word
     */
    public double getAbsoluteFrequency(String word) {
        double ret = 0;

        if (wordFreq.keySet().contains(word)) {
            // System.out.println("true");
            ret = wordFreq.get(word);
        }

        return ret;
    }

    /**
     * Calculates the relative frequency of a word in a unit
     * 
     * @return Returns the relative frequency of the word
     */
    public double getRelativeFrequency(String word) {
        int ret = 0;
        if (recalcNecessary) {
            recalcNumberOfWords();
        }

        if (numberOfWords != 0) {
            double freq = getAbsoluteFrequency(word);
            return freq / numberOfWords;
        }
        return ret;
    }

    /**
     * Adds a word with frequency=1
     * 
     * @param word the word to add
     * @see at.tuwien.ifs.somtoolbox.output.labeling.UnitWords#addWord(String word, double frequency)
     */
    public void addWord(String word) {
        addWord(word, 1);
    }

    /**
     * Adds a word with its frequency. The frequency will be added to the existing frequency if the word already exists.
     * 
     * @param word the word to add
     * @param frequency of the occurrence of the word
     */
    public void addWord(String word, double frequency) {
        recalcNecessary = true;
        double freq = frequency;
        if (wordFreq.keySet().contains(word)) {
            freq += wordFreq.get(word);
        }
        wordFreq.put(word, freq);
    }

    /**
     * Calculates the total number of words
     */
    private void recalcNumberOfWords() {
        numberOfWords = 0;
        Enumeration<Double> elements = wordFreq.elements();
        while (elements.hasMoreElements()) {
            numberOfWords += elements.nextElement();
        }
        recalcNecessary = false;
    }

    /**
     * Returns a Set which contains all words of this unit
     * 
     * @return the set of words
     */
    public Set<String> getWordSet() {
        return this.wordFreq.keySet();
    }

}
