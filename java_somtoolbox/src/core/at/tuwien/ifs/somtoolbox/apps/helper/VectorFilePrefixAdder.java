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

import com.martiansoftware.jsap.JSAPResult;
import com.martiansoftware.jsap.Parameter;

import at.tuwien.ifs.somtoolbox.apps.config.AbstractOptionFactory;
import at.tuwien.ifs.somtoolbox.apps.config.OptionFactory;
import at.tuwien.ifs.somtoolbox.data.InputDataWriter;
import at.tuwien.ifs.somtoolbox.data.SOMLibSparseInputData;

/**
 * @author Rudolf Mayer
 * @version $Id: VectorFilePrefixAdder.java 4031 2011-01-29 22:30:28Z mayer $
 */
public class VectorFilePrefixAdder {

    private static final Parameter[] OPTIONS = new Parameter[] { OptionFactory.getOptInputFileName(),
            OptionFactory.getOptProperties(true), OptionFactory.getOptOutputVector() };

    public static void main(String[] args) throws IOException {
        JSAPResult config = OptionFactory.parseResults(args, OPTIONS);
        String prefix = config.getString("prefix");

        String fileName = AbstractOptionFactory.getFilePath(config, "inputFile");
        String outputFileName = AbstractOptionFactory.getFilePath(config, "output");
        SOMLibSparseInputData data = new SOMLibSparseInputData(fileName);
        int numVectors = data.numVectors();
        for (int i = 0; i < numVectors; i++) {
            data.setLabel(i, prefix + data.getLabel(i));
        }
        InputDataWriter.writeAsSOMLib(data, outputFileName);
    }

}
