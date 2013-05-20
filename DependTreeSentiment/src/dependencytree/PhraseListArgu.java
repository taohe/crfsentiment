package dependencytree;

/**
 * Created by IntelliJ IDEA.
 * User: tao
 * Date: 5/10/13
 * Time: 1:55 PM
 * To change this template use File | Settings | File Templates.
 */
public class PhraseListArgu {
    public String[] phraseList;  // Index starts from 1
    public String[] grammarRel;  // Index starts from 1
    public int[] headIndList;  // Ordinary gov starts from 1, default to -1, root is 0
    public boolean isPos;  // If true, the sentence has a positive sentiment

    public PhraseListArgu(int length, boolean isPositive) {
        this.phraseList = new String[length+1];
        this.grammarRel = new String[length+1];
        this.headIndList = new int[length+1];
        for (int i = 1; i < length + 1; i++) {
            headIndList[i] = -1;
        }
    }
}
