package dependencytree;

import iitb.CRF.CRF;
import iitb.CRF.FeatureGenerator;
import iitb.Model.FeatureGenImpl;
import iitb.Utils.Options;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: tao
 * Date: 5/14/13
 * Time: 12:41 PM
 */
public class SentimentCV {
    private static final String CRF_Model_File_Path = "./log.crf";
    private static final String FeatureGen_Features_File_Path =
            "./log.features";
    private LoadPriorPolarityFeature loadPriorPolarityFeature;  // reference to FeatureGenImpl's
    private String modelGraphType = "naive";
    private int nlabels = 4;    // 1: Pos, 2: Neg, 3: Neutral
    private CRF crfModel;
    private FeatureGenImpl featureGen;
    private Options options;
    private SentimentDataIter fullTrainDataIter;
    private final int cvFold = 5;
    final int oneChunkSize;

    /**
     * Main class for sentiment prediction, using "naive" model graph
     * @throws Exception
     */
    public SentimentCV() throws Exception {
        // Allocate Model
        allocateModel();
        fullTrainDataIter = new SentimentDataIter(true, loadPriorPolarityFeature);
        oneChunkSize = (fullTrainDataIter.getTrainSequenceList().size()) / cvFold;
    }

    /**
     * Allocate Model and FeatureGen class for training/testing
     * @throws Exception
     */
    public void allocateModel() throws Exception {
        options = new Options();
        featureGen = new FeatureGenImpl(modelGraphType, nlabels);
        this.loadPriorPolarityFeature = featureGen.getLoadPriorPolarityFeature();
        crfModel = new CRF(featureGen.numStates(), featureGen, options);
    }

    /**
     * Training the CRF using data in trainDataIter(files under /src/resource)
     * @param cvTestFractionInd   cross-validation test fraction index, range= [0, cvFold)
     * @param isDebugging         If true, go verbose
     * @throws Exception
     */
    public void train(int cvTestFractionInd, boolean isDebugging) throws Exception {
        //DataIter trainDataIter = new SentimentDataIter(true, loadPriorPolarityFeature);
        SentimentDataIter cvTrainDataIter = null;
        if (cvTestFractionInd == 0) {  // Test on first chunk
            cvTrainDataIter = new SentimentDataIter(true, fullTrainDataIter, oneChunkSize,
                    fullTrainDataIter.getTrainSequenceList().size());
        } else if (cvTestFractionInd == cvFold - 1) {
            cvTrainDataIter = new SentimentDataIter(true, fullTrainDataIter, 0, cvTestFractionInd * oneChunkSize);
        } else {
            // training on [0, testInd * oneChunkSize) and [(test+1)*oneChunkSize, end)
            cvTrainDataIter = new SentimentDataIter(fullTrainDataIter, 0, cvTestFractionInd * oneChunkSize,
                    (cvTestFractionInd + 1) * oneChunkSize, fullTrainDataIter.getTrainSequenceList().size());
        }
        featureGen.train(cvTrainDataIter);
        double featureWts[] = crfModel.train(cvTrainDataIter);
        System.out.println("Training Done on train sample of size= " + cvTrainDataIter.getTrainSequenceList().size());

        crfModel.write(SentimentCV.CRF_Model_File_Path);
        featureGen.write(SentimentCV.FeatureGen_Features_File_Path);

        if (isDebugging) {
            featureGen.displayModel(featureWts);
        }
    }

    /**
     * CRF test function
     * @param cvTestFractionInd  cross-validation test fraction index, range= [0, cvFold)
     * @throws IOException
     * @return test accuracy (correct ratio)
     */
    public double test(int cvTestFractionInd) throws Exception {
        crfModel.read(SentimentCV.CRF_Model_File_Path);
        featureGen.read(SentimentCV.FeatureGen_Features_File_Path);

        double testAccuracy = doTest(cvTestFractionInd);

        allocateModel();  // re-init model
        return testAccuracy;
    }

