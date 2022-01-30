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
package at.tuwien.ifs.somtoolbox.properties;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Properties;
import java.util.Vector;
import java.util.logging.Logger;

import org.apache.commons.lang.StringUtils;

import com.martiansoftware.jsap.FlaggedOption;
import com.martiansoftware.jsap.JSAP;

import at.tuwien.ifs.somtoolbox.data.InputData;
import at.tuwien.ifs.somtoolbox.data.TemplateVector;
import at.tuwien.ifs.somtoolbox.layers.Layer.GridLayout;
import at.tuwien.ifs.somtoolbox.layers.Layer.GridTopology;
import at.tuwien.ifs.somtoolbox.layers.metrics.L2Metric;
import at.tuwien.ifs.somtoolbox.layers.quality.QuantizationError;

/**
 * Properties for SOM training.
 * 
 * @author Michael Dittenbach
 * @author Rudolf Mayer
 * @version $Id: SOMProperties.java 4327 2014-12-29 21:48:43Z mayer $
 */
public class SOMProperties extends Properties {
    public enum SelectedClassMode {
        NORMAL, EXCEPT, FAVOUR
    }

    public static final FlaggedOption OPTION_XSIZE = PropertyUtils.getIntegerOption("xSize",
            "size of the map in x direction");

    public static final FlaggedOption OPTION_YSIZE = PropertyUtils.getIntegerOption("ySize",
            "size of the map in y direction");

    public static final FlaggedOption OPTION_ZSIZE = PropertyUtils.getIntegerOption("zSize", 1, false,
            "size of the map in z direction; if ommitted or set to 1, a 2-D SOM will be created.");

    public static final FlaggedOption OPTION_LEARNRATE = PropertyUtils.getDoubleOption("learnRate", 0.75, false,
            "the learning rate (alpha)");

    public static final FlaggedOption OPTION_SIGMA = PropertyUtils.getIntegerOption("sigma", false,
            "the neighbourhood size (sigma / hc_i)");

    public static final FlaggedOption OPTION_NUM_ITERATIONS = PropertyUtils.getIntegerOption("numIterations", false,
            "number of training iterations; required, unless " + "numCycles" + " is given.");

    public static final FlaggedOption OPTION_NUM_CYCLES = PropertyUtils.getIntegerOption("numCycles", false,
            "number of training cycles; required, unless " + OPTION_NUM_ITERATIONS.getID() + " is given.");

    public static final FlaggedOption OPTION_METRIC_NAME = PropertyUtils.getStringOption("metricName",
            L2Metric.class.getCanonicalName(), false, "the distance metric to be used for vector comparison");

    public static final FlaggedOption OPTION_TAU = PropertyUtils.getDoubleOption("tau", 1, false,
            "the tau parameter for the Growing Grid and GHSOM models");

    public static final FlaggedOption OPTION_GROWTH_QUALITY = PropertyUtils.getStringOption("growthQualityMeasureName",
            "QuantizationError.mqe", false,
            "the measure used to determin whether growth is still needed, e.g. in the Growing Grid");

    public static final FlaggedOption[] SOM_TRAINING_OPTIONS = { OPTION_XSIZE, OPTION_YSIZE, OPTION_ZSIZE,//
            OPTION_LEARNRATE, OPTION_SIGMA, OPTION_NUM_ITERATIONS, OPTION_NUM_CYCLES,//
            OPTION_METRIC_NAME, //
            OPTION_TAU, OPTION_GROWTH_QUALITY //
    };

    public static final FlaggedOption OPTION_BATCH_SOM = PropertyUtils.getBooleanOption("batchSOM", false, false,
            "whether to use the batch learning process");

    public static final FlaggedOption OPTION_BATCH_SOM_NEIGHBOUR_WIDTH = PropertyUtils.getIntegerOption(
            "neighbour_width", 3, false, "the neighbour width for the batch training");

    public static final FlaggedOption[] SOM_BATCH_OPTIONS = { OPTION_BATCH_SOM, OPTION_BATCH_SOM_NEIGHBOUR_WIDTH };

