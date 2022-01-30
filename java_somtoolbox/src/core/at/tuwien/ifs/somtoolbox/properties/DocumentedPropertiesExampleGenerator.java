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
package at.tuwien.ifs.somtoolbox.properties;

import com.martiansoftware.jsap.Option;

import at.tuwien.ifs.somtoolbox.SOMToolboxMetaConstants;
import at.tuwien.ifs.somtoolbox.util.StringUtils;

/**
 * @author Creates a fully documented properties file from the information defined in {@link FileProperties},
 *         {@link SOMProperties} and {@link GHSOMProperties}
 * @version $Id: DocumentedPropertiesExampleGenerator.java 4257 2012-01-25 13:19:20Z mayer $
 */
public class DocumentedPropertiesExampleGenerator {
    public static void main(String[] args) {
        StringBuilder sb = new StringBuilder();

        int longestStringLength = 0;
        final Option[][] allOptions = new Option[][] { FileProperties.FILE_OPTIONS, FileProperties.DATABASE_OPTIONS,
                SOMProperties.SOM_TRAINING_OPTIONS, SOMProperties.SOM_BATCH_OPTIONS, SOMProperties.SOM_GRID_OPTIONS,
                SOMProperties.SOM_OTHERS, GHSOMProperties.GHSOM_OPTIONS };
        for (Option[] options : allOptions) {
            for (Option o : options) {
                if (o.getID().length() + o.getHelp().length() > longestStringLength) {
                    longestStringLength = o.getID().length() + o.getHelp().length();
                }
            }
        }
        int maxSpace = longestStringLength + 5;

        sb.append("# SOMToolbox version " + SOMToolboxMetaConstants.getVersion());

        sb.append("\n\n");
        sb.append("# 1. Properties specifiying input data and generated output data\n");
        processOptions(sb, FileProperties.FILE_OPTIONS, maxSpace);

        sb.append("\n\n");
        sb.append("# 2. Database properties; alternative to section 1 (input and output data).\n");
        processOptions(sb, FileProperties.DATABASE_OPTIONS, maxSpace);

        sb.append("\n\n");
        sb.append("# 3. SOM training properties\n");
        processOptions(sb, SOMProperties.SOM_TRAINING_OPTIONS, maxSpace);

        sb.append("\n\n");
        sb.append("# 3b. Batch SOM gproperties\n");
        processOptions(sb, SOMProperties.SOM_BATCH_OPTIONS, maxSpace);

        sb.append("\n\n");
        sb.append("# 3c. SOM grid layout properties\n");
        processOptions(sb, SOMProperties.SOM_GRID_OPTIONS, maxSpace);

        sb.append("\n\n");
        sb.append("# 3d. Miscellaneous SOM properties\n");
        processOptions(sb, SOMProperties.SOM_OTHERS, maxSpace);

        sb.append("\n\n");
        sb.append("# 4. GHSOM training properties (GHSOM requires SOM training properties as well)\n");
        processOptions(sb, GHSOMProperties.GHSOM_OPTIONS, maxSpace);

        System.out.println(sb);

    }

    private static void processOptions(final StringBuilder sb, final Option[] options, int maxSpace) {
        for (Option o : options) {
            final String defaultValue = o.getDefault() != null ? "\tdefault: '" + o.getDefault()[0] + "'" : "";
            String x = StringUtils.repeatString(maxSpace - o.getID().length() - o.getHelp().length(), " ");
            sb.append(o.getID() + " = <" + o.getHelp() + ">" + x + (o.required() ? "REQUIRED  " : "          ")
                    + defaultValue + "\n");
        }
    }

}
