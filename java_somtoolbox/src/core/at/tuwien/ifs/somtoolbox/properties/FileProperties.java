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
import java.util.Properties;
import java.util.logging.Logger;

import org.apache.commons.lang.StringUtils;

import com.martiansoftware.jsap.FlaggedOption;
import com.martiansoftware.jsap.JSAP;
import com.martiansoftware.jsap.ParseException;

/**
 * Properties for I/O stuff.
 * 
 * @author Michael Dittenbach
 * @version $Id: FileProperties.java 4305 2014-01-09 12:22:20Z mayer $
 */
public class FileProperties extends Properties {

    public static final FlaggedOption OPTION_WORKING_DIRECTORY = PropertyUtils.getStringOption("workingDirectory", "."
            + File.separator, false, "the directory with your data files");

    public static final FlaggedOption OPTION_OUTPUT_DIRECTORY = PropertyUtils.getStringOption("outputDirectory", "."
            + File.separator, false, "directory where files will be created");

    public static final FlaggedOption OPTION_NAME_PREFIX = PropertyUtils.getStringOption("namePrefix",
            "will be used as prefix in the SOM files created");

    public static final FlaggedOption OPTION_INPUT_VECTOR = PropertyUtils.getStringOption("vectorFileName",
            "name of normalized input vector file containing the training data");

    public static final FlaggedOption OPTION_TEMPLATE_VECTOR = PropertyUtils.getStringOption("templateFileName",
            JSAP.NO_DEFAULT, false,
            "name of template vector file (describing the component names); if absent, components will be named 'component_i'.");

    public static final FlaggedOption OPTION_SPARSE_DATA = PropertyUtils.getBooleanOption("sparseData", true, false,
            "use yes if vectors are sparse (e.g. text data), no if vectors are not sparse (audio!)");

    public static final FlaggedOption OPTION_NORMALIZED = PropertyUtils.getBooleanOption("isNormalized", true, false,
            "whether vectorFile has been previously normalized; currently not used");

    public static final FlaggedOption OPTION_RANDOM_SEED = PropertyUtils.getIntegerOption("randomSeed", 7, false,
            "Seed value for random number generator; reusing the same value ensures repeatability of the training process.");

    public static final FlaggedOption OPTION_NUM_CACHE_BLOCKS = PropertyUtils.getIntegerOption("numCacheBlocks", 1,
            false, "How many blocs to divide the training data to; 1 means all data will be read at once.");

    public static final FlaggedOption OPTION_USE_DB = PropertyUtils.getBooleanOption("useDatabase", true, false,
            "enables reading input and template vector from the database");

    public static final FlaggedOption OPTION_DB_SERVER = PropertyUtils.getStringOption("databaseServerAddress",
            "localhost", false, "server IP or name");

    public static final FlaggedOption OPTION_DB_NAME = PropertyUtils.getStringOption("databaseName", "localhost",
            false, "name of the database to use");

    public static final FlaggedOption OPTION_DB_USER = PropertyUtils.getStringOption("databaseUser", "root", false,
            "username to access the database");

    public static final FlaggedOption OPTION_DB_PASSWORD = PropertyUtils.getStringOption("databasePassword", "", false,
            "password to access the database");

    public static final FlaggedOption OPTION_DB_TABLE_PREFIX = PropertyUtils.getStringOption("databaseTableNamePrefix",
            "localhost", false, "prefix to prepend to all tables generated; should be changed for each training");

    public static final FlaggedOption[] FILE_OPTIONS = { OPTION_WORKING_DIRECTORY, OPTION_OUTPUT_DIRECTORY,
            OPTION_NAME_PREFIX,//
            OPTION_INPUT_VECTOR, OPTION_TEMPLATE_VECTOR, OPTION_SPARSE_DATA, OPTION_NORMALIZED,//
            OPTION_RANDOM_SEED, OPTION_NUM_CACHE_BLOCKS, };

    public static final FlaggedOption[] DATABASE_OPTIONS = { OPTION_USE_DB, OPTION_DB_SERVER, OPTION_DB_NAME,
            OPTION_DB_USER, OPTION_DB_PASSWORD, OPTION_DB_TABLE_PREFIX };

