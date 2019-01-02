package eppic;

import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;
import org.biojava.nbio.structure.*;
import org.biojava.nbio.structure.asa.AsaCalculator;
import org.junit.Ignore;
import org.junit.Test;

import javax.vecmath.AxisAngle4d;
import javax.vecmath.Matrix4d;
import javax.vecmath.Vector3d;
import java.io.IOException;

public class TestAsaCalc {

    /**
     * Testing how resolution behaves with different sampling sphere points values.
     * See a very good resolution-dependency study for Shrake and Rupley: https://f1000research.com/articles/5-189/v1
     * specifically this figure: https://f1000researchdata.s3.amazonaws.com/manuscripts/8538/ba5c8904-d388-4258-98fe-bd76cef6df16_figure2.gif
     */
    @Ignore // since it doesn't assert. Intended to be run manually
    @Test
    public void testAsaResolutions() throws StructureException, IOException {

        Structure structure = StructureIO.getStructure("1SMT");
        Atom[] atoms = StructureTools.getAllAtomArray(structure.getPolyChainByPDB("A"));

        // the reference value, using a very large sampling value, as done in https://f1000research.com/articles/5-189/v1
        double[] refAsas = calcAsas(atoms, 10000);

        int[] nSpherePointsSamples = {10, 100, 500, 1000, 2000, 3000};
        for (int nSpherePoints:nSpherePointsSamples) {
            System.out.println("Number of sphere points " + nSpherePoints);
            estimateAsaError(atoms, nSpherePoints, refAsas);
        }

        // this tests does not need to assert anything
    }

    private double[] calcAsas(Atom[] atoms, int nSpherePoints) {
        long start = System.currentTimeMillis();
        AsaCalculator asaCalc = new AsaCalculator(atoms,
                AsaCalculator.DEFAULT_PROBE_SIZE,
                nSpherePoints, 1);

        double[] asas = asaCalc.calculateAsas();
        long end = System.currentTimeMillis();
        System.out.printf("Took %6.2f s to calculate ASAs with %d sphere points\n", (end-start)/1000.0, nSpherePoints);

        return asas;
    }

    private void estimateAsaError(Atom[] atoms, int nSpherePoints, double[] refAsas) {

        double[] rotationAngles = { Math.PI/8, Math.PI/4, 3*Math.PI/8, Math.PI/2, 5*Math.PI/8, 6*Math.PI/8, 7*Math.PI/8, Math.PI};
        double[][] allAsas = new double[rotationAngles.length][atoms.length];

        for (int i = 0; i<rotationAngles.length; i++) {
            double rotationAngle = rotationAngles[i];
            Matrix4d m = new Matrix4d(1,0,0,0, 0,1,0,0, 0,0,1,0, 0,0,0,1);

            // important, for a good estimate we need to rotate around a diagonal axis. Effects with straight x, y, z axes are much lower
            m.setRotation(new AxisAngle4d(new Vector3d(1,0.74,0.35), rotationAngle));

            Atom[] rotatedAtoms = new Atom[atoms.length];
            for (int j = 0; j < rotatedAtoms.length; j++) {
                rotatedAtoms[j] = (Atom) atoms[j].clone();
                rotatedAtoms[j].setGroup(atoms[j].getGroup());
            }
            Calc.transform(rotatedAtoms, m);
            double[] asas = calcAsas(rotatedAtoms, nSpherePoints);
            allAsas[i] = asas;
        }

        // standard deviations
        double[] allStds = new double[atoms.length];
        for (int j = 0; j<atoms.length; j ++) {
            double[] column = new double[rotationAngles.length];
            for (int i = 0; i < rotationAngles.length; i++) {
                column[i] = allAsas[i][j];
            }
            DescriptiveStatistics ds = new DescriptiveStatistics(column);
            double std = ds.getStandardDeviation();
            allStds[j] = std;
        }
        DescriptiveStatistics ds = new DescriptiveStatistics(allStds);
        System.out.printf("Mean standard deviation: %6.3f \n", ds.getMean());


        // now stats based on deviation from reference value
        double[] diffsToRef = new double[atoms.length*rotationAngles.length];
        for (int j = 0; j<atoms.length; j++) {
            //double[] diffsToRef = new double[rotationAngles.length];
            for (int i = 0; i < rotationAngles.length; i++) {
                diffsToRef[i + j] = Math.abs(refAsas[j] - allAsas[i][j]);
            }
        }
        ds = new DescriptiveStatistics(diffsToRef);
        System.out.printf("Mean deviation from reference value: %6.3f \n", ds.getMean());


    }
}
