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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Logger;

import org.apache.commons.lang.ArrayUtils;

import com.martiansoftware.jsap.JSAPResult;
import com.martiansoftware.jsap.Parameter;

import at.tuwien.ifs.somtoolbox.SOMToolboxException;
import at.tuwien.ifs.somtoolbox.apps.config.AbstractOptionFactory;
import at.tuwien.ifs.somtoolbox.apps.config.OptionFactory;
import at.tuwien.ifs.somtoolbox.data.InputData;
import at.tuwien.ifs.somtoolbox.data.InputDataFactory;
import at.tuwien.ifs.somtoolbox.data.InputDataWriter;
import at.tuwien.ifs.somtoolbox.data.InputDatum;
import at.tuwien.ifs.somtoolbox.data.SOMLibClassInformation;
import at.tuwien.ifs.somtoolbox.data.SOMLibSparseInputData;

/**
 * Creates a subset of a vector file by removing instances from specific classes
 * 
 * @author Rudolf Mayer
 * @version $Id: VectorFileSubsetGenerator.java 4157 2011-02-11 15:57:27Z mayer $
 */
public class VectorFileSubsetGenerator {

    private static final Parameter[] OPTIONS = new Parameter[] { OptionFactory.getOptInputFileName(),
            OptionFactory.getOptInputFormat(false, InputDataFactory.INPUT_FILE_FORMAT_TYPES),
            OptionFactory.getOptClassInformationFile(false), OptionFactory.getOptClasslist(true),
            OptionFactory.getOptOutputFormat(false, InputDataWriter.OUTPUT_FILE_FORMAT_TYPES),
            OptionFactory.getOptOutputFileName(true), };

    public static void main(String[] args) throws IOException, SOMToolboxException {
        // register and parse all options
        JSAPResult config = OptionFactory.parseResults(args, OPTIONS);

        String inputFileName = AbstractOptionFactory.getFilePath(config, "input");
        String classInformationFileName = AbstractOptionFactory.getFilePath(config, "classInformationFile");
        String outputFileName = AbstractOptionFactory.getFilePath(config, "output");

        String[] keepClasses = config.getStringArray("classList");

        boolean skipInstanceNames = false;// config.getBoolean("skipInstanceNames");
        boolean skipInputsWithoutClass = false;// config.getBoolean("skipInputsWithoutClass");
        boolean tabSeparatedClassFile = false;// config.getBoolean("tabSeparatedClassFile");

        String inputFormat = config.getString("inputFormat");
        if (inputFormat == null) {
            inputFormat = InputDataFactory.detectInputFormatFromExtension(inputFileName, "input");
        }
        String outputFormat = config.getString("outputFormat");
        if (outputFormat == null) {
            outputFormat = InputDataFactory.detectInputFormatFromExtension(outputFileName, "output");
        }

        InputData data = InputDataFactory.open(inputFormat, inputFileName);
        if (classInformationFileName != null) {
            SOMLibClassInformation classInfo = new SOMLibClassInformation(classInformationFileName);
            data.setClassInfo(classInfo);
        }
        if (data.classInformation() == null) {
            throw new SOMToolboxException("Need to provide a class information file.");
        }

        Logger.getLogger("at.tuwien.ifs.somtoolbox").info(
                "Retaining elements of classes: " + Arrays.toString(keepClasses));

        ArrayList<InputDatum> subData = new ArrayList<InputDatum>();
        for (int i = 0; i < data.numVectors(); i++) {
            InputDatum datum = data.getInputDatum(i);
            String className = data.classInformation().getClassName(datum.getLabel());
            System.out.println(datum.getLabel() + "=>" + className);
            if (ArrayUtils.contains(keepClasses, className)) {
                subData.add(datum);
            }
        }

        InputData subset = new SOMLibSparseInputData(subData.toArray(new InputDatum[subData.size()]),
                data.classInformation());
        InputDataWriter.write(outputFileName, subset, outputFormat, tabSeparatedClassFile, skipInstanceNames,
                skipInputsWithoutClass);
    }

}
