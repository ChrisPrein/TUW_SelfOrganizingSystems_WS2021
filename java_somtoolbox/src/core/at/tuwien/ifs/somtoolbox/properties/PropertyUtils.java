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

import com.martiansoftware.jsap.FlaggedOption;
import com.martiansoftware.jsap.JSAP;
import com.martiansoftware.jsap.StringParser;

/**
 * @author Rudolf Mayer
 * @version $Id: PropertyUtils.java 4249 2012-01-25 10:28:29Z mayer $
 */
public class PropertyUtils {

    public static FlaggedOption getStringOption(String id, String defaultValue, boolean required, String help) {
        return getOption(id, JSAP.STRING_PARSER, defaultValue, required, help);
    }

    public static FlaggedOption getStringOption(String id, String help) {
        return getOption(id, JSAP.STRING_PARSER, JSAP.NO_DEFAULT, true, help);
    }

    public static FlaggedOption getBooleanOption(String id, boolean defaultValue, boolean required, String help) {
        return getOption(id, JSAP.BOOLEAN_PARSER, String.valueOf(defaultValue), required, help);
    }

    public static FlaggedOption getDoubleOption(String id, double defaultValue, boolean required, String help) {
        return getOption(id, JSAP.DOUBLE_PARSER, String.valueOf(defaultValue), required, help);
    }

    public static FlaggedOption getIntegerOption(String id, int defaultValue, boolean required, String help) {
        return getOption(id, JSAP.INTEGER_PARSER, String.valueOf(defaultValue), required, help);
    }

    public static FlaggedOption getIntegerOption(String id, boolean required, String help) {
        return getOption(id, JSAP.INTEGER_PARSER, JSAP.NO_DEFAULT, required, help);
    }

    public static FlaggedOption getIntegerOption(String id, String help) {
        return getOption(id, JSAP.INTEGER_PARSER, JSAP.NO_DEFAULT, true, help);
    }

    public static FlaggedOption getOption(String id, StringParser parser, String defaultValue, boolean required,
            String help) {
        return new FlaggedOption(id, parser, defaultValue, required, JSAP.NO_SHORTFLAG, id, help);
    }

}
