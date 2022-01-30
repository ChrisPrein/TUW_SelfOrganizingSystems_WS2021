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
import java.util.logging.Logger;

import com.martiansoftware.jsap.JSAPResult;
import com.martiansoftware.jsap.Parameter;

import at.tuwien.ifs.somtoolbox.SOMToolboxException;
import at.tuwien.ifs.somtoolbox.apps.config.AbstractOptionFactory;
import at.tuwien.ifs.somtoolbox.apps.config.OptionFactory;
import at.tuwien.ifs.somtoolbox.data.InputDataFactory;
import at.tuwien.ifs.somtoolbox.data.InputDataWriter;
import at.tuwien.ifs.somtoolbox.data.SOMLibSparseInputData;

/**
 * Creates a subset of a vector file by removing instances from specific classes
 * 
 * @author Rudolf Mayer
 * @version $Id: VectorFileSubsetGenerator.java 4157 2011-02-11 15:57:27Z mayer $
 */
public class VectorFileRandomiser {

    private static final Parameter[] OPTIONS = new Parameter[] { OptionFactory.getOptInputFileName(),
            OptionFactory.getOptInputFormat(false, InputDataFactory.INPUT_FILE_FORMAT_TYPES),
            OptionFactory.getOptOutputFormat(false, InputDataWriter.OUTPUT_FILE_FORMAT_TYPES),
            OptionFactory.getOptOutputFileName(true), OptionFactory.getOptNumberItems(true) };

    public static void main(String[] args) throws IOException, SOMToolboxException {
        // register and parse all options
        JSAPResult config = OptionFactory.parseResults(args, OPTIONS);

        String inputFileName = AbstractOptionFactory.getFilePath(config, "input");
        String outputFileName = AbstractOptionFactory.getFilePath(config, "output");
        int attributeIndex = config.getInt("numberItems");

        String outputFormat = config.getString("outputFormat");
        if (outputFormat == null) {
            outputFormat = InputDataFactory.detectInputFormatFromExtension(outputFileName, "output");
        }

        SOMLibSparseInputData data = new SOMLibSparseInputData(inputFileName);

        Logger.getLogger("at.tuwien.ifs.somtoolbox").info("Modifying attribute with index " + attributeIndex);

        for (int i = 0; i < data.numVectors(); i++) {
            double[] vector = data.getInputVector(i);
            System.out.print(vector[attributeIndex]);
            data.setValue(i, attributeIndex, data.getValue(i, attributeIndex) * 2102);
            System.out.print("\t=>" + vector[attributeIndex]);

            vector = data.getInputVector(i);
            System.out.println("\t=>" + vector[attributeIndex]);
        }

        InputDataWriter.writeAsSOMLib(data, outputFileName);
    }
}