    private static final long serialVersionUID = 1L;

    private boolean isNormalized = true;

    private String namePrefix = null;

    private int numCacheBlocks = 0;

    private String outputDirectory = null;

    private long randomSeed = -1;

    private boolean sparseData = true;

    private String templateFileName = null;

    private String vectorFileName = null;

    private String workingDirectory = null;

    // Database-Properties
    private boolean usingDatabase = false;

    private String databaseServerAddress = null;

    private String databaseName = null;

    private String databaseUser = null;

    private String databasePassword = null;

    private String databaseTableNamePrefix = null;

    private String sourceFileName;

    /**
     * Loads and encapsulated properties related to the input data.
     * 
     * @param fname Name of the properties file.
     */
    public FileProperties(String fname) throws PropertiesException {
        this.sourceFileName = fname;
        try {
            load(new FileInputStream(fname));
        } catch (Exception e) {
            throw new PropertiesException("Could not open properties file " + fname);
        }
        parse();
    }

    public FileProperties(Properties properties) throws PropertiesException {
        putAll(properties);
        parse();
    }

    private void parse() throws PropertiesException {
        try {
            workingDirectory = getProperty(OPTION_WORKING_DIRECTORY.getID());
            if (StringUtils.isBlank(workingDirectory)) {
                workingDirectory = OPTION_WORKING_DIRECTORY.getDefault()[0];
                Logger.getLogger("at.tuwien.ifs.somtoolbox").warning(
                        "No workingDirectory given. Defaulting to '" + workingDirectory + "' ("
                                + new File(workingDirectory).getAbsolutePath() + ")");
            } else if (workingDirectory.trim().equals(".") || workingDirectory.trim().startsWith("./")) {
                String msg = "Relative workingDirectory '" + workingDirectory + "' given. Expanding to '";
                String parentDirectory = new File(sourceFileName).getParentFile().getAbsolutePath();
                workingDirectory = workingDirectory.replace(".", parentDirectory);
                Logger.getLogger("at.tuwien.ifs.somtoolbox").info(msg + workingDirectory + "'.");
            }
            if (!workingDirectory.endsWith(File.separator)) {
                workingDirectory += File.separator;
            }

            outputDirectory = getProperty(OPTION_OUTPUT_DIRECTORY.getID());
            if (outputDirectory == null) {
                outputDirectory = OPTION_OUTPUT_DIRECTORY.getDefault()[0];
                Logger.getLogger("at.tuwien.ifs.somtoolbox").warning(
                        "No outputDirectory given. Defaulting to '" + outputDirectory + "'.");
            } else {
                if (!outputDirectory.endsWith(File.separator)) {
                    outputDirectory += File.separator;
                }
            }

            // create outputdirectory if not existing
            File outputDir = new File(outputDirectory());
            if (!outputDir.exists()) {
                Logger.getLogger("at.tuwien.ifs.somtoolbox").info(
                        "Output directory '" + outputDir.getAbsolutePath() + "' does not exist. Trying to create it.");
                Logger.getLogger("at.tuwien.ifs.somtoolbox").info(
                        "Successfully created output directory: " + outputDir.mkdirs());
            }

            namePrefix = getProperty(OPTION_NAME_PREFIX.getID());
            if (namePrefix == null) {
                throw new PropertiesException("No namePrefix given.");
            } else {
                namePrefix = namePrefix.trim();
            }
            vectorFileName = getProperty(OPTION_INPUT_VECTOR.getID());
            if (vectorFileName == null) {
                throw new PropertiesException("No vectorFileName given.");
            }
            templateFileName = getProperty(OPTION_TEMPLATE_VECTOR.getID());
            if (templateFileName == null) {
                // throw new PropertiesException("No templateFileName given.");
                Logger.getLogger("at.tuwien.ifs.somtoolbox").warning(
                        "No templateFileName given. Using default template vector.");
            }
            String sparseDataStr = getProperty(OPTION_SPARSE_DATA.getID());
            if (sparseDataStr == null) {
                sparseData = Boolean.parseBoolean(OPTION_SPARSE_DATA.getDefault()[0]);
                Logger.getLogger("at.tuwien.ifs.somtoolbox").severe(
                        "No sparsity information given. Using '" + sparseData + "'.");
            } else {
                try {
                    sparseData = (Boolean) OPTION_SPARSE_DATA.getStringParser().parse(sparseDataStr);
                } catch (ParseException e) {
                    throw new PropertiesException("Sparsity value '" + sparseData + "' invalid. Use true or false.");
                }
            }
            String normStr = getProperty(OPTION_NORMALIZED.getID());
            if (normStr == null) {
                isNormalized = Boolean.parseBoolean(OPTION_NORMALIZED.getDefault()[0]);
                Logger.getLogger("at.tuwien.ifs.somtoolbox").severe(
                        "No normalization information given. Using '" + isNormalized + "'.");
            } else {
                try {
                    isNormalized = (Boolean) OPTION_NORMALIZED.getStringParser().parse(sparseDataStr);
                } catch (ParseException e) {
                    throw new PropertiesException("Normalization value '" + sparseData
                            + "' invalid. Use true or false.");
                }
            }
            String rs = getProperty(OPTION_RANDOM_SEED.getID());
            if (rs == null) {
                randomSeed = Long.parseLong(OPTION_RANDOM_SEED.getDefault()[0]);
                Logger.getLogger("at.tuwien.ifs.somtoolbox").warning(
                        "No " + OPTION_RANDOM_SEED.getID() + " given. Defaulting to " + randomSeed + ".");
            } else {
                randomSeed = Long.parseLong(rs);
            }
            String cs = getProperty(OPTION_NUM_CACHE_BLOCKS.getID());
            if (cs == null) {
                numCacheBlocks = Integer.parseInt(OPTION_NUM_CACHE_BLOCKS.getDefault()[0]);
                Logger.getLogger("at.tuwien.ifs.somtoolbox").warning(
                        "No " + OPTION_NUM_CACHE_BLOCKS.getID()
                                + " given. Reading the data en bloc. Can be problematic with large input data.");
            } else {
                numCacheBlocks = Integer.parseInt(cs);
            }

            // Should a database be used?
            String database = getProperty(OPTION_USE_DB.getID());
            if (database != null && database.equals("true")) {
                Logger.getLogger("at.tuwien.ifs.somtoolbox").info("Database found. Proceeding in database mode.");
                this.usingDatabase = true;

                if (StringUtils.isBlank(getProperty(OPTION_DB_SERVER.getID()))) {
                    databaseServerAddress = OPTION_DB_SERVER.getDefault()[0];
                    Logger.getLogger("at.tuwien.ifs.somtoolbox").info(
                            "No " + OPTION_DB_SERVER.getID() + " given. Defaulting to '" + databaseServerAddress + "'");
                } else {
                    databaseServerAddress = getProperty(OPTION_DB_SERVER.getID());
                }

                String databaseNameStr = getProperty(OPTION_DB_NAME.getID());
                if (databaseNameStr == null || databaseNameStr.equals("")) {
                    throw new PropertiesException("No " + OPTION_NAME_PREFIX.getID() + " given. Aborting");
                } else {
                    databaseName = databaseNameStr;
                }

                if (StringUtils.isBlank(getProperty(OPTION_DB_PASSWORD.getID()))) {
                    databasePassword = OPTION_DB_SERVER.getDefault()[0];
                    Logger.getLogger("at.tuwien.ifs.somtoolbox").info(
                            "No " + OPTION_DB_PASSWORD.getID() + " given. Defaulting to '" + databasePassword + "' ");
                } else {
                    databasePassword = getProperty(OPTION_DB_PASSWORD.getID());
                }

                if (StringUtils.isBlank(getProperty(OPTION_DB_TABLE_PREFIX.getID()))) {
                    throw new PropertiesException("No " + OPTION_DB_TABLE_PREFIX.getID() + " given. Aborting");
                } else {
                    databaseTableNamePrefix = getProperty(OPTION_DB_TABLE_PREFIX.getID());
                }

                if (StringUtils.isBlank(getProperty(OPTION_DB_USER.getID()))) {
                    databaseUser = OPTION_DB_PASSWORD.getDefault()[0];
                    Logger.getLogger("at.tuwien.ifs.somtoolbox").info(
                            "No databaseUser given. Defaulting to '" + databaseUser + "'");
                } else {
                    databaseUser = getProperty(OPTION_DB_USER.getID());
                }

            } else {
                // Logger.getLogger("at.tuwien.ifs.somtoolbox").info("No database specified. Proceeding in normal mode.");
                this.usingDatabase = false;
            }

        } catch (NumberFormatException e) {
            throw new PropertiesException("Illegal numeric value in properties file.");
        }
    }

