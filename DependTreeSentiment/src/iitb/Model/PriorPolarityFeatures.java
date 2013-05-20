package iitb.Model;

import dependencytree.PriorPolarityValue;
import iitb.CRF.DataSequence;

import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: tao
 * Date: 5/17/13
 * This feature identifies token appears in the prior polarity dictionary
 * fire one feature when a token's prior polarity equals its label (state)
 */
public class PriorPolarityFeatures extends FeatureTypes {
    private Map<String, PriorPolarityValue> priorMapDict;
    private int stateId;
    private int tokenId;
    private final int numStates;
    private String token;
    protected WordsInTrain dict;

    PriorPolarityFeatures(FeatureGenImpl m, WordsInTrain d,
                          Map<String, PriorPolarityValue> priorPolarityValueMap) {
        super(m);
        this.numStates = m.numStates();
        this.dict = d;
        this.priorMapDict = priorPolarityValueMap;
    }

    public boolean startScanFeaturesAt(DataSequence data, int prevPos, int pos) {
        token = (String)(data.x(pos));
        if (priorMapDict.containsKey(token) && dict.inDictionary(token)) {
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
        if (priorMapDict.containsKey(token)) {
            int qiVal = priorMapDict.get(token).priorPolarity;
            while ((stateId < numStates) && ((qiVal <= 0 && stateId == 1) ||
                    (qiVal >= 0 && stateId == 2) || (qiVal != 0 && stateId == 3))) {
                stateId++;
            }
            return (stateId < numStates);
        } else {
            return false;
        }
    }

    public void next(FeatureImpl f) {
        setFeatureIdentifier(tokenId * numStates + stateId, stateId, "PriPolarity_" + token, f);
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
