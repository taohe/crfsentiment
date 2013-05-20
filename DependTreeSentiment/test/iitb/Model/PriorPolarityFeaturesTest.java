package iitb.Model;

import dependencytree.LoadPriorPolarityFeature;
import dependencytree.ParserToDependTree;
import dependencytree.SentimentDataIter;
import dependencytree.SentimentTrainSequence;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Created by IntelliJ IDEA.
 * User: tao
 * Date: 5/17/13
 */
public class PriorPolarityFeaturesTest {
    private String testSentence = "player works and looks great - if you can get the dvd 's to play .";
    private boolean testSentencePolarity = true;
    private SentimentTrainSequence sentTrainSeq;
    private ParserToDependTree parserToDependTree;
    private LoadPriorPolarityFeature loadPriorPolarityFeature;
    private SentimentDataIter sentimentDataIter;

    private FeatureGenImpl featureGen;
    private WordsInTrain dict;
    private PriorPolarityFeatures priorPolarityFeatures;   // testing object

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
        priorPolarityFeatures = new PriorPolarityFeatures(featureGen, dict,
                loadPriorPolarityFeature.getPriorPolarityMap());
    }
    @After
    public void tearDown() throws Exception {
        parserToDependTree = null;
        sentTrainSeq = null;
        featureGen = null;
        dict = null;
        priorPolarityFeatures = null;
    }

    @Test
    public void simplePriorPolarityFeatureTypeTest() {
        assertTrue(priorPolarityFeatures.startScanFeaturesAt(sentTrainSeq, 4));  // testing token "great"
        assertTrue(priorPolarityFeatures.hasNext());
        assertEquals(priorPolarityFeatures.getStateId(), 1);  // "great" is positive in the priorPolarity dictionary

        FeatureImpl f = new FeatureImpl();
        priorPolarityFeatures.next(f);
        System.out.println("New feature name: " + f.strId.getName());
        System.out.println("MaxFeature ID of this type: " + priorPolarityFeatures.maxFeatureId());
        assertFalse(priorPolarityFeatures.hasNext());
    }

    @Test
    public void neutralPriPolarFeatureTypeTest() {
        assertTrue(priorPolarityFeatures.startScanFeaturesAt(sentTrainSeq, 3));  // testing token "looks"
        assertTrue(priorPolarityFeatures.hasNext());
        assertEquals(priorPolarityFeatures.getStateId(), 3);  // "looks" is neutral in the priorPolarity dictionary

        FeatureImpl f = new FeatureImpl();
        priorPolarityFeatures.next(f);
        System.out.println("New feature name: " + f.strId.getName());
        System.out.println("MaxFeature ID of this type: " + priorPolarityFeatures.maxFeatureId());
        assertFalse(priorPolarityFeatures.hasNext());
    }
}
