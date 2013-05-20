package dependencytree;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Created by IntelliJ IDEA.
 * User: tao
 * Date: 5/14/13
 * Time: 11:57 AM
 * To change this template use File | Settings | File Templates.
 */
public class SentimentTestSequenceTest {
    private String testSentence =
            "i recently purchased the canon powershot g3 and am extremely satisfied with the purchase .";
    private SentimentTestSequence sentTestSeq;
    private LoadPriorPolarityFeature loadPriorPolarityFeature;
    private ParserToDependTree parserToDependTree;

    @Before
    public void setUp() throws Exception {
        parserToDependTree = new ParserToDependTree(testSentence, true, true);
        assertEquals(true, parserToDependTree.getPhraseListArgu().isPos);
        loadPriorPolarityFeature = new LoadPriorPolarityFeature();
        loadPriorPolarityFeature.readPolarReversalDict(false);

        sentTestSeq = new SentimentTestSequence(parserToDependTree, loadPriorPolarityFeature);
    }

    @After
    public void tearDown() throws Exception {
        parserToDependTree = null;
        sentTestSeq = null;
    }

    @Test
    public void testSeqSetYTest() {
        sentTestSeq.set_y(3, 2);
        for (int i = 0; i < sentTestSeq.length(); i++) {
            if (i != 3) {
                assertEquals(0, sentTestSeq.getPredictLabelSeq()[i+1]);  // +1 because indexes are shifted
            } else {
                assertEquals(2, sentTestSeq.getPredictLabelSeq()[i+1]);
            }
        }
    }
}
