package dependencytree;

import iitb.CRF.DataSequence;

import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: tao
 * Date: 5/13/13
 * Output data without "ROOT", x(i), y(i) will return index i+1 of corresponding data
 */
public class SentimentTrainSequence implements DataSequence {
    private LoadPriorPolarityFeature loadPriorPolarityFeature;
    private Set<String> polarityReversalDict;
    private Map<String, PriorPolarityValue> priorPolarityMap;
    private ParserToDependTree parseTreeSequence;
    protected ArrayList<HashSet<Integer>> childIndListSets;
    private int[] heuristicLabels;  // Label is 1: Pos, 2: Neg, 3: Neutral

    /**
     * The polarityReversal dictionary has been used in this class
     * Caller must make sure the loadPriorPolarityFeature.readPolarReversalDict() and readPrior() have been called
     * before constructing this class
     * @param parserToDependTree
     * @param loadPriorPolarityFeature
     */
    public SentimentTrainSequence(ParserToDependTree parserToDependTree,
                                  LoadPriorPolarityFeature loadPriorPolarityFeature) {
        this.parseTreeSequence = parserToDependTree;
        this.loadPriorPolarityFeature = loadPriorPolarityFeature;
        this.polarityReversalDict = loadPriorPolarityFeature.getPolarityReversalSet();
        this.priorPolarityMap = loadPriorPolarityFeature.getPriorPolarityMap();
        childIndListSets =
                new ArrayList<HashSet<Integer>>(parserToDependTree.getPhraseListArgu().phraseList.length);
        this.heuristicLabels =
                new int[parserToDependTree.getPhraseListArgu().phraseList.length];
        // Update childIndListSets and heuristicLabels
        updateChildIndListSets();
        updateHeuristicLabels();
    }

    /**
     * Copy constructor, new references
     */
    public SentimentTrainSequence(SentimentTrainSequence trainSeq) {
        this(trainSeq.getParseTreeSequence(), trainSeq.getLoadPriorPolarityFeature());
    }

    /**
     * ChildIndList is an ArrayList of HashSets
     */
    public void updateChildIndListSets() {
        for (int i = 0; i < absoluteLength(); i++) {  // Allocate memory for sets
            childIndListSets.add(new HashSet<Integer>());
        }

        for (int i = 0; i < absoluteLength(); i++) {
            int[] headIndListRef = parseTreeSequence.getPhraseListArgu().headIndList;
            if (headIndListRef[i] != i && headIndListRef[i] >= 0) {  // Omit self loop and No-parent indexes
                (childIndListSets.get(headIndListRef[i])).add(i);
            }
        }
    }

    /**
     * Simple heuristics for assigning polarity for each token:
     * if appears in priorPolarDict, set 1 or 2, Otherwise, set as 3.
     * For first token: "ROOT", always set as the sentence polarity(definite for training data)
     * Label is 1: Pos, 2: Neg, 3: Neutral
     */
    /*
    public void updateHeuristicLabels() {
        // Label on [0] is always definite for training data
        heuristicLabels[0] = (parseTreeSequence.getPhraseListArgu().isPos)? 1:2;
        for (int i = 1; i < absoluteLength(); i++) {
            // token[i] is a polarity reversing word, set polarity to Negative
            if (polarityReversalDict.contains(parseTreeSequence.getPhraseListArgu().phraseList[i])) {
                heuristicLabels[i] = 2;
                continue;
            }

            if (priorPolarityMap.containsKey(parseTreeSequence.getPhraseListArgu().phraseList[i])) {
                int priorPolarity = priorPolarityMap.get(
                        parseTreeSequence.getPhraseListArgu().phraseList[i]).priorPolarity;
                if (priorPolarity > 0) {
                    heuristicLabels[i] = 1;
                } else if (priorPolarity < 0) {
                    heuristicLabels[i] = 2;
                } else {
                    heuristicLabels[i] = 3;
                }
            } else {
                heuristicLabels[i] = 3;
            }
        }
    }
    */

    /**
     * Absolute length of this train-sentence (with the first token as "ROOT")
     * @return      parseTreeSequence.getPhraseListArgu().phraseList.length;
     */
    public int absoluteLength() {
        return parseTreeSequence.getPhraseListArgu().phraseList.length;
    }

    /**
     * Output length: parseTreeSequence.getPhraseListArgu().phraseList.length - 1
     * Only for output to CRF package, sentence tokens without "ROOT"
     * @return
     */
    public int length() {
        return parseTreeSequence.getPhraseListArgu().phraseList.length - 1;
    }

