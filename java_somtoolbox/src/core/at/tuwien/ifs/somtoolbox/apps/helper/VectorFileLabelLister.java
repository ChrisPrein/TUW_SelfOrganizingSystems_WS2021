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
package at.tuwien.ifs.somtoolbox.apps.helper;

import java.io.IOException;
import java.io.PrintStream;
import java.util.Arrays;

import org.apache.commons.lang.StringUtils;

import com.martiansoftware.jsap.JSAPResult;
import com.martiansoftware.jsap.Parameter;

import at.tuwien.ifs.somtoolbox.apps.config.AbstractOptionFactory;
import at.tuwien.ifs.somtoolbox.apps.config.OptionFactory;
import at.tuwien.ifs.somtoolbox.data.InputData;
import at.tuwien.ifs.somtoolbox.data.InputDataFactory;

/**
 * Lists the name of the labels in the input vector file
 * 
 * @author Rudolf Mayer
 * @version $Id: VectorFileRewriter.java 3589 2010-05-21 10:42:01Z mayer $
 */
public class VectorFileLabelLister {

    private static final Parameter[] OPTIONS = new Parameter[] { OptionFactory.getOptInputFileName(),
            OptionFactory.getOptOutputFileName(false) };

    public static void main(String[] args) throws IOException {
        // register and parse all options
        JSAPResult config = OptionFactory.parseResults(args, OPTIONS);

        String inputVectorFileName = AbstractOptionFactory.getFilePath(config, "input");
        String outputFileName = AbstractOptionFactory.getFilePath(config, "output");

        PrintStream out;
        if (StringUtils.isBlank(outputFileName)) {
            out = System.out;
        } else {
            out = new PrintStream(outputFileName);
        }

        InputData data = InputDataFactory.open(inputVectorFileName);
        String[] labels = data.getLabels();
        Arrays.sort(labels);
        for (String s : labels) {
            out.println(s);
        }
        out.close();
    }

}