    public static final FlaggedOption OPTION_USE_PCA = PropertyUtils.getBooleanOption("usePCA", false, false,
            "whether to use PCA initialisation");

    public static final FlaggedOption OPTION_DUMP_EVERY = PropertyUtils.getIntegerOption("dumpEvery", -1, false,
            "COMMENT ME!");

    public static final FlaggedOption OPTION_ADAPTIVE_COORDINATES_THRESHOLD = PropertyUtils.getStringOption(
            "adaptiveCoordinatesThreshold", JSAP.NO_DEFAULT, false, "COMMENT ME!");

    public static final FlaggedOption OPTION_MINIMUM_FEATURE_DENSITY = PropertyUtils.getIntegerOption(
            "minimumFeatureDensity", false, "COMMENT ME!");

    public static final FlaggedOption OPTION_DATUM_TO_UNIT_MAPPING = PropertyUtils.getStringOption(
            "datumToUnitMappings", JSAP.NO_DEFAULT, false, "COMMENT ME!");

    public static final FlaggedOption OPTION_CLASS_SELECTION_MODE = PropertyUtils.getStringOption("classselectionmode",
            JSAP.NO_DEFAULT, false, "COMMENT ME!");

    public static final FlaggedOption OPTION_CLASSES_SELECTED = PropertyUtils.getStringOption("selectedClasses",
            JSAP.NO_DEFAULT, false, "COMMENT ME!");

    public static final FlaggedOption OPTION_CLASSINFOFILE = PropertyUtils.getStringOption("classInfoFileName",
            JSAP.NO_DEFAULT, false, "the name of the class information file");

    public static final FlaggedOption[] SOM_OTHERS = { OPTION_USE_PCA, OPTION_DUMP_EVERY,
            OPTION_ADAPTIVE_COORDINATES_THRESHOLD, OPTION_MINIMUM_FEATURE_DENSITY, OPTION_DATUM_TO_UNIT_MAPPING,
            OPTION_CLASS_SELECTION_MODE, OPTION_CLASSES_SELECTED, OPTION_CLASSINFOFILE };

    public static final FlaggedOption OPTION_GRID_LAYOUT = PropertyUtils.getStringOption("gridLayout",
            GridLayout.rectangular.toString(), false,
            "One of " + at.tuwien.ifs.somtoolbox.util.StringUtils.toString(GridLayout.values(), "", ""));

    public static final FlaggedOption OPTION_GRID_TOPOLOGY = PropertyUtils.getStringOption("gridTopology",
            GridTopology.planar.toString(), false,
            "One of " + at.tuwien.ifs.somtoolbox.util.StringUtils.toString(GridTopology.values(), "", ""));

    public static final FlaggedOption[] SOM_GRID_OPTIONS = { OPTION_GRID_LAYOUT, OPTION_GRID_TOPOLOGY };

    public static final String WORKING_DIRECTORY = FileProperties.OPTION_WORKING_DIRECTORY.getID();

    private static final String METRIC_PACKAGE = L2Metric.class.getPackage().getName() + ".";

    public static final String QUALITY_PACKAGE = QuantizationError.class.getPackage().getName() + ".";

    public static final double defaultLearnRate = Double.parseDouble(OPTION_LEARNRATE.getDefault()[0]);

    public static final String propertiesFileNameSuffix = ".prop";

    public class DatumToUnitMapping {
        public String label;

        public int unitX, unitY;
    }

    private static final long serialVersionUID = 1L;

    // private int expansionCheckCycles = 0;

    // private int expansionCheckIterations = 0;

    private boolean batchSom = false;

    private int neighbourWidth = 3;

    private double learnrate = 0;

    private String metricName = null;

    private String growthQualityMeasureName = null;

    private int numCycles = 0;

    private int numIterations = 0;

    /** TODO: move to {@link FileProperties} */
    private int dumpEvery = -1;

    /**
     * Default = -1 --> do not dump.
     * 
     * @return iteration % dumpEvery == 0 --> dump
     */
    public int getDumpEvery() {
        return dumpEvery;
    }

    private long randomSeed = -1;

    private double sigma = -1;

    private double tau = 1;

    private int xSize = 0;

