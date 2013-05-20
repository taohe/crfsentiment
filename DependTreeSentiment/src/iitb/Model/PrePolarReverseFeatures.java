package iitb.Model;

import dependencytree.PriorPolarityValue;
import iitb.CRF.DataSequence;

import java.util.Map;
import java.util.Set;

import static junit.framework.Assert.assertEquals;

/**
 * Created by IntelliJ IDEA.
 * User: tao
 * This FeatureType fire a Feature when the token right before (index = pos - 1)
 * cur-token (index = pos) is a polarity reversing word
 */
public class PrePolarReverseFeatures extends FeatureTypes {
    private Set<String> polarityReverseSetDict;
    private Map<String, PriorPolarityValue> priorMapDict;
    private final int numStates;
    protected WordsInTrain dict;
    private int stateId;
    /**
     * headToken is the parent node of "token" in the parser dependence-tree
     */
    private String preToken;
    private String token;
    private int tokenId;

    public PrePolarReverseFeatures(FeatureGenImpl m, WordsInTrain d, Set<String> polarRevSet,
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
        assertEquals(prevPos, pos - 1);
        token = (String) (data.x(pos));
        // Check for special case, where a token does NOT have a parent(head)
        if (prevPos < -1) {
            stateId = numStates;
            return false;
        }

        preToken = (String) (data.x(prevPos));  // prevPos = pos - 1
        //System.out.println("PreviousPolarityReverse feature, token= " + token +
        //        "  preToken= " + preToken);

        if (priorMapDict.containsKey(token) && dict.inDictionary(token)
                && polarityReverseSetDict.contains(preToken)) {
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
                "PreviousPolarityReverse_" + token + "_" + stateId, f);
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
