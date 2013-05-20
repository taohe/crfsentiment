package dependencytree;

/**
 * Created by IntelliJ IDEA.
 * User: tao
 */
public class PriorPolarityValue {
    /**
     * priorPolarity is type int, -1: weakNeg, -2: strongNeg, 1: weakPos, 2: strongPos, 0: neutral
     */
    public final int priorPolarity;
    public final String subjectiveType;  // Index starts from 1
    public final String posTag;
    public final boolean isStemmed;

    public PriorPolarityValue(String priPolar, String subType, String pos,
                              String stemmed) throws Exception {
        if (priPolar.startsWith("neg")) {
            if (subType.matches("weak.*")) {
                this.priorPolarity = -1;
            } else {
                this.priorPolarity = -2;
            }
        } else if (priPolar.startsWith("positive")) {
            if (subType.matches("weak.*")) {
                this.priorPolarity = 1;
            } else {
                this.priorPolarity = 2;
            }
        } else {
            this.priorPolarity = 0;
        }

        this.subjectiveType = subType;
        this.posTag = pos;

        if (stemmed.equals("y")) {
            this.isStemmed = true;
        } else if (stemmed.equals("n")) {
            this.isStemmed = false;
        } else {
            throw new Exception("Stemmed Exception!");
        }
    }
}
