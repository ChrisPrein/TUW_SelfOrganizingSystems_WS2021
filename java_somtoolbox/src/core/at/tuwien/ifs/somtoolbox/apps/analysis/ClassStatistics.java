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
package at.tuwien.ifs.somtoolbox.apps.analysis;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map.Entry;

import com.martiansoftware.jsap.JSAPResult;

import at.tuwien.ifs.commons.util.collection.EntryValueComparator;
import at.tuwien.ifs.somtoolbox.SOMToolboxException;
import at.tuwien.ifs.somtoolbox.apps.config.OptionFactory;
import at.tuwien.ifs.somtoolbox.data.SOMLibClassInformation;
import at.tuwien.ifs.somtoolbox.util.StringUtils;

/**
 * Prints out statistics on a {@link SOMLibClassInformation} file.
 * 
 * @author Rudolf Mayer
 * @version $Id: FeatureDistributionAnalysis.java 3589 2010-05-21 10:42:01Z mayer $
 */
public class ClassStatistics {

    public static void main(String[] args) throws SOMToolboxException {
        // register and parse all options
        JSAPResult config = OptionFactory.parseResults(args, OptionFactory.getOptClassInformationFile(false),
                OptionFactory.getOptSort(false));
        File classInfoFileName = config.getFile("classInformationFile");
        boolean sort = config.getBoolean("sort", true);

        SOMLibClassInformation classInfo = new SOMLibClassInformation(classInfoFileName.getPath());
        String[] classNames = classInfo.getClassNames();

        // output
        String[] classNameDifferences = StringUtils.getDifferences(classNames);
        int padLength = StringUtils.getLongestStringLength(classNameDifferences) + 5;

        System.out.println("Stats of '" + classInfoFileName.getName() + "'");
        System.out.println(StringUtils.pad("Classes", padLength) + classInfo.numClasses());
        System.out.println();

        if (sort) {
            HashMap<String, Integer> tempData = new HashMap<String, Integer>();
            for (int i = 0; i < classNameDifferences.length; i++) {
                tempData.put(classNameDifferences[i], classInfo.getNumberOfClassMembers(i));
            }
            ArrayList<Entry<String, Integer>> entries = new ArrayList<Entry<String, Integer>>(tempData.entrySet());
            Collections.sort(entries, new EntryValueComparator<String, Integer>(true));
            for (Entry<String, Integer> entry : entries) {
                String key = entry.getKey();
                Integer value = entry.getValue();
                printLine(padLength, key, value);
            }
        } else { // else-branch duplicates code, to not store the data needlessly in a map
            for (int i = 0; i < classNameDifferences.length; i++) {
                printLine(padLength, classNameDifferences[i], classInfo.getNumberOfClassMembers(i));
            }
        }
    }

    private static void printLine(int padLength, String key, Integer value) {
        System.out.println(StringUtils.pad(key, padLength) + "\t" + value);
    }
}