    private int ySize = 0;

    private int zSize = 0;

    private GridTopology gridTopology = GridTopology.planar;

    private GridLayout gridLayout = GridLayout.rectangular;

    private boolean usePCA = false;

    private Vector<DatumToUnitMapping> datumToUnitMappings = new Vector<DatumToUnitMapping>();

    /* Angela: training exceptions */
    private ArrayList<String> selectedClasses = null;

    private String classInfoFileName = null;

    private SelectedClassMode selectedClassMode = SelectedClassMode.NORMAL;

    private int minimumFeatureDensity = -1;

    private double[] adaptiveCoordinatesThreshold;

    /* Jakob: dumpEvery */
    public SOMProperties(int xSize, int ySize, int zSize, long seed, int trainingCycles, int trainingIterations,
            int dumpEvery, double lernrate, double sigma, double tau, String metric, boolean usePCA)
            throws PropertiesException {
        this(xSize, ySize, zSize, seed, trainingCycles, trainingIterations, lernrate, sigma, tau, metric, usePCA);
        this.dumpEvery = dumpEvery;
    }

    /* Jakob: 3D (zSize) */
    public SOMProperties(int xSize, int ySize, int zSize, long seed, int trainingCycles, int trainingIterations,
            double lernrate, double sigma, double tau, String metric, boolean usePCA) throws PropertiesException {
        this(xSize, ySize, seed, trainingCycles, trainingIterations, lernrate, sigma, tau, metric, usePCA);
        this.zSize = zSize;
    }

    public SOMProperties(int xSize, int ySize, int numIterations, double lernrate) throws PropertiesException {
        this.xSize = xSize;
        this.ySize = ySize;
        this.numIterations = numIterations;
        this.learnrate = lernrate;
    }

    public SOMProperties(int xSize, int ySize, long seed, int numCycles, int numIterations, double learnrate,
            double sigma, double tau, String metricName, boolean usePCA) throws PropertiesException {
        this(xSize, ySize, numIterations, learnrate);
        this.zSize = 1;
        this.tau = tau;
        this.metricName = metricName;
        this.numCycles = numCycles;
        this.growthQualityMeasureName = OPTION_GROWTH_QUALITY.getDefault()[0];
        this.sigma = sigma;
        this.randomSeed = -1;
        this.usePCA = usePCA;
        validatePropertyValues();
    }

    /**
     * Loads and encapsulated properties for the SOM training process.
     * 
     * @param fname name of the properties file.
     * @throws PropertiesException thrown if properties file could not be opened or the values of the properties are
     *             illegal.
     */
    public SOMProperties(String fname) throws PropertiesException {
        try {
            load(new FileInputStream(fname));
        } catch (Exception e) {
            throw new PropertiesException("Could not open properties file " + fname);
        }
        parse();
    }

    public SOMProperties(Properties properties) throws PropertiesException {
        putAll(properties);
        parse();
    }

