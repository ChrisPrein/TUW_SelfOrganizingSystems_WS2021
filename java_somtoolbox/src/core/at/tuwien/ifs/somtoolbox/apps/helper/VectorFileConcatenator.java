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
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

import cern.colt.matrix.DoubleMatrix1D;
import cern.colt.matrix.impl.DenseDoubleMatrix1D;

import com.martiansoftware.jsap.JSAPResult;
import com.martiansoftware.jsap.Parameter;

import at.tuwien.ifs.somtoolbox.SOMToolboxException;
import at.tuwien.ifs.somtoolbox.apps.SOMToolboxApp;
import at.tuwien.ifs.somtoolbox.apps.config.OptionFactory;
import at.tuwien.ifs.somtoolbox.data.InputData;
import at.tuwien.ifs.somtoolbox.data.InputDataFactory;
import at.tuwien.ifs.somtoolbox.data.InputDataWriter;
import at.tuwien.ifs.somtoolbox.data.InputDatum;
import at.tuwien.ifs.somtoolbox.data.SOMLibClassInformation;
import at.tuwien.ifs.somtoolbox.data.SOMLibSparseInputData;
import at.tuwien.ifs.somtoolbox.data.SOMLibTemplateVector;
import at.tuwien.ifs.somtoolbox.data.TemplateVector;
import at.tuwien.ifs.somtoolbox.util.VectorTools;

/**
 * Concatenates two vector files.
 * 
 * @author Jakob Frank
 * @version $Id: VectorFileConcatenator.java 4268 2012-04-03 14:51:33Z mayer $
 */
public class VectorFileConcatenator implements SOMToolboxApp {
    public static final Parameter[] OPTIONS = new Parameter[] { OptionFactory.getOptOutputVector(),
            OptionFactory.getOptInput(), OptionFactory.getOptNormalizeWeights(),
            OptionFactory.getOptOutputFormat(false, InputDataWriter.OUTPUT_FILE_FORMAT_TYPES),
            OptionFactory.getSwitchWriteTVFile() };

    public static String DESCRIPTION = "Merge SOMLibVectorFiles";

    public static String LONG_DESCRIPTION = "Merge two or more VectorFiles containing different Features of the same Data into one Vector file";

    public static final Type APPLICATION_TYPE = Type.Helper;

    /**
     * @param args Command line args.
     */
    public static void main(String[] args) throws SOMToolboxException, IOException {
        // TODO: Add option inner-, (left|right|both)outer-join, default is inner
        JSAPResult config = OptionFactory.parseResults(args, OPTIONS);

        File[] inFiles = config.getFileArray("input");
        File outFile = config.getFile("output");

        String outputFormat = config.getString("outputFormat");
        if (outputFormat == null) {
            outputFormat = InputDataFactory.detectInputFormatFromExtension(outFile.getAbsolutePath(), "output");
        }

        boolean skipInstanceNames = false;// config.getBoolean("skipInstanceNames");
        boolean skipInputsWithoutClass = false;// config.getBoolean("skipInputsWithoutClass");
        boolean tabSeparatedClassFile = false;// config.getBoolean("tabSeparatedClassFile");

        boolean normalise = true;
        float w[] = new float[inFiles.length];
        if (config.userSpecified("weights")) {
            for (int i = 0; i < w.length; i++) {
                w[i] = 1;
            }
            float wgts[] = config.getFloatArray("weights");
            for (int i = 0; i < Math.min(wgts.length, w.length); i++) {
                w[i] = wgts[i];
            }
        } else {
            normalise = false;
        }

        Logger log = Logger.getLogger(VectorFileConcatenator.class.getName());

        SOMLibClassInformation classInfo = null;

        InputData data[] = new InputData[inFiles.length];
        for (int i = 0; i < data.length; i++) {
            data[i] = InputDataFactory.open(inFiles[i].getAbsolutePath());
            if (classInfo == null && data[i].classInformation() != null) {
                classInfo = data[i].classInformation();
            }
        }

        List<InputDatum> idList = new LinkedList<InputDatum>();
        log.info("Starting merge...");
        String[] labels = data[0].getLabels();
        for (String label : labels) {
            InputDatum[] datum = new InputDatum[data.length];

            boolean incomplete = false;
            for (int i = 0; i < datum.length; i++) {
                datum[i] = data[i].getInputDatum(label);
                if (datum[i] == null) {
                    incomplete = true;
                    break;
                }
            }
            if (incomplete) {
                continue;
            }

            if (normalise) {
                for (int i = 0; i < datum.length; i++) {
                    datum[i] = VectorTools.normaliseByLength(datum[i], w[i]);
                }
            }

            DoubleMatrix1D[] vec = new DoubleMatrix1D[datum.length];
            int resSize = 0;
            for (int i = 0; i < vec.length; i++) {
                vec[i] = datum[i].getVector();
                resSize += vec[i].size();
            }

            DoubleMatrix1D res = new DenseDoubleMatrix1D(resSize);

            int offset = 0;
            for (DoubleMatrix1D element : vec) {
                for (int j = 0; j < element.size(); j++) {
                    res.setQuick(offset + j, element.get(j));
                }
                offset += element.size();
            }
            InputDatum id = new InputDatum(label, res);
            idList.add(id);
        }
        SOMLibSparseInputData id = new SOMLibSparseInputData(idList.toArray(new InputDatum[] {}), null);
        id.setClassInfo(classInfo);

        // check if a template vector was provided
        // FIXME: at the moment, this happens e.g. integrated in an ARFF file; this is a partial solution to
        // https://olymp.ifs.tuwien.ac.at/trac/somtoolbox/ticket/267
        boolean haveTemplate = true;
        for (InputData element : data) {
            if (element.templateVector() == null) {
                haveTemplate = false;
                break;
            }
        }

        SOMLibTemplateVector tv = null;
        if (haveTemplate) {
            log.info("TemplateVectors provided, merging");
            tv = new SOMLibTemplateVector(id.numVectors(), id.dim());
            int index = 0;
            for (InputData element : data) {
                TemplateVector t = element.templateVector();
                for (int j = 0; j < t.dim(); j++) {
                    tv.setElement(index, t.getElement(j));
                    index++;
                }
            }
        } else if (config.getBoolean("writeTV")) {
            log.info("Generating TemplateVector");
            String[] tvAttr = new String[id.dim()];
            int offset = 0;
            for (InputData element : data) {
                for (int j = 0; j < element.dim(); j++) {
                    // FIXME: will work only if the subtype is provided !
                    tvAttr[offset + j] = element.getContentType().getSubtype() + "_" + j;
                }
                offset += element.dim();
            }
            tv = new SOMLibTemplateVector(id.numVectors(), tvAttr);
        }
        id.setTemplateVector(tv);

        log.info("Merge finished. Writing result.");

        System.out.println(outFile.getAbsolutePath());

        InputDataWriter.write(outFile.getAbsolutePath(), id, outputFormat, tabSeparatedClassFile, skipInstanceNames,
                skipInputsWithoutClass);

        log.info("Vector written.");

        log.info("Done");
    }
}
