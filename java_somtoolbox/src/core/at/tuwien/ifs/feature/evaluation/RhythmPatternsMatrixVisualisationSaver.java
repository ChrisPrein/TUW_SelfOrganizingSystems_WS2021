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
package at.tuwien.ifs.feature.evaluation;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

import javax.imageio.ImageIO;

import com.martiansoftware.jsap.JSAPResult;
import com.martiansoftware.jsap.Parameter;

import at.tuwien.ifs.somtoolbox.apps.SOMToolboxApp;
import at.tuwien.ifs.somtoolbox.apps.config.AbstractOptionFactory;
import at.tuwien.ifs.somtoolbox.apps.config.OptionFactory;
import at.tuwien.ifs.somtoolbox.apps.viewer.RhythmPattern;
import at.tuwien.ifs.somtoolbox.data.DataDimensionException;
import at.tuwien.ifs.somtoolbox.data.InputData;
import at.tuwien.ifs.somtoolbox.data.InputDataFactory;
import at.tuwien.ifs.somtoolbox.data.InputDatum;
import at.tuwien.ifs.somtoolbox.util.FileUtils;
import at.tuwien.ifs.somtoolbox.util.StdErrProgressWriter;
import at.tuwien.ifs.somtoolbox.util.StringUtils;

/**
 * Provides a batch mode to save rhythm patterns matrix visualisation of all inputs contained in a vector file.
 * 
 * @author Rudolf Mayer
 * @version $Id: RhythmPatternsMatrixVisualisationSaver.java 4312 2014-01-10 13:07:12Z mayer $
 */
public class RhythmPatternsMatrixVisualisationSaver implements SOMToolboxApp {

    public static final Parameter[] OPTIONS = { OptionFactory.getOptInputVectorFile(true),
            OptionFactory.getOptWidth(false), OptionFactory.getOptImageFileType(false),
            OptionFactory.getOptDrawUnitGrid(false), OptionFactory.getOptBaseName(false), };

    public static final String DESCRIPTION = "Save rhythm patterns matrix visualisation of an input file as images to a file.";

    public static final String LONG_DESCRIPTION = "Provides a batch mode to save rhythm patterns matrix visualisation of all inputs in a vector file to image files.";

    public static final Type APPLICATION_TYPE = Type.Utils;

    public static void main(String[] args) {

        JSAPResult res = OptionFactory.parseResults(args, OPTIONS);

        String vFile = AbstractOptionFactory.getFilePath(res, "inputVectorFile");
        String ftype = res.getString("filetype");
        boolean unitGrid = res.getBoolean("unitGrid");
        int unitW = res.getInt("width", 10);

        String basename = res.getString("basename");
        if (basename == null) {
            basename = FileUtils.extractSOMLibInputPrefix(vFile) + "_images";
        }
        if (!basename.endsWith(File.separator)) {
            basename += File.separator;
        }

        InputData data = InputDataFactory.open(vFile);

        File outputDir = new File(basename);
        Logger.getLogger("at.tuwien.ifs.somtoolbox").info(
                "Output dir " + outputDir.getAbsolutePath() + (outputDir.mkdirs() ? " created" : " existing"));
        if (!outputDir.isDirectory()) {
            Logger.getLogger("at.tuwien.ifs.somtoolbox").severe(
                    "Output path '" + outputDir.getAbsolutePath() + "' is not a directory! Aborting.");
            return;
        }

        StdErrProgressWriter progress = new StdErrProgressWriter(data.numVectors(), "Creating RP image ", 10);
        for (int i = 0; i < data.numVectors(); i++) {
            InputDatum inputDatum = data.getInputDatum(i);
            try {
                RhythmPattern r;
                if (data.getFeatureMatrixColumns() != -1) {
                    r = new RhythmPattern(inputDatum.getVector(), data.getFeatureMatrixColumns(),
                            data.getFeatureMatrixRows());
                } else {
                    r = new RhythmPattern(inputDatum.getVector());
                }
                BufferedImage image = r.getImage();

                File out = new File(basename
                        + StringUtils.escapeString(inputDatum.getLabel().replace("/", "_").replace("\\", "_")) + "."
                        + ftype);
                ImageIO.write(image, ftype, out);
                progress.progress();
            } catch (DataDimensionException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

    }

}
