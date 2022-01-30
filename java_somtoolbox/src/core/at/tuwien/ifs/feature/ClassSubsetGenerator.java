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
package at.tuwien.ifs.feature;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.TreeSet;

import com.martiansoftware.jsap.JSAPResult;
import com.martiansoftware.jsap.Parameter;

import at.tuwien.ifs.somtoolbox.apps.SOMToolboxApp;
import at.tuwien.ifs.somtoolbox.apps.config.OptionFactory;
import at.tuwien.ifs.somtoolbox.data.ARFFFormatInputData;
import at.tuwien.ifs.somtoolbox.data.InputDataWriter;
import at.tuwien.ifs.somtoolbox.util.FileUtils;
import at.tuwien.ifs.somtoolbox.util.StdErrProgressWriter;
import at.tuwien.ifs.somtoolbox.util.StringUtils;

/**
 * @author Rudolf Mayer
 * @version $Id: $
 */
public class ClassSubsetGenerator implements SOMToolboxApp {

    public static final Parameter[] OPTIONS = new Parameter[] { OptionFactory.getOptClassInformationFile(true),
            OptionFactory.getOptGZip(false, true), OptionFactory.getOptInputFileName(),
            OptionFactory.getOptOutputFileName(true), };

    public static String DESCRIPTION = "Generates a subset and class assignment based on the given class file";

    public static String LONG_DESCRIPTION = DESCRIPTION;

    public static final Type APPLICATION_TYPE = Type.Helper;

    public static void main(String[] args) throws IOException {
        JSAPResult config = OptionFactory.parseResults(args, OPTIONS);

        File inputFile = config.getFile("input");
        File classInformationFile = config.getFile("classInformationFile");
        File outputFile = config.getFile("output");

        HashMap<String, String> classAssignment = new HashMap<String, String>();
        BufferedReader classReader = new BufferedReader(new FileReader(classInformationFile));
        String classLine = null;
        while ((classLine = classReader.readLine()) != null) {
            String[] split = classLine.split("\t");
            if (split.length == 2) {
                classAssignment.put(split[0].trim(), split[1].trim());
            } else if (classLine.trim().length() > 0) {
                System.out.println("Ignoring invalid class line '" + classLine + "'.");
            }
        }
        System.out.println("Read class assignments for " + classAssignment.size() + " files");

        TreeSet<String> classes = new TreeSet<String>();
        for (String cls : classAssignment.values()) {
            classes.add(cls);
        }
        System.out.println("Found classes: " + classes);

        BufferedReader reader = new BufferedReader(new FileReader(inputFile));
        PrintWriter writer = new PrintWriter(outputFile);

        int fileLineCount = FileUtils.lineCount(inputFile.getAbsolutePath());

        HashSet<String> assignedFiles = new HashSet<String>();
        boolean pastHeader = false;
        boolean replacedStringHeader = false;
        String line = null;
        long lineNumber = 0;

        StdErrProgressWriter progress = null;
        while ((line = reader.readLine()) != null) {
            lineNumber++;
            if (!pastHeader) { // just pass headers through to output file
                if (!replacedStringHeader && line.trim().toUpperCase().startsWith(ARFFFormatInputData.ATTRIBUTE)
                        && line.trim().toLowerCase().endsWith(ARFFFormatInputData.STRING_TYPE)) {
                    replacedStringHeader = true;
                    String wekaClassHeader = InputDataWriter.getWekaClassHeader(classes.toArray(new String[classes.size()]));
                    writer.println(wekaClassHeader);
                    System.out.println("Replacing " + ARFFFormatInputData.STRING_TYPE
                            + " attribute with class attribute:");
                    System.out.println("\t" + line);
                    System.out.println("\t=>");
                    System.out.println(wekaClassHeader);
                } else {
                    writer.println(line);
                }
                // System.out.println("passing on pre-data line");
            } else {
                progress.progress();
                if (line.trim().startsWith("%")) {
                    writer.println(line); // just pass comments through to output file
                    // System.out.println("passing on comment line");
                } else {
                    String[] split = line.split(",");
                    String label = StringUtils.stripQuotes(split[split.length - 1].trim());
                    if (classAssignment.containsKey(label)) {
                        writer.println(line.replace(label, classAssignment.get(label)));
                        assignedFiles.add(label);
                        // System.out.println("found class label for " + label);
                    } else {
                        // System.out.println("did not find class label for " + label);
                    }
                }
            }
            if (line.toLowerCase().trim().startsWith("@data")) {
                pastHeader = true;
                System.out.println("Found @data marker!");
                writer.flush();
                progress = new StdErrProgressWriter((int) (fileLineCount - lineNumber), "Reading input ", 100);
            }
        }
        writer.close();
        System.out.println("Wrote new file for " + assignedFiles.size() + " matches");

    }
}
