package dependencytree;

import iitb.CRF.CRF;
import iitb.CRF.FeatureGenerator;
import iitb.Model.FeatureGenImpl;
import iitb.Utils.Options;

import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 * User: tao
 * Date: 5/14/13
 * Time: 12:41 PM
 */
public class Sentiment {
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
    final int trainStartInd;

    /**
     * Main class for sentiment prediction, using "naive" model graph
     * @throws Exception
     */
    public Sentiment() throws Exception {
        // Allocate Model
        allocateModel();
        fullTrainDataIter = new SentimentDataIter(true, loadPriorPolarityFeature);
        trainStartInd = (fullTrainDataIter.getTrainSequenceList().size()) / cvFold;
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
     * @param isDebugging     If true, go verbose
     * @throws Exception
     */
    public void train(boolean isDebugging) throws Exception {
        //DataIter trainDataIter = new SentimentDataIter(true, loadPriorPolarityFeature);
        SentimentDataIter cvTrainDataIter = new SentimentDataIter(true, fullTrainDataIter, trainStartInd,
                fullTrainDataIter.getTrainSequenceList().size());  // training on last 4/5
        featureGen.train(cvTrainDataIter);
        double featureWts[] = crfModel.train(cvTrainDataIter);
        System.out.println("Training Done!");

        crfModel.write(Sentiment.CRF_Model_File_Path);
        featureGen.write(Sentiment.FeatureGen_Features_File_Path);

        if (isDebugging) {
            featureGen.displayModel(featureWts);
        }
    }

    public void test() throws IOException {
        crfModel.read(Sentiment.CRF_Model_File_Path);
        featureGen.read(Sentiment.FeatureGen_Features_File_Path);
        doTest();
    }

    public void doTest() {
        //DataIter testDataIter = new SentimentDataIter(false, loadPriorPolarityFeature);
        SentimentDataIter cvTestDataIter = new SentimentDataIter(false, fullTrainDataIter, 0,
                trainStartInd);
        System.out.println("Start test from ind = " + 0 + "  to ind= " + trainStartInd);

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
        System.out.println("Correct rate= " + (double)correctCount /
                (double)(((SentimentDataIter)cvTestDataIter).getTestSequenceList().size()));
        System.out.println("Pos in Test: " + truePosCountInTest + "   pos ratio: " +
                (double)truePosCountInTest/(double)(cvTestDataIter.getTestSequenceList().size()));
    }

    public static void main(String[] argv) throws Exception {
        Sentiment sentiment = new Sentiment();
        sentiment.train(true);
        sentiment.test();
    }

    public FeatureGenerator getFeatureGenerator() {
        return featureGen;
    }
}