    private void parse() throws PropertiesException {
        try {
            usePCA = StringUtils.equals(getProperty(OPTION_USE_PCA.getID()), OPTION_USE_PCA.getDefault()[0]);
            if (usePCA) {
                Logger.getLogger("at.tuwien.ifs.somtoolbox").info("Using PCA initialisation.");
            }

            // TODO: this has to be moved to some external file most likely
            if (getProperty(OPTION_DATUM_TO_UNIT_MAPPING.getID()) != null) {
                java.util.StringTokenizer st = new java.util.StringTokenizer(
                        getProperty(OPTION_DATUM_TO_UNIT_MAPPING.getID()));

                while (st.hasMoreTokens()) {
                    DatumToUnitMapping mapping = new DatumToUnitMapping();

                    mapping.label = st.nextToken();
                    mapping.unitX = Integer.parseInt(st.nextToken());
                    mapping.unitY = Integer.parseInt(st.nextToken());

                    System.out.println("\n   *** Adding datum to unit mapping, datum label: " + mapping.label
                            + "unit x, y: " + mapping.unitX + ", " + mapping.unitY);

                    datumToUnitMappings.add(mapping);
                }
            }

            // ...
            neighbourWidth = Integer.parseInt(getProperty(OPTION_BATCH_SOM_NEIGHBOUR_WIDTH.getID(),
                    OPTION_BATCH_SOM_NEIGHBOUR_WIDTH.getDefault()[0]));
            batchSom = Boolean.parseBoolean(getProperty(OPTION_BATCH_SOM.getID(), OPTION_BATCH_SOM.getDefault()[0]));
            if (batchSom) {
                Logger.getLogger("at.tuwien.ifs.somtoolbox").info("Training in Batch-SOM mode");
            } else {
                Logger.getLogger("at.tuwien.ifs.somtoolbox").info("Training incrementaly");
            }

            this.xSize = Integer.parseInt(getProperty(OPTION_XSIZE.getID(), "0"));
            this.ySize = Integer.parseInt(getProperty(OPTION_YSIZE.getID(), "0"));
            this.zSize = Integer.parseInt(getProperty(OPTION_ZSIZE.getID(), OPTION_ZSIZE.getDefault()[0]));

            this.learnrate = Double.parseDouble(getPropertyAndLog(OPTION_LEARNRATE.getID(),
                    OPTION_LEARNRATE.getDefault()[0]));
            this.sigma = Double.parseDouble(getProperty(OPTION_SIGMA.getID(), "-1"));

            this.metricName = getProperty(OPTION_METRIC_NAME.getID());
            this.growthQualityMeasureName = getProperty(OPTION_GROWTH_QUALITY.getID());

            if (getProperty(FileProperties.OPTION_RANDOM_SEED.getID()) == null) {
                randomSeed = Long.parseLong(FileProperties.OPTION_RANDOM_SEED.getDefault()[0]);
                Logger.getLogger("at.tuwien.ifs.somtoolbox").warning(
                        "No " + FileProperties.OPTION_RANDOM_SEED.getID() + " given. Defaulting to " + randomSeed + ".");
            } else {
                randomSeed = Long.parseLong(getProperty(FileProperties.OPTION_RANDOM_SEED.getID()));
            }

            this.numIterations = Integer.parseInt(getProperty(OPTION_NUM_ITERATIONS.getID(), "0"));
            this.numCycles = Integer.parseInt(getProperty(OPTION_NUM_CYCLES.getID(), "0"));
            this.dumpEvery = Integer.parseInt(getProperty(OPTION_DUMP_EVERY.getID(), "-1"));
            this.tau = Double.parseDouble(getProperty(OPTION_TAU.getID(), OPTION_TAU.getDefault()[0]));

            // parameter for adaptive coordinate visualization
            if (getProperty(OPTION_ADAPTIVE_COORDINATES_THRESHOLD.getID()) != null) {
                adaptiveCoordinatesThreshold = at.tuwien.ifs.somtoolbox.util.StringUtils.parseDoublesAndRanges(getProperty(OPTION_ADAPTIVE_COORDINATES_THRESHOLD.getID()));
            }

            /* Angela: for training selection (excepting or favouring) */
            String selectedClassesString = getProperty(OPTION_CLASSES_SELECTED.getID());
            if (selectedClassesString != null) {
                selectedClasses = new ArrayList<String>();
                String[] selectedClassesTmp = selectedClassesString.split(",");
                for (String element : selectedClassesTmp) {
                    selectedClasses.add(element.trim());
                }
            }
            classInfoFileName = getProperty(OPTION_CLASSINFOFILE.getID(), null);
            if (classInfoFileName != null && getProperty(WORKING_DIRECTORY, null) != null) {
                String tmpStr = getProperty(WORKING_DIRECTORY, null).concat(File.separator).concat(classInfoFileName);
                classInfoFileName = tmpStr;
            }
            if (getProperty(OPTION_CLASS_SELECTION_MODE.getID()) != null) {
                selectedClassMode = SelectedClassMode.valueOf(getProperty(OPTION_CLASS_SELECTION_MODE.getID()));
            }
            minimumFeatureDensity = Integer.parseInt(getProperty(OPTION_MINIMUM_FEATURE_DENSITY.getID(), "-1"));

            validatePropertyValues();

            // FIXME: find a generic method to parse enums w/o repeating the code...
            try {
                gridLayout = GridLayout.valueOf(getProperty(OPTION_GRID_LAYOUT.getID(),
                        OPTION_GRID_LAYOUT.getDefault()[0]));
            } catch (Exception e) {
                throw new PropertiesException("Illegal value '" + getProperty(OPTION_GRID_LAYOUT.getID())
                        + "' for property '" + OPTION_GRID_LAYOUT.getID() + "', valid options are: "
                        + Arrays.toString(GridLayout.values()));
            }
            try {
                gridTopology = GridTopology.valueOf(getProperty(OPTION_GRID_TOPOLOGY.getID(),
                        OPTION_GRID_TOPOLOGY.getDefault()[0]));
            } catch (Exception e) {
                throw new PropertiesException("Illegal value '" + getProperty(OPTION_GRID_TOPOLOGY.getID())
                        + "' for property '" + OPTION_GRID_TOPOLOGY.getID() + "', valid options are: "
                        + Arrays.toString(GridTopology.values()));
            }
        } catch (NumberFormatException e) {
            throw new PropertiesException("Illegal numeric value '" + e + "' in properties file.");
        }
    }