    /**
     * Return the NAIVE label: sentimental polarity of the sub-tree rooted
     * at index i: dataSequence.getPhraseListArgu().phraseList[i]
     * ---- This version is VERY CRUDE now
     * @return    1 for Positive sentiment; 2 for Negative sentiment
     */
    public void updateHeuristicLabels() {
        int sentencePolar = (parseTreeSequence.getPhraseListArgu().isPos)? 1 : 2;
        for (int i = 0; i < absoluteLength(); i++) {
            heuristicLabels[i] = sentencePolar;
        }
    }

    /**
     * Simple heuristics for assigning polarity for each token:
     * First assign sentence polarity to each token, then
     * if a token appears in priorPolarDict, set 1 or 2, Otherwise, set as 3.
     * For first token: "ROOT", always set as the sentence polarity(definite for training data)
     * Label is 1: Pos, 2: Neg, 3: Neutral
     */
    /*
    public void updateHeuristicLabels() {
        // Label on [0] is always definite for training data
        int sentencePolar = (parseTreeSequence.getPhraseListArgu().isPos)? 1:2;
        for (int i = 0; i < absoluteLength(); i++) {
            heuristicLabels[i] = sentencePolar;
        }

        for (int i = 1; i < absoluteLength(); i++) {
            // token[i] is a polarity reversing word, set polarity to Negative
            if (polarityReversalDict.contains(parseTreeSequence.getPhraseListArgu().phraseList[i])) {
                heuristicLabels[i] = 2;
                continue;
            }

            if (priorPolarityMap.containsKey(parseTreeSequence.getPhraseListArgu().phraseList[i])) {
                int priorPolarity = priorPolarityMap.get(
                        parseTreeSequence.getPhraseListArgu().phraseList[i]).priorPolarity;
                if (priorPolarity > 0) {
                    heuristicLabels[i] = 1;
                } else if (priorPolarity < 0) {
                    heuristicLabels[i] = 2;
                } else {
                    heuristicLabels[i] = 3;
                }
            }
        }
    }
    */

    /**
     * Return label based on simple heuristics (Use polarityReversal Dictionary)
     * @param i     Index
     * @return      heuristicLabels[i+1]
     */
    public int y(int i) {
        // Return labels based on simple heuristics
        return heuristicLabels[i+1];
    }

    /**
     * Get the observed token of this SentimentTrainSequence at position (i+1)
     * x[0] will always be "ROOT"--- Will NOT be exported
     * The type of x is never interpreted by the CRF package. This could be useful for your FeatureGenerator class
     */
    public Object x(int i) {
        return (Object) (parseTreeSequence.getPhraseListArgu().phraseList[i+1]);
    }

    /**
     * This method will NOT be called, because this class is ONLY
     * for Training sequence
     * @param i
     * @param label
     */
    public void set_y(int i, int label) { }

    public ArrayList<HashSet<Integer>> getChildIndListSets() {
        return childIndListSets;
    }

    public ParserToDependTree getParseTreeSequence() {
        return parseTreeSequence;
    }

    public LoadPriorPolarityFeature getLoadPriorPolarityFeature() {
        return loadPriorPolarityFeature;
    }

    /**
     * Update the heuristicLabels int array, start from the root index
     * Each label corresponds to the polarity of the subtree rooted at this index
     * Label is 1: Pos, 2: Neg, 3: Neutral
     * NOT good performance, test score = 0.485
     */
    /*
    public void updateHeuristicLabels() {
        // Set init values
        heuristicLabels[0] = (parseTreeSequence.getPhraseListArgu().isPos)? 1:2;
        for (int i = 1; i < length(); i++) {
            heuristicLabels[i] = 3;
        }

        // BFS search on the parsed dependency tree
        Queue bfsQueue = new LinkedList<Integer>();
        bfsQueue.add(0);

        while (!bfsQueue.isEmpty()) {
            int parentInd = (Integer)(bfsQueue.remove());  // Remove current visiting parent
            // Get an iterator of all children under this parent
            Iterator<Integer> childrenIter = childIndListSets.get(parentInd).iterator();
            while (childrenIter.hasNext()) {
                int nextChildInd = childrenIter.next();
                bfsQueue.add(nextChildInd);

                // parent is ROOT or NON-polarity reversing, assign same polarity as parent to children
                if (parentInd == 0 || !(polarityReversalDict.contains(parseTreeSequence.
                        getPhraseListArgu().phraseList[parentInd]))) {
                    heuristicLabels[nextChildInd] = heuristicLabels[parentInd];
                } else {
                    switch (heuristicLabels[parentInd]) {
                        case 1: heuristicLabels[nextChildInd] = 2; break;
                        case 2: heuristicLabels[nextChildInd] = 1; break;
                        case 3: heuristicLabels[nextChildInd] = 3; break;
                        default: System.out.println("SentimentTrainSequence.updateHeuristicLabels parent polarity Wrong!");
                            break;
                    }
                }
            }
        }
        return;
    }
    */
}
