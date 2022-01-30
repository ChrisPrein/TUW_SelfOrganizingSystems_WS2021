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

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.logging.Logger;

import cern.colt.Arrays;

import com.martiansoftware.jsap.JSAPResult;
import com.martiansoftware.jsap.Parameter;

import at.tuwien.ifs.somtoolbox.SOMToolboxException;
import at.tuwien.ifs.somtoolbox.apps.SOMToolboxApp;
import at.tuwien.ifs.somtoolbox.apps.config.OptionFactory;
import at.tuwien.ifs.somtoolbox.data.InputData;
import at.tuwien.ifs.somtoolbox.data.InputDataFactory;
import at.tuwien.ifs.somtoolbox.data.InputDataWriter;
import at.tuwien.ifs.somtoolbox.data.InputDatum;

/**
 * Appends multiple vector files to a new one.
 * 
 * @author Rudolf Mayer
 * @version $Id: VectorFileConcatenator.java 4268 2012-04-03 14:51:33Z mayer $
 */
public class VectorFileAppender implements SOMToolboxApp {
    public static final Parameter[] OPTIONS = new Parameter[] { OptionFactory.getOptOutputVector(),
            OptionFactory.getOptInput(), };

    public static String DESCRIPTION = "Append SOMLibVectorFiles";

    public static String LONG_DESCRIPTION = "Append multilple VectorFiles containing the same type of features into one Vector file";

    public static final Type APPLICATION_TYPE = Type.Helper;

    public static void main(String[] args) throws SOMToolboxException, IOException {
        JSAPResult config = OptionFactory.parseResults(args, OPTIONS);

        File[] inFiles = config.getFileArray("input");
        File outFile = config.getFile("output");

        Logger log = Logger.getLogger(VectorFileAppender.class.getName());
        log.info("Reading from files: " + Arrays.toString(inFiles));
        log.info("Output file: " + outFile);

        File logFile = new File(outFile.getAbsolutePath() + ".log");
        PrintWriter logWriter = new PrintWriter(logFile);
        log.info("Logging error files to '" + logFile.getAbsolutePath() + "'.");

        HashMap<String, InputDatum> mergedData = new HashMap<String, InputDatum>();
        HashMap<String, File> mergedDataOrigin = new HashMap<String, File>();

        int duplicates = 0;
        int errors = 0;

        for (File inFile : inFiles) {
            try {
                log.info("Processing " + inFile);
                InputData tmp = InputDataFactory.open(inFile.getAbsolutePath());
                for (int j = 0; j < tmp.numVectors(); j++) {
                    InputDatum datum = tmp.getInputDatum(j);
                    if (mergedData.containsKey(datum.getLabel())) {
                        InputDatum existingDatum = mergedData.get(datum.getLabel());
                        String msg = "Datum " + datum.getLabel() + " already present in merged file, read from "
                                + mergedDataOrigin.get(datum.getLabel()).getAbsolutePath();
                        System.out.println(msg);
                        if (datum.equals(existingDatum)) {
                            System.out.println("\t=> vectors are equal, ingoring");
                            duplicates++;
                        } else {
                            String detailedMsg = "\t=> vector differ!" + "\n" + "\tExisting vector:"
                                    + existingDatum.getVector() + "\n" + "\tThis vector vector:"
                                    + existingDatum.getVector();
                            System.out.println(detailedMsg);
                            logWriter.println(msg);
                            logWriter.println(detailedMsg);
                            errors++;
                        }
                    } else {
                        mergedData.put(datum.getLabel(), datum);
                        mergedDataOrigin.put(datum.getLabel(), inFile);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        log.info("Duplicates: " + duplicates);
        log.info("Errors: " + errors);

        log.info("Creating vector file... ");
        log.info("Writing vector file to " + outFile.getAbsolutePath());
        InputDataWriter.writeAsSOMLib(mergedData.values(), outFile.getAbsolutePath());

        log.info("Done");
        logWriter.close();
    }
}
