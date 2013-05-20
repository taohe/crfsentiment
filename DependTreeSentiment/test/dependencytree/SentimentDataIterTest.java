package dependencytree;

import iitb.CRF.DataSequence;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

/**
 * Created by IntelliJ IDEA.
 * User: tao
 * Date: 5/14/13
 * Time: 3:23 PM
 */
public class SentimentDataIterTest {
    private SentimentDataIter sentimentDataIter;
    private LoadPriorPolarityFeature loadPriorPolarityFeature;
    @Before
    public void setUp() throws Exception {
        loadPriorPolarityFeature = new LoadPriorPolarityFeature();
        loadPriorPolarityFeature.readPriPolarDict(false);
        loadPriorPolarityFeature.readPolarReversalDict(false);
    }
    @After
    public void tearDown() throws Exception {
        sentimentDataIter = null;
    }

    @Test
    public void trainDataSizeTest() {
        sentimentDataIter = new SentimentDataIter(true, loadPriorPolarityFeature);  // test on train data, already loaded
        sentimentDataIter.startScan();
        System.out.println("TrainData Size = " +
                sentimentDataIter.getTrainSequenceList().size());
        assertEquals(sentimentDataIter.getTrainSequenceList().size(),
                sentimentDataIter.getParserToDependTreeList().size());
    }
    
    @Test
    public void trainDataIntegrityTest() {
        sentimentDataIter = new SentimentDataIter(true, loadPriorPolarityFeature);  // test on train data, already loaded
        sentimentDataIter.startScan();

        assertEquals(0, sentimentDataIter.getCurDataSeqInd());
        for (ParserToDependTree parserToDependTree : sentimentDataIter.getParserToDependTreeList()) {
            assertTrue(sentimentDataIter.hasNext());
            DataSequence trainSequence = sentimentDataIter.next();
            assertTrue(trainSequence instanceof SentimentTrainSequence);
            assertEquals(trainSequence.length(),
                    parserToDependTree.getPhraseListArgu().phraseList.length);
            for (int i = 0; i < (trainSequence).length(); i++) {
                assertEquals(trainSequence.x(i), parserToDependTree.getPhraseListArgu().phraseList[i]);
                if (parserToDependTree.getPhraseListArgu().isPos) {
                    assertEquals(1, trainSequence.y(i));
                } else {
                    assertEquals(2, trainSequence.y(i));
                }
            }
        }
    }

    @Test
    public void loadTestDataTest() {
        sentimentDataIter = new SentimentDataIter(false, loadPriorPolarityFeature);
        sentimentDataIter.startScan();

        System.out.println("TestData Size = " +
                sentimentDataIter.getTestSequenceList().size());
        assertEquals(sentimentDataIter.getTestSequenceList().size(),
                sentimentDataIter.getParserToDependTreeList().size());

        assertEquals(0, sentimentDataIter.getCurDataSeqInd());
        for (ParserToDependTree parserToDependTree : sentimentDataIter.getParserToDependTreeList()) {
            assertTrue(sentimentDataIter.hasNext());
            DataSequence testSequence = sentimentDataIter.next();
            assertTrue(testSequence instanceof SentimentTestSequence);
            assertEquals(testSequence.length(),
                    parserToDependTree.getPhraseListArgu().phraseList.length - 1);
            for (int i = 0; i < testSequence.length(); i++) {
                assertEquals(testSequence.x(i), parserToDependTree.getPhraseListArgu().phraseList[i+1]);
                assertEquals(0, ((SentimentTestSequence) testSequence).getPredictLabelSeq()[i+1]);   // Because this is test data
                /* -- this only works for Naive labeling
                if (parserToDependTree.getPhraseListArgu().isPos) {
                    assertEquals(1, testSequence.y(i));
                } else {
                    assertEquals(2, testSequence.y(i));
                }
                */
            }
        }
    }

