package iitb.Model;

import dependencytree.LoadPriorPolarityFeature;
import dependencytree.ParserToDependTree;
import dependencytree.SentimentDataIter;
import dependencytree.SentimentTrainSequence;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by IntelliJ IDEA.
 * User: tao
 * Date: 5/17/13
 */
public class PrePolarReverseFeaturesTest {
    private String testSentence = "it prevents cancer and heart disease .";
    private boolean testSentencePolarity = true;
    private SentimentTrainSequence sentTrainSeq;
    private ParserToDependTree parserToDependTree;
    private LoadPriorPolarityFeature loadPriorPolarityFeature;
    private SentimentDataIter sentimentDataIter;

    private FeatureGenImpl featureGen;
    private WordsInTrain dict;
    private PrePolarReverseFeatures prePolarReverseFeatures;   // testing object

    @Before
    public void setUp() throws Exception {
        parserToDependTree = new ParserToDependTree(testSentence, testSentencePolarity, true);
        assertEquals(testSentencePolarity, parserToDependTree.getPhraseListArgu().isPos);
        loadPriorPolarityFeature = new LoadPriorPolarityFeature();
        loadPriorPolarityFeature.readPolarReversalDict(false);   // Read Polarity Reversal Dict
        loadPriorPolarityFeature.readPriPolarDict(false);  // Read Prior Polarity Dict

        sentTrainSeq = new SentimentTrainSequence(parserToDependTree, loadPriorPolarityFeature);
        sentimentDataIter = new SentimentDataIter(false, loadPriorPolarityFeature);  // Read test data

        // FeatureGenImpl's constructor has been changed
        featureGen = new FeatureGenImpl("naive", 4);
        dict = new WordsInTrain();
        dict.train(sentimentDataIter, 4);
        prePolarReverseFeatures = new PrePolarReverseFeatures(featureGen, dict,
                loadPriorPolarityFeature.getPolarityReversalSet(),
                loadPriorPolarityFeature.getPriorPolarityMap());
    }
    @After
    public void tearDown() throws Exception {
        parserToDependTree = null;
        sentTrainSeq = null;
        featureGen = null;
        dict = null;
        prePolarReverseFeatures = null;
    }

    @Test
    public void simplePolarReverseFeatureTypeTest() {
        assertTrue(prePolarReverseFeatures.startScanFeaturesAt(sentTrainSeq, 2));  // "cancer"
        assertTrue(prePolarReverseFeatures.hasNext());
    }
}
