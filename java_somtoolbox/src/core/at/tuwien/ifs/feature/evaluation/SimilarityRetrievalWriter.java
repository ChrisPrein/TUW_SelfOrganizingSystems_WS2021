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
package at.tuwien.ifs.feature.evaluation;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.logging.Logger;

import org.apache.commons.lang.math.NumberUtils;

import com.martiansoftware.jsap.JSAPResult;
import com.martiansoftware.jsap.Parameter;

import at.tuwien.ifs.commons.util.collection.SmallestElementSet;
import at.tuwien.ifs.somtoolbox.SOMToolboxException;
import at.tuwien.ifs.somtoolbox.apps.SOMToolboxApp;
import at.tuwien.ifs.somtoolbox.apps.config.AbstractOptionFactory;
import at.tuwien.ifs.somtoolbox.apps.config.OptionFactory;
import at.tuwien.ifs.somtoolbox.data.InputDatum;
import at.tuwien.ifs.somtoolbox.data.SOMLibSparseInputData;
import at.tuwien.ifs.somtoolbox.layers.metrics.AbstractMetric;
import at.tuwien.ifs.somtoolbox.layers.metrics.DistanceMetric;
import at.tuwien.ifs.somtoolbox.layers.metrics.L1Metric;
import at.tuwien.ifs.somtoolbox.util.FileUtils;
import at.tuwien.ifs.somtoolbox.util.StdErrProgressWriter;
import at.tuwien.ifs.somtoolbox.util.StringUtils;
import at.tuwien.ifs.somtoolbox.util.comparables.InputDistance;

/**
 * Provides a simple similarity-retrieval on vector files, as stand-alone application.
 * 
 * @author Rudolf Mayer
 * @version $Id: SimilarityRetrievalWriter.java 4218 2011-05-26 13:42:34Z mayer $
 */
public class SimilarityRetrievalWriter implements SOMToolboxApp {
    public static final Parameter[] OPTIONS = new Parameter[] { OptionFactory.getOptInputVectorFile(true),
            OptionFactory.getOptNumberNeighbours(false, "1000"),
            OptionFactory.getOptMetric(false, L1Metric.class.getName()), OptionFactory.getOptOutputDirectory(true),
            OptionFactory.getOptStartIndex(false, 0), OptionFactory.getOptNumberItems(false) };

    public static final String DESCRIPTION = "Performs similarity retrieval in a given database (vector file)";

    public static final String LONG_DESCRIPTION = DESCRIPTION + ", and writes the results in one file per data itme";

    public static final Type APPLICATION_TYPE = Type.Utils;

    public static void main(String[] args) throws SOMToolboxException, IOException {
        // register and parse all options
        JSAPResult config = OptionFactory.parseResults(args, OPTIONS);
        File inputVectorFile = config.getFile("inputVectorFile");
        String outputDirStr = AbstractOptionFactory.getFilePath(config, "outputDirectory");
        File outputDirBase = new File(outputDirStr);
        outputDirBase.mkdirs();
        String metricName = config.getString("metric");
        DistanceMetric metric = AbstractMetric.instantiateNice(metricName);

        int neighbours = config.getInt("numberNeighbours");
        int startIndex = config.getInt("startIndex");
        int numberItems = config.getInt("numberItems", -1);

        try {
            SOMLibSparseInputData data = new SOMLibSparseInputData(inputVectorFile.getAbsolutePath());
            int endIndex = data.numVectors();
            if (numberItems != -1) {
                if (startIndex + numberItems > endIndex) {
                    System.out.println("Specified number of items (" + numberItems + ") exceeds maximum ("
                            + data.numVectors() + "), limiting to " + (endIndex - startIndex) + ".");
                } else {
                    endIndex = startIndex + numberItems;
                }
            }
            StdErrProgressWriter progress = new StdErrProgressWriter(endIndex - startIndex, "processing vector ");
            // SortedSet<InputDistance> distances;
            for (int inputDatumIndex = startIndex; inputDatumIndex < endIndex; inputDatumIndex++) {

                InputDatum inputDatum = data.getInputDatum(inputDatumIndex);
                String inputLabel = inputDatum.getLabel();
                if (inputDatumIndex == -1) {
                    throw new IllegalArgumentException("Input with label '" + inputLabel
                            + "' not found in vector file '" + inputVectorFile + "'; possible labels are: "
                            + StringUtils.toString(data.getLabels(), 15));
                }

                File outputDir = new File(outputDirBase, inputLabel.charAt(2) + "/" + inputLabel.charAt(3) + "/"
                        + inputLabel.charAt(4));
                outputDir.mkdirs();
                File outputFile = new File(outputDir, inputLabel + ".txt");

                boolean fileExistsAndValid = false;
                if (outputFile.exists()) {
                    // check if it the valid data
                    String linesInvalid = "";
                    int validLineCount = 0;
                    ArrayList<String> lines = FileUtils.readLinesAsList(outputFile.getAbsolutePath());
                    for (String string : lines) {
                        if (string.trim().length() == 0) {
                            continue;
                        }
                        String[] parts = string.split("\t");
                        if (parts.length != 2) {
                            linesInvalid += "Line '" + string + "' invalid - contains " + parts.length + " elements.\n";
                        } else if (!NumberUtils.isNumber(parts[1])) {
                            linesInvalid = "Line '" + string + "' invalid - 2nd part is not a number.\n";
                        } else {
                            validLineCount++;
                        }
                    }
                    if (validLineCount != neighbours) {
                        linesInvalid = "Not enough valid lines; expected " + neighbours + ", found " + validLineCount
                                + ".\n";
                    }
                    fileExistsAndValid = true;
                    if (org.apache.commons.lang.StringUtils.isNotBlank(linesInvalid)) {
                        System.out.println("File " + outputFile.getAbsolutePath() + " exists, but is not valid:\n"
                                + linesInvalid);
                    }
                }

                if (fileExistsAndValid) {
                    Logger.getLogger("at.tuwien.ifs.feature.evaluation").finer(
                            "File " + outputFile.getAbsolutePath() + " exists and is valid; not recomputing");
                } else {
                    PrintWriter p = new PrintWriter(outputFile);
                    SmallestElementSet<InputDistance> distances = data.getNearestDistances(inputDatumIndex, neighbours,
                            metric);
                    for (InputDistance inputDistance : distances) {
                        p.println(inputDistance.getInput().getLabel() + "\t" + inputDistance.getDistance());
                    }
                    p.close();
                }
                progress.progress();
            }

        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage() + ". Aborting.");
            System.exit(-1);
        }
    }
}