    private String getPropertyAndLog(String key, String defaultValue) {
        Logger.getLogger("at.tuwien.ifs.somtoolbox").warning(key + " is missing. Defaulting to " + defaultValue);
        return getProperty(key, String.valueOf(defaultValue));
    }

    private void validatePropertyValues() throws PropertiesException {
        if (xSize <= 0 || ySize <= 0) {
            throw new PropertiesException("Either " + OPTION_XSIZE.getID() + " or " + OPTION_YSIZE.getID()
                    + " is less than or equal zero.");
        }
        if (zSize > 1) {
            Logger.getLogger("at.tuwien.ifs.somtoolbox").info(OPTION_ZSIZE.getID() + " > 1 --> Training 3D-SOM");
        }
        if (learnrate <= 0) {
            throw new PropertiesException(OPTION_LEARNRATE.getID() + " is less than or equal zero or missing.");
        }

        if (sigma <= 0) {
            // FIXME: find a good initial value for sigma (neighbourhood radius)
            // old equation from michael, produces too small radii !!
            // sigma = Math.sqrt((-1 * Math.pow(0.375 * Math.max(xSize, ySize), 2)) / (2 * Math.log(0.3)));

            // new equation, inspired by e.g.
            // http://xmipp.cnb.csic.es/NewXmipp/Web_Site/public_html/NewXmipp/Applications/Src/SOM/Help/som.html and
            // http://www.spatialanalysisonline.com/output/html/SOMunsupervisedclassificationofhyper-spectralimagedata.html
            // initial radius = span whole map! we take a slightly smaller radius here
            // sigma = Math.max(xSize, ySize) * 0.9;

            // newest method, inspired by ESOM
            // http://databionic-esom.sourceforge.net/user.html#Training_Parameters: should be on the order of half the
            // smaller length of the grid.
            // special handling of 1-dimensional maps => we take half of the longer axis
            if (xSize == 1 || ySize == 1) {
                sigma = Math.min(xSize, ySize) / 2d;
            } else {
                sigma = Math.max(xSize, ySize) / 2d;
            }
            Logger.getLogger("at.tuwien.ifs.somtoolbox").warning(
                    OPTION_SIGMA.getID() + " is missing or negative. Defaulting to "
                            + at.tuwien.ifs.somtoolbox.util.StringUtils.format(sigma, 2) + " for a map of size "
                            + xSize + "x" + ySize);
        }
        if (tau == 1) {
            Logger.getLogger("at.tuwien.ifs.somtoolbox").info(OPTION_TAU.getID() + " = 1 implies fix-sized layer");
        } else if (tau <= 0 || tau > 1) {
            throw new PropertiesException(OPTION_TAU.getID() + " less than or equal zero or greater than 1.");
        }
        if (StringUtils.isBlank(metricName)) {
            metricName = OPTION_METRIC_NAME.getDefault()[0];
            Logger.getLogger("at.tuwien.ifs.somtoolbox").warning(
                    "No " + OPTION_METRIC_NAME.getID() + " given. Defaulting to " + metricName + ".");
        } else if (!metricName.startsWith(METRIC_PACKAGE)) {
            metricName = METRIC_PACKAGE + metricName;
        }
        if (StringUtils.isBlank(growthQualityMeasureName)) {
            growthQualityMeasureName = QUALITY_PACKAGE + OPTION_GROWTH_QUALITY.getDefault()[0];
            Logger.getLogger("at.tuwien.ifs.somtoolbox").warning(
                    "No " + OPTION_GROWTH_QUALITY.getID() + " given. Defaulting to " + growthQualityMeasureName);
        } else {
            growthQualityMeasureName = QUALITY_PACKAGE + growthQualityMeasureName;
        }

        if (numIterations <= 0 && numCycles <= 0) {
            throw new PropertiesException(OPTION_NUM_ITERATIONS.getID() + " and " + OPTION_NUM_CYCLES.getID()
                    + " are less than or equal zero or missing.\n" + "Provide either " + OPTION_NUM_ITERATIONS.getID()
                    + " or " + OPTION_NUM_CYCLES.getID() + " (multiple of # training data).");
        } else if (numIterations > 0 && numCycles > 0) {
            throw new PropertiesException(OPTION_NUM_ITERATIONS.getID() + " and " + OPTION_NUM_CYCLES.getID()
                    + " are mutually exclusive. Specify just one of them.");
        } else if (numIterations > 0 && numCycles <= 0) {
            numCycles = 0; // just to be sure
            if (tau == 1) {
                Logger.getLogger("at.tuwien.ifs.somtoolbox").info(
                        OPTION_NUM_ITERATIONS.getID() + " defines the fixed number of training iterations.");
            } else {
                Logger.getLogger("at.tuwien.ifs.somtoolbox").info(
                        OPTION_NUM_ITERATIONS.getID()
                                + " defines the number of iterations after which an expansion check is performed.");
            }
        } else if (numIterations <= 0 && numCycles > 0) {
            numIterations = 0; // just to be sure
            if (tau == 1) {
                Logger.getLogger("at.tuwien.ifs.somtoolbox").info(
                        OPTION_NUM_CYCLES.getID()
                                + " defines the fixed number of training cycles (multiples of the number of data).");
            } else {
                Logger.getLogger("at.tuwien.ifs.somtoolbox").info(
                        OPTION_NUM_CYCLES.getID()
                                + " defines the number of cycles (multiples of the number of data) after which an expansion check is performed.");
            }
        }
    }

