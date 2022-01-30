/*
 * Copyright 2004-2010 Information & Software Engineering Group (188/1)
 *                     Institute of Software Technology and Interactive Systems
 *                     Vienna University of Technology, Austria
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
package at.tuwien.ifs.somtoolbox.properties;

import java.io.FileInputStream;
import java.util.logging.Logger;

import com.martiansoftware.jsap.FlaggedOption;

import at.tuwien.ifs.somtoolbox.layers.quality.QuantizationError;

/**
 * Properties for GHSOM training.
 * 
 * @author Michael Dittenbach
 * @version $Id: GHSOMProperties.java 4303 2014-01-09 12:15:51Z mayer $
 */
public class GHSOMProperties extends SOMProperties {
    private static final long serialVersionUID = 1L;

    private boolean reIndex = false;

    public static final FlaggedOption OPTION_TAU2 = PropertyUtils.getDoubleOption("tau2", 1, false,
            "the tau2 parameter for the GHSOM");

    public static final FlaggedOption OPTION_EXPAND_QUALITY_MEASURE_NAME = PropertyUtils.getStringOption(
            "expandQualityMeasureName", QuantizationError.class.getName() + ".qe", false, "TODO");

    public static final FlaggedOption[] GHSOM_OPTIONS = { OPTION_TAU2, OPTION_EXPAND_QUALITY_MEASURE_NAME };

    private double tau2 = 1;

    private String expandQualityMeasureName = null;

    /**
     * Loads and encapsulated properties for the GHSOM training process.
     * 
     * @param fname Name of the properties file.
     */
    public GHSOMProperties(String fname) throws PropertiesException {
        super(fname);
        try {
            load(new FileInputStream(fname));
        } catch (Exception e) {
            throw new PropertiesException("Could not open properties file " + fname);
        }
        try {
            tau2 = Double.parseDouble(getProperty(OPTION_TAU2.getID(), OPTION_TAU2.getDefault()[0]));
            if (tau2 == 1) {
                Logger.getLogger("at.tuwien.ifs.somtoolbox").info(
                        OPTION_TAU2.getID() + " = 1 implies single flat layer");
            } else if (tau2 <= 0 || tau2 > 1) {
                throw new PropertiesException(OPTION_TAU2.getID() + " less than or equal zero or greater than 1.");
            }
            expandQualityMeasureName = getProperty(OPTION_EXPAND_QUALITY_MEASURE_NAME.getID());
            if (expandQualityMeasureName == null) {
                expandQualityMeasureName = OPTION_EXPAND_QUALITY_MEASURE_NAME.getDefault()[0];
                Logger.getLogger("at.tuwien.ifs.somtoolbox").warning(
                        "No " + OPTION_EXPAND_QUALITY_MEASURE_NAME.getID() + " given. Defaulting to "
                                + expandQualityMeasureName + ".");
            } else {
                expandQualityMeasureName = SOMProperties.QUALITY_PACKAGE + expandQualityMeasureName;
            }

            reIndex = Boolean.valueOf(getProperty("reIndex", "false")).booleanValue();
        } catch (NumberFormatException e) {
            throw new PropertiesException("Illegal numeric value in properties file.");
        }

    }

    /**
     * Not used at the moment.
     * 
     * @return Returns the reIndex.
     */
    public boolean reIndex() {
        return reIndex;
    }

    /**
     * Returns tau2 determining the maximum data representation granularity.
     * 
     * @return tau2 determining the maximum data representation granularity.
     */
    public double tau2() {
        return tau2;
    }

    /**
     * Returns the name of the used quality measure.
     * 
     * @return the name of the used quality measure.
     */
    public String expandQualityMeasureName() {
        return expandQualityMeasureName;
    }

}