    @Test
    /**
     * Test for split a trainData DataIter to train and test fractions
     * Used for cross-validation
     */
    public void splitDataIterForCrossValidTest() {
        final int cvFold = 5;
        sentimentDataIter = new SentimentDataIter(true, loadPriorPolarityFeature);
        final int trainEndInd = (cvFold - 1) * (sentimentDataIter.getTrainSequenceList().size()) / cvFold;
        sentimentDataIter.startScan();

        System.out.println("Before Split TrainData Size = " +
                sentimentDataIter.getTrainSequenceList().size());

        SentimentDataIter cvTrainDataIter = new SentimentDataIter(true, sentimentDataIter, 0, trainEndInd);
        SentimentDataIter cvTestDataIter = new SentimentDataIter(false, sentimentDataIter, trainEndInd,
                sentimentDataIter.getTrainSequenceList().size());

        int trainLen = cvTrainDataIter.getTrainSequenceList().size();
        int testLen = cvTestDataIter.getTestSequenceList().size();
        System.out.println("cvTrain Len = " + trainLen + "  cvTest Len = " + testLen);
        assertEquals(trainLen + testLen, sentimentDataIter.getTrainSequenceList().size());

        sentimentDataIter.startScan();
        int count = 0;
        for (ParserToDependTree parserToDependTree : sentimentDataIter.getParserToDependTreeList()) {
            assertTrue(sentimentDataIter.hasNext());
            DataSequence testSequence = sentimentDataIter.next();
            assertTrue(testSequence instanceof SentimentTrainSequence);
            assertEquals(testSequence.length(),
                    parserToDependTree.getPhraseListArgu().phraseList.length - 1);

            if (count < trainEndInd) {
                assertEquals(((SentimentTrainSequence) testSequence).getParseTreeSequence().getPhraseListArgu().phraseList[2],
                        cvTrainDataIter.getTrainSequenceList().get(count).getParseTreeSequence().
                                getPhraseListArgu().phraseList[2]);
            } else {
                assertEquals(((SentimentTrainSequence) testSequence).getParseTreeSequence().getPhraseListArgu().phraseList[2],
                        cvTestDataIter.getTestSequenceList().get(count - trainEndInd).getParseTreeSequence().
                                getPhraseListArgu().phraseList[2]);
            }
            count++;
        }
    }

    /**
     * Test for split a trainData DataIter to train and test fractions
     * Train fraction is a combination of two chunks
     * Used for cross-validation
     */
    @Test
    public void splitTwoChunksTrainForCVTest() {
        final int cvFold = 5;
        sentimentDataIter = new SentimentDataIter(true, loadPriorPolarityFeature);
        final int oneChunkSize = (sentimentDataIter.getTrainSequenceList().size()) / cvFold;
        sentimentDataIter.startScan();

        System.out.println("Before Split TrainData Size = " +
                sentimentDataIter.getTrainSequenceList().size());

        SentimentDataIter cvTrainDataIter = new SentimentDataIter(sentimentDataIter, 0, oneChunkSize,
                2 * oneChunkSize, sentimentDataIter.getTrainSequenceList().size());
        SentimentDataIter cvTestDataIter = new SentimentDataIter(false, sentimentDataIter, oneChunkSize,
                2 * oneChunkSize);

        int trainLen = cvTrainDataIter.getTrainSequenceList().size();
        int testLen = cvTestDataIter.getTestSequenceList().size();
        System.out.println("cvTrain Len = " + trainLen + "  cvTest Len = " + testLen);
        assertEquals(trainLen + testLen, sentimentDataIter.getTrainSequenceList().size());

        sentimentDataIter.startScan();
        int count = 0;
        for (ParserToDependTree parserToDependTree : sentimentDataIter.getParserToDependTreeList()) {
            System.out.println("Comparing on index: " + count + "  of original Full train sequence.");

            assertTrue(sentimentDataIter.hasNext());
            DataSequence testSequence = sentimentDataIter.next();
            assertTrue(testSequence instanceof SentimentTrainSequence);
            assertEquals(testSequence.length(),
                    parserToDependTree.getPhraseListArgu().phraseList.length - 1);

            if (count < oneChunkSize || count >= 2 * oneChunkSize) {
                if (count < oneChunkSize) {
                    assertEquals(((SentimentTrainSequence) testSequence).getParseTreeSequence().getPhraseListArgu().phraseList[2],
                            cvTrainDataIter.getTrainSequenceList().get(count).getParseTreeSequence().
                                    getPhraseListArgu().phraseList[2]);
                } else {
                    assertEquals(((SentimentTrainSequence) testSequence).getParseTreeSequence().getPhraseListArgu().phraseList[2],
                            cvTrainDataIter.getTrainSequenceList().get(count - oneChunkSize).getParseTreeSequence().
                                    getPhraseListArgu().phraseList[2]);
                }
            } else {
                assertEquals(((SentimentTrainSequence) testSequence).getParseTreeSequence().getPhraseListArgu().phraseList[2],
                        cvTestDataIter.getTestSequenceList().get(count - oneChunkSize).getParseTreeSequence().
                                getPhraseListArgu().phraseList[2]);
            }
            count++;
        }
    }
}
