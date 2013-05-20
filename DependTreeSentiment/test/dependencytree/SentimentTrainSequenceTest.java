package dependencytree;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Iterator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * User: tao
 * Date: 5/14/13
 * Time: 11:38 AM
 */
public class SentimentTrainSequenceTest {
    //private String testSentence =
    //       "this player is not worth any price and i recommend that you do n't purchase it .";
           //"output went black and white only via both s-video and composite connections , and with no v-hold .";
           // "i recently purchased the canon powershot g3 and am extremely satisfied with the purchase .";
    private String testSentence = "It prevents cancer and heart disease .";
    //private String testSentence =
    //        "it does not play windows media.";
    private boolean testSentencePolarity = true;
    private SentimentTrainSequence sentTrainSeq;
    private ParserToDependTree parserToDependTree;
    private LoadPriorPolarityFeature loadPriorPolarityFeature;
    @Before
    public void setUp() throws Exception {
        parserToDependTree = new ParserToDependTree(testSentence, testSentencePolarity, true);
        assertEquals(testSentencePolarity, parserToDependTree.getPhraseListArgu().isPos);
        loadPriorPolarityFeature = new LoadPriorPolarityFeature();
        loadPriorPolarityFeature.readPolarReversalDict(false);   // Read Polarity Reversal Dict
        loadPriorPolarityFeature.readPriPolarDict(false);  // Read Prior Polarity Dict

        sentTrainSeq = new SentimentTrainSequence(parserToDependTree, loadPriorPolarityFeature);
    }
    @After
    public void tearDown() throws Exception {
        parserToDependTree = null;
        sentTrainSeq = null;
    }

    @Test
    /**
     * sentTrainSeq.length() should be actual length - 1
     */
    public void trainSeqLengthTest() {
        assertEquals(sentTrainSeq.length() + 1, parserToDependTree.getPhraseListArgu().phraseList.length);
    }

    @Test
    /**
     * Print y values from sentTrainSeq.y(-1)
     */
    public void trainSeqYValTest() {
        for (int i = -1; i < sentTrainSeq.length(); i++) {
            System.out.println("y[" + i + "]= " + ((parserToDependTree.getPhraseListArgu().isPos)? 1 : 2));
            //assertEquals(sentTrainSeq.y(i), ((parserToDependTree.getPhraseListArgu().isPos) ? 1 : 2));
        }
    }
    
    @Test
    public void trainSeqXValTest() {
        for (int i = -1; i < sentTrainSeq.length(); i++) {
            System.out.println("x[" + i + "]= " + sentTrainSeq.x(i));
            assertEquals((String)sentTrainSeq.x(i), parserToDependTree.getPhraseListArgu().phraseList[i+1]);
        }
    }

    @Test
    public void trainSeqUpdateChildIndListSetsTest() {
        printTestSentence(false);
        printHeadIndList();
        printChildIndList();

        for (int i = 1; i < sentTrainSeq.length(); i++) {  // start from 1, check parent omit ROOT
            if (parserToDependTree.getPhraseListArgu().headIndList[i] >= 0) {  // Omit token with NO parent
                assertTrue(sentTrainSeq.getChildIndListSets().get(
                        parserToDependTree.getPhraseListArgu().headIndList[i]).contains(i));
            }
        }
    }

    @Test
    public void trainSeqHeuristicLabelsTest() {
        printTestSentence(true);
        printHeadIndList();
        printChildIndList();

        for (int i = 1; i < sentTrainSeq.length(); i++) {  // start from 1, check parent omit ROOT
            if (parserToDependTree.getPhraseListArgu().headIndList[i] >= 0) {  // Omit token with NO parent
                assertTrue(sentTrainSeq.getChildIndListSets().get(
                        parserToDependTree.getPhraseListArgu().headIndList[i]).contains(i));
            }
        }
    }

    public void printTestSentence(boolean isWithLabel) {
        System.out.print("Sentence:     ");
        for (int i = -1; i < sentTrainSeq.length(); i++) {
            if (isWithLabel) {
                String polarSymbol = "0";
                if (sentTrainSeq.y(i) == 1) {
                    polarSymbol = "+";
                } else if (sentTrainSeq.y(i) == 2) {
                    polarSymbol = "-";
                }
                System.out.print(parserToDependTree.getPhraseListArgu().phraseList[i+1]
                        +"(" + polarSymbol + ") ");
            } else {
                System.out.print(parserToDependTree.getPhraseListArgu().phraseList[i+1] + " ");
            }
        }
        System.out.print("\n");
    }

    public void printHeadIndList() {
        System.out.print("HeadIndList:  ");
        for (int i = 0; i < sentTrainSeq.length(); i++) {
            System.out.print("  " + parserToDependTree.getPhraseListArgu().headIndList[i] + "  ");
        }
        System.out.print("\n");
    }

    public void printChildIndList() {
        System.out.print("ChildIndList: ");
        for (int i = 0; i < sentTrainSeq.length(); i++) {
            Iterator<Integer> childrenIter = sentTrainSeq.getChildIndListSets().get(i).iterator();
            System.out.print("(");
            while (childrenIter.hasNext()) {
                System.out.print(childrenIter.next() + "  ");
            }
            System.out.print(") ");
        }
        System.out.print("\n");
    }
}