    /**
     * Returns <code>true</code>, if the vectors are normalized to unit length. This information is used for map
     * creation to know when to normalize the units' weight vectors.
     * 
     * @return Returns <code>true</code>, if the vectors are normalized to unit length.
     */
    public boolean isNormalized() {
        return isNormalized;
    }

    /**
     * Returns the name of the test run.
     * 
     * @return the name of the test run.
     */
    public String namePrefix(boolean withPrefix) {
        if (withPrefix == true) {
            return workingDirectory + namePrefix;
        } else {
            return namePrefix;
        }
    }

    /**
     * Not used at the moment.
     * 
     * @return Returns the numCacheBlocks.
     */
    public int numCacheBlocks() {
        return numCacheBlocks;
    }

    /**
     * Returns the name of the output directory.
     * 
     * @return the name of the output directory.
     */

    public String outputDirectory() {
        return prependDirectory(true, outputDirectory, workingDirectory);
    }

    /**
     * Returns the random seed.
     * 
     * @return the random seed.
     */
    public long randomSeed() {
        return randomSeed;
    }

    /**
     * Returns <code>true</code> if the input data vectors are sparsely populated.
     * 
     * @return <code>true</code> if the input data vectors are sparsely populated.
     */
    public boolean sparseData() {
        return sparseData;
    }

