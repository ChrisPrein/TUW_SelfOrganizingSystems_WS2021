package at.tuwien.ifs.somtoolbox.apps.helper;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Random;

import at.tuwien.ifs.somtoolbox.visualization.QeMqeDifferenceVisualizer;

/**
 * Creates data sets that show the difference between the quantization error and the mean quantization error (see
 * {@link QeMqeDifferenceVisualizer}.
 * 
 * @author Stefan Belk
 * @author Herbert Pajer
 * @version $Id: QeMqeDifferenceDataGenerator.java 4346 2015-03-09 16:23:46Z mayer $
 */
public class QeMqeDifferenceDataGenerator {
    static Random rand = new Random();

    private static int numstatic = 100; // number of data points in the static noise

    // the clusters have a Gaussian normal distribution
    // the parameters are: number of nodes in the cluster, center of the cluster, standard deviation of the cluster
    private static int cluster1size = 5;

    private static double[] cluster1coordinates = { 1, 1, 1 };

    private static double cluster1sigma = 0.05;

    private static int cluster2size = 5;

    private static double[] cluster2coordinates = { -1, 1, 0 };

    private static double cluster2sigma = 0.05;

    private static int cluster3size = 5;

    private static double[] cluster3coordinates = { 1, -1, 0 };

    private static double cluster3sigma = 0.05;

    private static int cluster4size = 5;

    private static double[] cluster4coordinates = { -1, -1, -1 };

    private static double cluster4sigma = 0.05;

    private static double z_scale = 1; // scale of the z-axis

    // total number of data points
    private static int totalnodes = numstatic + cluster1size + cluster2size + cluster3size + cluster4size;

    public static void main(String[] args) throws IOException {
        int ID = 1;

        // creates vector template file
        File templatefile = new File(System.getProperty("user.dir").toString() + "/output/Clusters.tv");
        FileOutputStream templateoutputstream = new FileOutputStream(templatefile);
        OutputStreamWriter templateoutputstreamwriter = new OutputStreamWriter(templateoutputstream);
        Writer templatewriter = new BufferedWriter(templateoutputstreamwriter);
        templatewriter.write("$TYPE template\n$XDIM 7\n$YDIM " + totalnodes
                + "\n$VEC_DIM 3\n1 comp_1 1 1 1 1 1.0\n2 comp_2 1 1 1 1 1.0\n3 comp_3 1 1 1 1 1.0\n");
        templatewriter.close();

        // creates input vector file
        File vectorfile = new File(System.getProperty("user.dir").toString() + "/output/Clusters.vec");
        FileOutputStream vectorfileoutputstream = new FileOutputStream(vectorfile);
        OutputStreamWriter vectorstreamwriter = new OutputStreamWriter(vectorfileoutputstream);
        Writer vectorwriter = new BufferedWriter(vectorstreamwriter);
        vectorwriter.write("$TYPE vec\n$XDIM " + totalnodes + "\n$YDIM 1\n$VEC_DIM 3\n");

        for (int i = 0; i < cluster1size; i++) {
            vectorwriter.write(cluster(cluster1coordinates, cluster1sigma) + " " + ID + "\n");
            ID++;
        }

        for (int i = 0; i < cluster2size; i++) {
            vectorwriter.write(cluster(cluster2coordinates, cluster2sigma) + " " + ID + "\n");
            ID++;
        }

        for (int i = 0; i < cluster3size; i++) {
            vectorwriter.write(cluster(cluster3coordinates, cluster3sigma) + " " + ID + "\n");
            ID++;
        }

        for (int i = 0; i < cluster4size; i++) {
            vectorwriter.write(cluster(cluster4coordinates, cluster4sigma) + " " + ID + "\n");
            ID++;
        }

        for (int i = 0; i < numstatic; i++) {
            vectorwriter.write(createstatic() + " " + ID + "\n");
            ID++;
        }

        vectorwriter.close();
    }

    public static String cluster(double[] coordinates, double sigma) {
        // coordinates denotes the center of the cluster and sigma is the standard deviation

        double x = coordinates[0] + rand.nextGaussian() * sigma;
        double y = coordinates[1] + rand.nextGaussian() * sigma;
        double z = (coordinates[2] + rand.nextGaussian() * sigma) * z_scale;

        return x + " " + y + " " + z;
    }

    public static String createstatic() {
        // creates uniformly distributed static noise in the box ([-1,1],[-1,1],[-z_scale,z_scale])

        double x = 2 * rand.nextDouble() - 1;
        double y = 2 * rand.nextDouble() - 1;
        double z = (2 * rand.nextDouble() - 1) * z_scale;

        return x + " " + y + " " + z;
    }
}
