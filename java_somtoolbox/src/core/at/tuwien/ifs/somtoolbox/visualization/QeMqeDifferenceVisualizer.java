package at.tuwien.ifs.somtoolbox.visualization;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.logging.Logger;

import at.tuwien.ifs.somtoolbox.layers.LayerAccessException;
import at.tuwien.ifs.somtoolbox.layers.Unit;
import at.tuwien.ifs.somtoolbox.layers.quality.QeMqeDifference;
import at.tuwien.ifs.somtoolbox.layers.quality.QualityMeasureNotFoundException;
import at.tuwien.ifs.somtoolbox.models.GrowingSOM;

/**
 * Creates the visualizations for the difference between the quantization error and the mean quantization error.
 * 
 * @author Stefan Belk
 * @author Herbert Pajer
 * @version $Id:
 */
public class QeMqeDifferenceVisualizer extends AbstractMatrixVisualizer implements QualityMeasureVisualizer {

    private QeMqeDifference diffQeMqe = null;

    public QeMqeDifferenceVisualizer() {
        NUM_VISUALIZATIONS = 4;
        VISUALIZATION_NAMES = new String[] { "Difference QE/MQE", "Difference QE/MQE - Logarithmic ",
                "Difference QE/MQE - Squared ", "Difference QE/MQE - Exponential " };
        VISUALIZATION_SHORT_NAMES = VISUALIZATION_NAMES;
        VISUALIZATION_DESCRIPTIONS = VISUALIZATION_NAMES;
        neededInputObjects = null;
    }

    @Override
    public BufferedImage createVisualization(int index, GrowingSOM gsom, int width, int height) {
        if (diffQeMqe == null) {
            diffQeMqe = new QeMqeDifference(gsom.getLayer(), null);
        }

        switch (index) {
            case 0: {
                return createImage(gsom, width, height, QeMqeDifference.DIFF_QE_MQE);
            }
            case 1: {
                return createImage(gsom, width, height, QeMqeDifference.LOG_DIFF_QE_MQE);
            }
            case 2: {
                return createImage(gsom, width, height, QeMqeDifference.SQUARE_DIFF_QE_MQE);
            }
            case 3: {
                return createImage(gsom, width, height, QeMqeDifference.EXP_DIFF_QE_MQE);
            }
            default: {
                return null;
            }
        }
    }

    /**
     * Applies min-max normalization to the supplied value.
     * 
     * @param val The value that shall be normalized.
     * @param max The maximum of all corresponding values.
     * @param min The minimum of all corresponding values.
     * @return The normalized value.
     */
    private double normalize(double val, double max, double min) {
        return (val - min) / (max - min);
    }

    /**
     * @param gsom A GrowingSOM.
     * @param width Width of the image.
     * @param height Height of the image.
     * @param visualType Type of the visualization: "diff_qe_mqe", "log_diff_qe_mqe", "square_diff_qe_mqe",
     *            "exp_diff_qe_mqe"
     * @return A BufferedImage.
     */
    private BufferedImage createImage(GrowingSOM gsom, int width, int height, String visualType) {
        double maxDiff = Double.MIN_VALUE;
        double minDiff = Double.MAX_VALUE;
        double[][] unitQualities;
        try {
            unitQualities = diffQeMqe.getUnitQualities(visualType);
        } catch (QualityMeasureNotFoundException e) {
            Logger.getLogger("at.tuwien.ifs.somtoolbox").severe(e.getMessage());
            return null;
        }

        // find the maximum and minimum for normalization
        for (int j = 0; j < gsom.getLayer().getYSize(); j++) {
            for (int i = 0; i < gsom.getLayer().getXSize(); i++) {
                try {
                    Unit u = gsom.getLayer().getUnit(i, j);
                    if (u.getNumberOfMappedInputs() > 0) {
                        double quality = unitQualities[u.getXPos()][u.getYPos()];
                        if (quality > maxDiff) {
                            maxDiff = quality;
                        }

                        if (quality < minDiff) {
                            minDiff = quality;
                        }
                    }
                } catch (LayerAccessException e) {
                    Logger.getLogger("at.tuwien.ifs.somtoolbox").severe(e.getMessage());
                }
            }
        }

        maximumMatrixValue = maxDiff;
        minimumMatrixValue = minDiff;

        // create the image
        BufferedImage res = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = (Graphics2D) res.getGraphics();

        int unitWidth = width / gsom.getLayer().getXSize();
        int unitHeight = height / gsom.getLayer().getYSize();

        int ci = 0;
        for (int y = 0; y < gsom.getLayer().getYSize(); y++) {
            for (int x = 0; x < gsom.getLayer().getXSize(); x++) {
                try {
                    Unit u = gsom.getLayer().getUnit(x, y);
                    if (u.getNumberOfMappedInputs() > 0) {
                        double d = unitQualities[u.getXPos()][u.getYPos()];
                        ci = (int) Math.round(normalize(d, maxDiff, minDiff) * palette.maxColourIndex());
                        g.setPaint(palette.getColor(ci));
                    } else {
                        g.setPaint(Color.WHITE);
                    }
                    g.setColor(null);
                    g.fill(new Rectangle(x * unitWidth, y * unitHeight, unitWidth, unitHeight));
                } catch (LayerAccessException e) {
                    Logger.getLogger("at.tuwien.ifs.somtoolbox").severe(e.getMessage());
                }
            }
        }

        return res;
    }
}
