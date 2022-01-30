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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.zip.GZIPOutputStream;

import com.martiansoftware.jsap.JSAPResult;
import com.martiansoftware.jsap.Parameter;

import at.tuwien.ifs.somtoolbox.apps.SOMToolboxApp;
import at.tuwien.ifs.somtoolbox.apps.config.AbstractOptionFactory;
import at.tuwien.ifs.somtoolbox.apps.config.OptionFactory;
import at.tuwien.ifs.somtoolbox.apps.config.OptionFactory.MatchMode;
import at.tuwien.ifs.somtoolbox.util.FileUtils;
import at.tuwien.ifs.somtoolbox.util.StdErrProgressWriter;

/**
 * Re-writes an unit description file by replacing the label names of the input vectors as defined by the given mapping
 * file.
 * 
 * @author Michael Dittenbach
 * @author Rudolf Mayer
 * @version $Id: VectorFileRewriter.java 4267 2012-04-03 14:51:20Z mayer $
 */
public class VectorFileRewriter implements SOMToolboxApp {

    private static final Parameter[] OPTIONS = new Parameter[] { OptionFactory.getOptInputFileName(),
            OptionFactory.getOptNameMappingFile(true), OptionFactory.getOptOutputVector(),
            OptionFactory.getOptMatchMode(false), OptionFactory.getOptStrip(false),
            OptionFactory.getSwitchOnlyLastPathSegment(false), OptionFactory.getOptGZip(false, false) };

    public static String DESCRIPTION = "Replace labels in SOMLibVectorFiles";

    public static String LONG_DESCRIPTION = "Replaces labels in SOMLibVectorFiles by means of a mapping file; labels in the mapping file must be separated by a tab";

    public static final Type APPLICATION_TYPE = Type.Helper;

    public static void main(String[] args) throws IOException {
        // register and parse all options
        JSAPResult config = OptionFactory.parseResults(args, OPTIONS);

        String inputFileName = AbstractOptionFactory.getFilePath(config, "input");
        String mappingFile = AbstractOptionFactory.getFilePath(config, "nameMappingFile");
        String outputFileName = AbstractOptionFactory.getFilePath(config, "output");

        String stripFromLabel = config.getString("stripFromString");
        boolean onlyLastPathSegment = config.getBoolean("onlyLastPathSegment");

        boolean gzip = config.getBoolean("gzip");

        MatchMode matchMode = MatchMode.valueOf(config.getString("matchMode"));

        Hashtable<String, String> mapping = readMappingFile(mappingFile);
        String[] keys = null;
        if (matchMode == MatchMode.endsWith) {
            // defining the keys ones as String[] is still slow, but faster than using Hashmap#keySet() often
            keys = mapping.keySet().toArray(new String[mapping.size()]);
        }

        OutputStream out = gzip ? new GZIPOutputStream(new FileOutputStream(outputFileName)) : new FileOutputStream(
                outputFileName);
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(out));

        HashMap<String, String> headers = FileUtils.readSOMLibFileHeaders(
                FileUtils.openFile("Input vector", inputFileName), "input vector");
        int totalVectorCount = Integer.parseInt(headers.get("$XDIM"));

        BufferedReader br = FileUtils.openFile("Input vector", inputFileName);
        String line = null;
        StdErrProgressWriter progress = new StdErrProgressWriter(totalVectorCount, "rewriting vector ", 100);
        int written = 0;
        int skipped = 0;
        while ((line = br.readLine()) != null) {
            if (line.startsWith("$")) {
                bw.write(line);
                bw.newLine();
            } else {
                int lastPos = line.lastIndexOf(" ");
                String label = line.substring(lastPos + 1);

                // System.out.println(stripFromLabel);
                // System.out.print(label);
                label = label.replaceAll(stripFromLabel, "");
                if (onlyLastPathSegment && label.contains("/")) {
                    label = label.substring(label.lastIndexOf("/") + 1);
                }
                // System.out.println("=>" + label);

                String target = getReplacement(mapping, keys, label, matchMode);
                if (target != null) {
                    bw.write(line.substring(0, lastPos));
                    bw.write(" " + target);
                    bw.newLine();
                    written++;
                } else {
                    // System.out.println("No label found for " + label);
                    skipped++;
                }

                progress.progress();
            }
        }

        br.close();
        bw.close();

        System.out.println("Wrote " + written + " vectors, skipped " + skipped
                + " because no label found in matching file");

    }

    public static String getReplacement(Hashtable<String, String> mapping, String[] keys, String token,
            MatchMode matchMode) {
        if (matchMode == MatchMode.exact) {
            return mapping.get(token);
        } else if (matchMode == MatchMode.withoutPath) {
            int index = token.lastIndexOf("/");
            String searchToken = token.substring(index + 1);
            return mapping.get(searchToken);
        } else if (matchMode == MatchMode.endsWith) {
            // FIXME: this is not at all efficient
            for (String key : keys) {
                if (token.endsWith(key)) {
                    return mapping.get(key);
                }
            }
            return null;
        } else {
            throw new IllegalArgumentException("Unsupported MatchMode '" + matchMode + "'.");
        }
    }

    static private Hashtable<String, String> readMappingFile(String fName) throws IOException {
        Hashtable<String, String> res = new Hashtable<String, String>();
        res.put("$MAPPED_VECS", "$MAPPED_VECS");
        BufferedReader br = FileUtils.openFile("Mapping file", fName);

        String line = null;
        while ((line = br.readLine()) != null) {
            String[] splits = line.split("\t", 2);
            String origString = splits[0];
            String targetString = splits[1];
            res.put(origString, targetString);
        }

        br.close();

        return res;
    }

}