    /**
     * Returns an ArrayList of Strings containing the class names which should be excluded from training.
     * 
     * @return classes to be excluded from the training
     */
    public ArrayList<String> getSelectedClasses() {
        return this.selectedClasses;
    }

    public SelectedClassMode getSelectedClassMode() {
        return selectedClassMode;
    }

    public String getClassInfoFileName() {
        return this.classInfoFileName;
    }

    /**
     * Returns the batch_som status.
     * 
     * @return the batch_som status.
     */
    public boolean batchSom() {
        return batchSom;
    }

    /**
     * Returns the neighbour_width.
     * 
     * @return the neighbour_width.
     */
    public int neighbourWidth() {
        return neighbourWidth;
    }

    /**
     * Returns the learnrate.
     * 
     * @return the learnrate.
     */
    public double learnrate() {
        return learnrate;
    }

    /**
     * Returns the name of the used metric.
     * 
     * @return the name of the used metric.
     */
    public String metricName() {
        return metricName;
    }

    /**
     * Returns the name of the used quality measure.
     * 
     * @return the name of the used quality measure.
     */
    public String growthQualityMeasureName() {
        return growthQualityMeasureName;
    }

    /**
     * Returns the number of training cycles.
     * 
     * @return the number of training cycles.
     */
    public int numCycles() {
        return numCycles;
    }

    /**
     * Returns the number of training iterations.
     * 
     * @return the number of training iterations.
     */
    public int numIterations() {
        return numIterations;
    }