    /**
     * CRF doTest function
     * @param cvTestFractionInd   cross-validation test fraction index, range= [0, cvFold)
     */
    public double doTest(int cvTestFractionInd) {
        //DataIter testDataIter = new SentimentDataIter(false, loadPriorPolarityFeature);
        SentimentDataIter cvTestDataIter = null;
        if (cvTestFractionInd != cvFold - 1) {  // // testing on [cvTestInd/5, (cvTestInd + 1)/5)
            cvTestDataIter = new SentimentDataIter(false, fullTrainDataIter,
                    cvTestFractionInd * oneChunkSize, (cvTestFractionInd + 1) * oneChunkSize);
        } else {  // Test on the last chunk
            cvTestDataIter = new SentimentDataIter(false, fullTrainDataIter,
                    cvTestFractionInd * oneChunkSize, fullTrainDataIter.getTrainSequenceList().size());
        }
        System.out.println("Test fraction size= " + cvTestDataIter.getTestSequenceList().size()
                + "  Start test from ind = " + cvTestFractionInd * oneChunkSize + "  to ind= "
                + (cvTestFractionInd + 1) * oneChunkSize);

        int correctCount = 0;
        int truePosCountInTest = 0;
        while (cvTestDataIter.hasNext()) {
            SentimentTestSequence oneTest = (SentimentTestSequence)cvTestDataIter.next();
            crfModel.apply(oneTest);
            //int posCount = 0;
            for (int i = 0; i < oneTest.length(); i++) {
                System.out.print(oneTest.x(i) + "(" + oneTest.getPredictLabelSeq()[i+1] + ") ");  // i+1 because of shifting
                if (oneTest.getPredictLabelSeq()[i] == 1) {
              //      posCount++;
                }
            }
            boolean isPos = oneTest.getPredSentencePolarity();
            if (isPos) {  // Only check the root polarity
                System.out.println("   Positive(Exp: " + ((oneTest.y(-1) == 1)? "Pos":"Neg") + ")");
            } else {
                System.out.println("   Negative(Exp: " + ((oneTest.y(-1) == 1)? "Pos":"Neg") + ")");
            }
            if (isPos == (oneTest.y(-1) == 1)) {
                correctCount++;
            }
            if (oneTest.y(-1) == 1) {  // This test sequence is actually positive
                truePosCountInTest++;
            }
        }

        double testAccuracy = (double)correctCount /
                (double)(((SentimentDataIter)cvTestDataIter).getTestSequenceList().size());
        System.out.println("CV test index= " + cvTestFractionInd + " Correct Count= " + correctCount
                + "(" + cvTestDataIter.getTestSequenceList().size() + ")  Correct RATIO= "
                + testAccuracy);
        System.out.println("Pos in Test: " + truePosCountInTest + "   pos ratio: " +
                (double)truePosCountInTest/(double)(cvTestDataIter.getTestSequenceList().size()));

        return testAccuracy;
    }

    /**
     * If do CV only once, first argument is the cvTest fraction index: [0, cvFold)
     * @param argv
     * @throws Exception
     */
    public static void main(String[] argv) throws Exception {
        //int testFractionInd = 2;
        SentimentCV sentiment = new SentimentCV();

        if (argv.length > 0) {
            int testFractionInd = Integer.parseInt(argv[0]);
            sentiment.train(testFractionInd, true);
            sentiment.test(testFractionInd);
        } else {
            List<Double> cvAccuracyList = new ArrayList<Double>(sentiment.getCvFold());

            for (int testFractionInd = 0; testFractionInd < sentiment.getCvFold(); testFractionInd++) {
                sentiment.train(testFractionInd, true);
                double testAccuracy = sentiment.test(testFractionInd);
                cvAccuracyList.add(testAccuracy);
            }

            double mean = SentimentCV.getMean(cvAccuracyList);
            double sd = SentimentCV.getSD(cvAccuracyList, mean);
            SentimentCV.printList(cvAccuracyList);
            System.out.println("CV mean= " + mean + "  SD= " + sd);
        }
    }


    public static double getSD(List<Double> doubleList, double mean) {
        double sum = 0.0;
        for (int i = 0; i < doubleList.size(); i++) {
            sum += Math.pow(doubleList.get(i) - mean, 2);
        }
        return Math.sqrt(sum / (doubleList.size() - 1));
    }

    public static double getMean(List<Double> doubleList) {
        double sum = 0.0;
        for (int i = 0; i < doubleList.size(); i++) {
            sum += doubleList.get(i);
        }

        return (sum / doubleList.size());
    }
    
    public static void printList(List<Double> doubleList) {
        System.out.print("List size= " + doubleList.size() + "  [ ");
        for (int i = 0; i < doubleList.size(); i++) {
            System.out.print(doubleList.get(i) + "  ");
        }
        System.out.print("]\n");
    }

    public FeatureGenerator getFeatureGenerator() {
        return featureGen;
    }
    
    public int getCvFold() {
        return cvFold;
    }
}
