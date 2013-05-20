package iitb.Model;
import iitb.CRF.DataSequence;
import iitb.CRF.SegmentDataSequence;

import java.io.Serializable;


/**
 *
 * Inherit from the FeatureTypes class for creating any kind of
 * feature. You will see various derived classes from them,
 * EdgeFeatures, StartFeatures, etc, etc.  The ".id" field of
 * FeatureImpl does NOT need to be set by the FeatureTypes.next()
 * methods.
 *
 * @author Sunita Sarawagi
 * @since 1.0
 * @version 1.3
 */

public abstract class FeatureTypes implements Serializable {
	private static final long serialVersionUID = 8062238861233186461L;
	int thisTypeId;
    private FeatureGenImpl fgen;
    public Model model;
    public boolean cache = false;
    protected boolean disabled = false;

    public FeatureTypes(FeatureGenImpl fgen) {
        model = fgen.model;
        this.fgen = fgen;
        thisTypeId = fgen.numFeatureTypes++;
    }
    /**
     * @param s   FeatureTypes s, copy constructor
     */
    public FeatureTypes(FeatureTypes s) {
        this(s.fgen);
        thisTypeId = s.thisTypeId;
        fgen.numFeatureTypes--;
        
    }

    /**
     * startScanFeaturesAt() with 2 parameters
     * @param data    DataSequence type
     * @param pos     int, position to scan
     * @return        startScanFeaturesAt(data, pos - 1, pos)
     */
    public  boolean startScanFeaturesAt(DataSequence data, int pos) {
        return startScanFeaturesAt(data,pos-1,pos);
    }

    /**
     * This method is used to instruct the FeatureTypes to generate all the features for label of
     * the token at the position pos in the data sequence data. The previous position indicates the
     * previous label assigned to the current sequence. Since, this implementation of the CRF is
     * a one order markov model, a feature can have dependancy on previous label of the current sequence only.
     * This method initiates generation of the features in given FeatureTypes.
     * @param data            DataSequence
     * @param prevPos         int, previous token index
     * @param pos             int, current token index
     * @return
     */
    public abstract boolean startScanFeaturesAt(DataSequence data, int prevPos, int pos);

    /**
     * This method is used to check if there are any more features for the current scan
     * intitaited in startScanFeaturesAt().
     * @return
     */
    public abstract boolean hasNext();

    /**
     * An important function of this class, it basically contains the code for generating the next feature
     * i.e. this function will contain the code for capturing the characteristic that the user wants to capture
     * in the form of a feature. A Feature, object passed to this function as an argument, will be assigned the
     * values of the newly generated feature. The features are generated on-the-fly as and when needed for
     * efficiency reasons.
     * @param f       FeatureImpl
     */
    public abstract void next(FeatureImpl f);

    public void setFeatureIdentifier(int fId, int stateId, String name, FeatureImpl f) {
        setFeatureIdentifier( fId,  stateId, (Object)name,  f);
    }
    public void setFeatureIdentifier(int fId, int stateId, Object name, FeatureImpl f) {
        f.strId.init(fId*fgen.numFeatureTypes + thisTypeId,stateId,name);
    }
    public void setFeatureIdentifier(int fId, FeatureImpl f) {
        f.strId.init(fId*fgen.numFeatureTypes + thisTypeId);
    }
    public int labelIndependentId(FeatureImpl f) {
        return ((f.strId.id-thisTypeId)-f.strId.stateId*fgen.numFeatureTypes)/model.numStates()+thisTypeId;
    }
    int offsetLabelIndependentId(FeatureImpl f) {
        return (labelIndependentId(f)-thisTypeId)/fgen.numFeatureTypes;
    }
    public static int featureTypeId(FeatureImpl f, FeatureGenImpl fgen) {
        return featureTypeId(f.strId,fgen);
    }
    public static int featureTypeId(FeatureIdentifier strId, FeatureGenImpl fgen) {
        return strId.id % fgen.numFeatureTypes;
    }
    //public void print(FeatureGenImpl.FeatureMap strToInt, double crfWs[]) {;}
    public int maxFeatureId() {
		iitb.Utils.LogMessage.issueWarning("WARNING : Class " + getClass().getName() + " does not implement maxFeatureId(). Returning default value. Please refer to the documentation.");
		return Integer.MAX_VALUE;
	}
    /*  private void readObject(ObjectInputStream s) throws IOException, ClassNotFoundException  {
     s.defaultReadObject();
     offset = Math.max(fgen.numFeatureTypes,thisTypeId+1);
     }
     */
    public int getTypeId() {return thisTypeId;}
    public boolean featureCollectMode() {return fgen.featureCollectMode;}
    // returns false if transition features change with x or position
    public boolean fixedTransitionFeatures() {
        return true;
    }
    public boolean requiresTraining(){return false;}
    public void train(DataSequence data, int pos) {
		if(requiresTraining()) {
			iitb.Utils.LogMessage.issueWarning("WARNING : Class " + getClass().getName() + " does not implement the train(DataSequence, int) method. Please implement the train() methods properly.");
		}
	}	
    /**
     * Training for semi-Markov features
     * @param sequence
     * @param segStart: inclusive of the segment start
     * @param segEnd
     */
    public void train(SegmentDataSequence sequence, int segStart, int segEnd) {
		if(requiresTraining()) {
			iitb.Utils.LogMessage.issueWarning("WARNING : Class " + getClass().getName() + " does not implement the train(SegmentDataSequence, int, int) method. Calling train(DataSequence, int) instead. Please implement the train() methods properly.");
			train((DataSequence)sequence, segEnd);
        }
	}	
    /**
     * @return
     */
    public boolean needsCaching() {
        return cache;
    }
    public void trainingDone() {
        if(requiresTraining()) {
            iitb.Utils.LogMessage.issueWarning("WARNING : Class " + getClass().getName() + " does not implement the train(SegmentDataSequence, int, int) method. Calling train(DataSequence, int) instead. Please implement the trainDone() methods properly.");
        }
    }
    public void disable() {
        disabled=true;
    }
    public boolean isDisabled() {
        return disabled;
    }
    public String name() {
        return getClass().getName();
    }
};