    /**
     * Return the number of iterations really trained, either using {@link #numIterations} or {@link #numCycles},
     * whichever value is set.
     */
    public int trainedIterations(int numVectors) {
        if (numIterations() > 0) {
            return numIterations();
        } else {
            return numCycles() * numVectors;
        }
    }

    /**
     * Returns the random seed.<br/>
     * FIXME: this is a duplicate to {@link FileProperties#randomSeed()}
     * 
     * @return the random seed.
     */
    public long randomSeed() {
        return randomSeed;
    }

    /**
     * Returns sigma determining the neighbourhood radius.
     * 
     * @return sigma determining the neighbourhood radius.
     */
    public double sigma() {
        return sigma;
    }

    /**
     * Returns tau determining the desired data representation granularity.
     * 
     * @return tau determining the desired data representation granularity.
     */
    public double tau() {
        return tau;
    }

    /**
     * Returns the number of units in horizontal direction.
     * 
     * @return the number of units in horizontal direction.
     */
    public int xSize() {
        return xSize;
    }

    /**
     * Returns the number of units in vertical direction.
     * 
     * @return the number of units in vertical direction.
     */
    public int ySize() {
        return ySize;
    }

    /**
     * Returns the number of units in z-direction. Default is 1
     * 
     * @return the number of units in z-direction. Default is 1
     */
    public int zSize() {
        return zSize;
    }

    public int getMinimumFeatureDensity() {
        return minimumFeatureDensity;
    }

    public boolean pca() {
        return usePCA;
    }

    public GridTopology getGridTopology() {
        return gridTopology;
    }

    public GridLayout getGridLayout() {
        return gridLayout;
    }

    public Vector<DatumToUnitMapping> datumToUnitMappings() {
        return datumToUnitMappings;
    }

    /** Writes the properties to a file. */
    public void writeToFile(String dataName, String outputDir, boolean normalised) throws IOException {
        String[] split = dataName.split(File.separator);
        String name = split[split.length - 1];
        String fileName = dataName + propertiesFileNameSuffix;

        // we are not using the Properties.store() method, as we want to guarantee a certain order in the file
        PrintWriter writer = at.tuwien.ifs.somtoolbox.util.FileUtils.openFileForWriting("SOM Properties File",
                fileName, false);

        writer.println(FileProperties.OPTION_OUTPUT_DIRECTORY.getID() + "=" + (outputDir != null ? outputDir : name));
        writer.println(WORKING_DIRECTORY + "=.");
        writer.println(FileProperties.OPTION_NAME_PREFIX.getID() + "=" + name);
        writer.println();
        writer.println(FileProperties.OPTION_INPUT_VECTOR.getID() + "=" + name + InputData.inputFileNameSuffix);
        writer.println(FileProperties.OPTION_TEMPLATE_VECTOR.getID() + "=" + name
                + TemplateVector.templateFileNameSuffix);
        writer.println();
        writer.println(FileProperties.OPTION_NORMALIZED.getID() + "=" + normalised);
        writer.println(FileProperties.OPTION_RANDOM_SEED.getID() + "=" + randomSeed());
        writer.println();
        if (StringUtils.isNotBlank(metricName) && !metricName.equals(OPTION_METRIC_NAME.getDefault()[0])) {
            writer.println(OPTION_METRIC_NAME.getID() + metricName);
            writer.println();
        }
        writer.println(OPTION_XSIZE.getID() + "=" + xSize);
        writer.println(OPTION_YSIZE.getID() + "=" + ySize);
        if (zSize > 1) {
            writer.println(OPTION_ZSIZE.getID() + "=" + zSize);
        }
        writer.println();
        writer.println(OPTION_LEARNRATE.getID() + "=" + learnrate);
        if (numCycles > 0) {
            writer.println(OPTION_NUM_CYCLES.getID() + "=" + numCycles);
        } else {
            writer.println(OPTION_NUM_ITERATIONS.getID() + "=" + numIterations);
        }
        writer.flush();
        writer.close();
    }

    public double[] adaptiveCoordinatesTreshold() {
        return adaptiveCoordinatesThreshold;
    }
}
