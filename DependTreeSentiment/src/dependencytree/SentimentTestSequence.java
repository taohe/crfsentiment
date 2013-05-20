package dependencytree;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: tao
 * Date: 5/13/13
 * Time: 8:56 PM
 * To change this template use File | Settings | File Templates.
 */
public class SentimentTestSequence extends SentimentTrainSequence {
    private int[] predictLabelSeq;

    public SentimentTestSequence(ParserToDependTree parserToDependTree,
                                 LoadPriorPolarityFeature loadPriorPolarityFeature) {
        super(parserToDependTree, loadPriorPolarityFeature);
        predictLabelSeq = new int[parserToDependTree.getPhraseListArgu().phraseList.length];
        for (int i = 0; i < absoluteLength(); i++) {
            predictLabelSeq[i] = 0;
        }
    }

    /**
     * Construct test sequence from SentimentTrainSequence
     * @param trainSeq
     */
    public SentimentTestSequence(SentimentTrainSequence trainSeq) {
        super(trainSeq);
        predictLabelSeq = new int[absoluteLength()];
        for (int i = 0; i < absoluteLength(); i++) {
            predictLabelSeq[i] = 0;
        }
    }

    /**
     * Construct new List of test sequences from List of train sequences,
     * All data copied, NOT referenced
     * @param trainSequenceList
     * @return
     */
    public static List<SentimentTestSequence> constrTestSeqsFromTrainSeqs(
            List<SentimentTrainSequence> trainSequenceList) {
        List<SentimentTestSequence> newTestSeqs =
                new ArrayList<SentimentTestSequence>(trainSequenceList.size());
        for (SentimentTrainSequence trainSeq : trainSequenceList) {
            newTestSeqs.add(new SentimentTestSequence(trainSeq));
        }
        return newTestSeqs;
    }

    /**
     * Set new predicted label for y at position (i+1)
     * @param i         Index
     * @param label     New predicted label
     */
    public void set_y(int i, int label) {
        predictLabelSeq[i+1] = label;
    }

    /**
     * Get Sentence polarity from token level predicted polarities
     * This model only considers all children directly under the nonRootHead
     * Count pos/neg among those children, choose majority polarity
     * @return
     */
    public boolean getPredSentencePolarity() {
        Iterator<Integer> childrenIter = childIndListSets.get(0).iterator();
        int nonRootHeadInd = childrenIter.next();
        childrenIter = childIndListSets.get(nonRootHeadInd).iterator();  // Get all children of nonRootHead

        int posCount = 0;
        int negCount = 0;
        while (childrenIter.hasNext()) {
            int childInd = childrenIter.next();
            if (predictLabelSeq[childInd] == 1) {
                posCount++;
            } else if (predictLabelSeq[childInd] == 2) {
                negCount++;
            }
        }

        System.out.println("\n   ***NonRoot Head Ind= " + nonRootHeadInd + "  posCount= " + posCount
                + "  negCount= " + negCount);
        if (posCount > negCount) {
            return true;
        } else if (negCount > posCount) {
            return false;
        } else {  // direct children posCount and negCount equal, then count all tokens
            posCount = 0;
            negCount = 0;
            for (int i = 1; i < absoluteLength(); i++) {
                if (predictLabelSeq[i] == 1) {
                    posCount++;
                } else if (predictLabelSeq[i] == 2) {
                    negCount++;
                }
            }
            System.out.println("\n   ***Direct children equal, total posCount= " + posCount
                    + "  negCount= " + negCount);
            return (posCount >= negCount);

        }
    }

    /**
     * This actually performs worse: Correct rate= 0.5860058309037901
     * @return
     */
    /*
    public boolean getPredSentencePolarity() {
        int posCount = 0;
        int negCount = 0;
        for (int i = 1; i < absoluteLength(); i++) {  // Count all token polarities from non-Root tokens
            if (predictLabelSeq[i] == 1) {
                posCount++;
            } else if (predictLabelSeq[i] == 2) {
                negCount++;
            }
        }

        System.out.println("\n   ***Token posCount= " + posCount + "  negCount= " + negCount);
        if (posCount >= negCount) {
            return true;
        } else {
            return false;
        }
    }
    */
    
    public int[] getPredictLabelSeq() {
        return predictLabelSeq;
    }
}
