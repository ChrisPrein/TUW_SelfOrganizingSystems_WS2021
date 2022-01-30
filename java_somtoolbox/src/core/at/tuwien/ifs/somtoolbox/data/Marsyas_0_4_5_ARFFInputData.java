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
package at.tuwien.ifs.somtoolbox.data;

import java.io.BufferedReader;
import java.io.IOException;

/**
 * Marsyas 0.4.5 writes, after the line with the label name as comment (see {@link MarsyasARFFInputData}, an additional
 * line such as
 * 
 * <pre>
 * % srate 43.0664
 * </pre>
 * 
 * This line will be ignored when reading the file.
 * 
 * @author Rudolf Mayer
 * @version $Id: $
 */
public class Marsyas_0_4_5_ARFFInputData extends MarsyasARFFInputData {

    public Marsyas_0_4_5_ARFFInputData(String arffFileName) {
        super(arffFileName);
    }

    @Override
    protected String getInstanceName(String line, BufferedReader reader) throws IOException {
        String instanceName = line.replaceFirst("% filename ", "").trim(); // replace % filename
        reader.readLine();
        return instanceName;
    }

    public static String getFormatName() {
        return "Marsyas0.4.5ARFF";
    }

}
