package at.tuwien.ifs.somtoolbox.layers.quality;

import at.tuwien.ifs.somtoolbox.data.InputData;
import at.tuwien.ifs.somtoolbox.layers.Layer;
import at.tuwien.ifs.somtoolbox.layers.LayerAccessException;
import at.tuwien.ifs.somtoolbox.layers.Unit;

/**
 * Calculates the difference between the quantization error and the mean quantization error in four variations. Simple,
 * logarithmic, squared and exponential difference.
 * 
 * @author Stefan Belk
 * @author Herbert Pajer
 * @version $Id:
 */
public class QeMqeDifference extends AbstractQualityMeasure {

    public static final String EXP_DIFF_QE_MQE = "exp_diff_qe_mqe";

    public static final String SQUARE_DIFF_QE_MQE = "square_diff_qe_mqe";

    public static final String LOG_DIFF_QE_MQE = "log_diff_qe_mqe";

    public static final String DIFF_QE_MQE = "diff_qe_mqe";

    private double averageDiff = 0;

    private double[][] diffQeMqe;

    private int nonEmpty = 0;

    private int xSize = 0;

    private int ySize = 0;

    public QeMqeDifference(Layer layer, InputData data) {
        super(layer, data);
        mapQualityNames = new String[] { DIFF_QE_MQE, LOG_DIFF_QE_MQE, SQUARE_DIFF_QE_MQE, EXP_DIFF_QE_MQE };
        mapQualityDescriptions = new String[] { "Difference QE MQE", "Logarithmic Difference QE MQE",
                "Squared Difference QE MQE", "Exponential Difference QE MQE" };
        unitQualityNames = new String[] { DIFF_QE_MQE, LOG_DIFF_QE_MQE, SQUARE_DIFF_QE_MQE, EXP_DIFF_QE_MQE };
        unitQualityDescriptions = new String[] { "Difference QE MQE", "Logarithmic Difference QE MQE",
                "Squared Difference QE MQE", "Exponential Difference QE MQE" };

        xSize = layer.getXSize();
        ySize = layer.getYSize();
        diffQeMqe = new double[xSize][ySize];

        for (int y = 0; y < ySize; y++) {
            for (int x = 0; x < xSize; x++) {
                try {
                    Unit u = layer.getUnit(x, y);
                    double quantErr = 0;

                    if (u != null && u.getNumberOfMappedInputs() > 0) {
                        double[] dists = u.getMappedInputDistances();
                        for (int i = 0; i < u.getNumberOfMappedInputs(); i++) {
                            quantErr += dists[i];
                        }

                        double unitQe = quantErr;
                        double unitMqe = quantErr / u.getNumberOfMappedInputs();
                        double diff = Math.abs(unitQe - unitMqe);

                        nonEmpty++;
                        averageDiff += diff;
                        diffQeMqe[x][y] = diff;

                    } else {
                        diffQeMqe[x][y] = 0;
                    }
                } catch (LayerAccessException e) {
                    e.printStackTrace();
                }

            }
        }

        if (nonEmpty > 0) {
            averageDiff = averageDiff / nonEmpty;
        } else {
            averageDiff = 0;
        }
    }

    /**
     * Returns the average difference.
     * 
     * @param type Visualization type: "diff_qe_mqe", "log_diff_qe_mqe", "square_diff_qe_mqe", "exp_diff_qe_mqe"
     * @return The average difference
     */
    private double getDiff(String type) {
        double diff = 0;
        for (int y = 0; y < ySize; y++) {
            for (int x = 0; x < xSize; x++) {
                if (type.equals(LOG_DIFF_QE_MQE) && diffQeMqe[x][y] != 0) {
                    diff += Math.log(diffQeMqe[x][y]);
                } else if (type.equals(SQUARE_DIFF_QE_MQE)) {
                    diff += diffQeMqe[x][y] * diffQeMqe[x][y];
                } else if (type.equals(EXP_DIFF_QE_MQE)) {
                    diff += Math.exp(diffQeMqe[x][y]);
                }
            }
        }
        return diff / nonEmpty;
    }

    /**
     * Computes the matrix of differences difference "log_diff_qe_mqe", "square_diff_qe_mqe", "exp_diff_qe_mqe" or
     * returns the pre-computed matrix for "diff_qe_mqe"
     * 
     * @param type Visualization type: "diff_qe_mqe", "log_diff_qe_mqe", "square_diff_qe_mqe", "exp_diff_qe_mqe"
     * @return The matrix of differences
     */
    private double[][] getDiffMatrix(String type) {
        double[][] diff = new double[xSize][ySize];
        for (int y = 0; y < ySize; y++) {
            for (int x = 0; x < xSize; x++) {
                if (type.equals(LOG_DIFF_QE_MQE) && diffQeMqe[x][y] != 0) {
                    diff[x][y] += Math.log(diffQeMqe[x][y]);
                } else if (type.equals(SQUARE_DIFF_QE_MQE)) {
                    diff[x][y] = diffQeMqe[x][y] * diffQeMqe[x][y];
                } else if (type.equals(EXP_DIFF_QE_MQE)) {
                    diff[x][y] = Math.exp(diffQeMqe[x][y]);
                }
            }
        }
        return diff;
    }

    @Override
    public double getMapQuality(String name) throws QualityMeasureNotFoundException {
        if (name.equals(DIFF_QE_MQE)) {
            return averageDiff;
        } else if (name.equals(LOG_DIFF_QE_MQE)) {
            return this.getDiff(LOG_DIFF_QE_MQE);
        } else if (name.equals(SQUARE_DIFF_QE_MQE)) {
            return this.getDiff(SQUARE_DIFF_QE_MQE);
        } else if (name.equals(EXP_DIFF_QE_MQE)) {
            return this.getDiff(EXP_DIFF_QE_MQE);
        } else {
            throw new QualityMeasureNotFoundException("Quality measure with name " + name + " not found.");
        }
    }

    @Override
    public double[][] getUnitQualities(String name) throws QualityMeasureNotFoundException {
        if (name.equals(DIFF_QE_MQE)) {
            return diffQeMqe;
        } else if (name.equals(LOG_DIFF_QE_MQE)) {
            return this.getDiffMatrix(LOG_DIFF_QE_MQE);
        } else if (name.equals(SQUARE_DIFF_QE_MQE)) {
            return this.getDiffMatrix(SQUARE_DIFF_QE_MQE);
        } else if (name.equals(EXP_DIFF_QE_MQE)) {
            return this.getDiffMatrix(EXP_DIFF_QE_MQE);
        } else {
            throw new QualityMeasureNotFoundException("Quality measure with name " + name + " not found.");
        }
    }

}
