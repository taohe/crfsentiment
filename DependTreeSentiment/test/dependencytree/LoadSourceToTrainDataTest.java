package dependencytree;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertNotNull;

/**
 * Created by IntelliJ IDEA.
 * User: tao
 * Date: 5/10/13
 * Time: 8:31 PM
 */
public class LoadSourceToTrainDataTest {
    private LoadSourceToTrainData loadSourceToTrainData;
    @Before
    public void setUp() throws Exception {
        loadSourceToTrainData = new LoadSourceToTrainData();
    }
    @After
    public void tearDown() throws Exception {
        loadSourceToTrainData = null;
    }

    @Test
    public void smallDataTest() {
        loadSourceToTrainData.readAllFilesInResource(Integer.MAX_VALUE, "/resource", false);
        //loadSourceToTrainData.readAllFilesInResource(1, false);
        List<ParserToDependTree> parserToDependTreeList =
                loadSourceToTrainData.getParserToDependTreeList();
        for (ParserToDependTree parserToDependTree : parserToDependTreeList) {
            PhraseListArgu phraseListArgu = parserToDependTree.getPhraseListArgu();
            assertNotNull(phraseListArgu);

            System.out.println("Saved sentence:  " + Arrays.toString(phraseListArgu.phraseList));
            System.out.println("Polarity:   " + phraseListArgu.isPos);
            for (int i = 0; i < phraseListArgu.phraseList.length; i++) {
                System.out.println("Word: " + phraseListArgu.phraseList[i]
                        + "  ind: " + i + "  parentInd: " + phraseListArgu.headIndList[i]
                        + "  rel: " + phraseListArgu.grammarRel[i]);
            }
            System.out.println("\n");
        }
    }

    @Test
    public void loadTestDataTest() {  // loading data from "test_files" folder
        loadSourceToTrainData.readAllFilesInResource(Integer.MAX_VALUE, "/test_files", false);
        //loadSourceToTrainData.readAllFilesInResource(1, false);
        List<ParserToDependTree> parserToDependTreeList =
                loadSourceToTrainData.getParserToDependTreeList();
        for (ParserToDependTree parserToDependTree : parserToDependTreeList) {
            PhraseListArgu phraseListArgu = parserToDependTree.getPhraseListArgu();
            assertNotNull(phraseListArgu);

            System.out.println("Saved sentence:  " + Arrays.toString(phraseListArgu.phraseList));
            System.out.println("Polarity:   " + phraseListArgu.isPos);
            for (int i = 0; i < phraseListArgu.phraseList.length; i++) {
                System.out.println("Word: " + phraseListArgu.phraseList[i]
                        + "  ind: " + i + "  parentInd: " + phraseListArgu.headIndList[i]
                        + "  rel: " + phraseListArgu.grammarRel[i]);
            }
            System.out.println("\n");
        }
    }
}
