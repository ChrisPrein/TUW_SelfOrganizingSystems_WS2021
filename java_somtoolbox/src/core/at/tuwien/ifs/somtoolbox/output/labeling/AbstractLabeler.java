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
package at.tuwien.ifs.somtoolbox.output.labeling;

import java.util.logging.Logger;

import at.tuwien.ifs.somtoolbox.data.InputData;

/**
 * This class provides basic functionality all Labelers can use. Classes providing labelling algortihm implementations
 * are advised to extend this class.
 * 
 * @author Michael Dittenbach
 * @version $Id: AbstractLabeler.java 4184 2011-03-01 14:17:39Z mayer $
 */
public abstract class AbstractLabeler implements Labeler {

    public static Labeler instantiate(String lName) throws ClassNotFoundException, InstantiationException,
            IllegalAccessException {
        if (!lName.startsWith("at.tuwien.ifs.somtoolbox.output.labeling.")) {
            lName = "at.tuwien.ifs.somtoolbox.output.labeling." + lName;
        }
        Labeler labeler = null;
        labeler = (Labeler) Class.forName(lName).newInstance();
        return labeler;
    }

    protected int checkMaxDimensionality(InputData data, int num) {
        if (num > data.templateVector().dim()) {
            Logger.getLogger("at.tuwien.ifs.somtoolbox").warning(
                    "Specified number of labels (" + num + ") exceeds number of features in template vector ("
                            + data.templateVector().dim()
                            + ") - defaulting to number of features as maximum possible value.");
            return data.templateVector().dim();
        }
        return num;
    }

}
