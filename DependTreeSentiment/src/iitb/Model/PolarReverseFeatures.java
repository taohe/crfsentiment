package iitb.Model;

import dependencytree.PriorPolarityValue;
import dependencytree.SentimentTrainSequence;
import iitb.CRF.DataSequence;

import java.util.Map;
import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: tao
 * Date: 5/17/13
 */
public class PolarReverseFeatures extends FeatureTypes {
    private Set<String> polarityReverseSetDict;
    private Map<String, PriorPolarityValue> priorMapDict;
    private final int numStates;
    protected WordsInTrain dict;
    private int stateId;
    /**
     * headToken is the parent node of "token" in the parser dependence-tree
     */
    private String headToken;
    private String token;
    private int tokenId;

    public PolarReverseFeatures(FeatureGenImpl m, WordsInTrain d, Set<String> polarRevSet,
                         Map<String, PriorPolarityValue> priorPolarityValueMap) {
        super(m);
        this.numStates = m.numStates();
        this.dict = d;
        this.polarityReverseSetDict = polarRevSet;
        this.priorMapDict = priorPolarityValueMap;
    }

    /**
     * startScanFeaturesAt() Check data.x(pos) and data.x(headInd[pos])
     * @param data            DataSequence
     * @param prevPos         int, previous token index
     * @param pos             int, current token index
     * @return
     */
    public boolean startScanFeaturesAt(DataSequence data, int prevPos, int pos) {
        token = (String)(data.x(pos));
        // Check for special case, where a token does NOT have a parent(head)
        if (((SentimentTrainSequence)data).getParseTreeSequence().getPhraseListArgu().
                headIndList[pos + 1] == -1) {
            stateId = numStates;
            return false;
        }

        headToken = (String)(data.x(((SentimentTrainSequence)data).getParseTreeSequence().
                getPhraseListArgu().headIndList[pos+1]-1));  // +1, -1 because "pos" is shifted
        //System.out.println("PolarReversal feature, token= " + token + "  headToken= " + headToken);
        if (priorMapDict.containsKey(token) && dict.inDictionary(token)
                && polarityReverseSetDict.contains(headToken)) {
            tokenId = dict.getIndex(data.x(pos));
            stateId = 1;  // This feature type only works for feature = 1: Pos, 2: Neg, 3: neutral
            return true;
        } else {
            stateId = numStates;
            return false;
        }
    }

    public boolean hasNext() {
        //System.out.println("Token when calling hasNext(): " + token);
        return (stateId < numStates);
    }

    public void next(FeatureImpl f) {
        setFeatureIdentifier(tokenId * numStates + stateId, stateId,
                "HeadPolarityReverse_" + token + "_" + stateId, f);
        f.yend = stateId;
        f.ystart = -1;
        f.val = 1;
        stateId++;
    }

    public int maxFeatureId() {
        return dict.dictionaryLength() * numStates;
    }

    public int getStateId() {
        return stateId;
    }
}