    /**
     * Returns the name of the template vector file. The file name includes the working directory, if argument
     * <code>withPrefix</code> is true.
     * 
     * @param withPrefix determines if the file name is prefixed with the working directory.
     * @return the name of the template vector file.
     */
    public String templateFileName(boolean withPrefix) {
        return prependDirectory(withPrefix, templateFileName, workingDirectory);
    }

    private String prependDirectory(boolean withPrefix, String path, String dir) {
        if (path != null && withPrefix == true) {
            return prependDirectory(path, dir);
        } else {
            return path;
        }
    }

    private String prependDirectory(String path, String dir) {
        if (path.startsWith(File.separator) || new File(path).isAbsolute()) {
            return path;
        } else {
            return dir + path;
        }
    }

    /**
     * Returns the name of the input vector file. The file name includes the working directory, if argument
     * <code>withPrefix</code> is true.
     * 
     * @param withPrefix determines if the file name is prefixed with the working directory.
     * @return the name of the input vector file.
     */
    public String vectorFileName(boolean withPrefix) {
        return prependDirectory(withPrefix, vectorFileName, workingDirectory);
    }

    /**
     * Returns the name of the working directory.
     * 
     * @return the name of the working directory.
     */
    public String workingDirectory() {
        return workingDirectory;
    }

    public boolean isUsingDatabase() {
        return usingDatabase;
    }

    public void setUsingDatabase(boolean usedatabase) {
        this.usingDatabase = usedatabase;
    }

    public String getDatabaseName() {
        return databaseName;
    }

    public String getDatabasePassword() {
        return databasePassword;
    }

    public String getDatabaseServerAddress() {
        return databaseServerAddress;
    }

    public String getDatabaseUser() {
        return databaseUser;
    }

    public String getDatabaseTableNamePrefix() {
        return databaseTableNamePrefix;
    }

}