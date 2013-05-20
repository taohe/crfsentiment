package dependencytree;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.DataFormatException;

/**
 * Created by IntelliJ IDEA.
 * User: tao
 * Date: 5/11/13
 * Time: 4:36 PM
 * This class Load prior polarity (q_i) dictionary from features folder
 * store the dictionary into a hash set.
 */
public class LoadPriorPolarityFeature {
    private static final int InitMapCapacity = 8192;
    private static final int InitSetCapacity = 128;
    private static final String SubjectivityLexiconFileName =
            "subjclueslen1-HLTEMNLP05.txt";
    private static final String PolarReversalFileName =
            "polarReversal.txt";
    private Map<String, PriorPolarityValue> priorPolarityMap;
    private Set<String> polarityReversalSet;
    
    public LoadPriorPolarityFeature() {
        priorPolarityMap = new HashMap<String, PriorPolarityValue>(
                LoadPriorPolarityFeature.InitMapCapacity);
        polarityReversalSet = new HashSet<String>(LoadPriorPolarityFeature.InitSetCapacity);
    }

    /**
     * Read the polarity reversal dictionary and insert words into polarityReversalSet
     * @param isDebugging    If true, go for verbose
     * @throws IOException   For bufferedReader.readLine()
     */
    public void readPolarReversalDict(boolean isDebugging) throws IOException {
        InputStream inputStream = LoadPriorPolarityFeature.class
                .getResourceAsStream("/features/" + LoadPriorPolarityFeature.PolarReversalFileName);
        InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
        String dictLine;

        int count = 0;
        while ((dictLine = bufferedReader.readLine()) != null) {
            if (isDebugging)
                System.out.println(dictLine);

            if (polarityReversalSet.add(dictLine.trim()))  // Only increase count if insert succeed
                count++;

            if (isDebugging) {
                System.out.println("Inserted word=  " + dictLine.trim());
            }
        }

        System.out.println("Total words in Polarity Reversal Dict: " + count);
        try {
            if (bufferedReader != null)
                bufferedReader.close();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    public void readPriPolarDict(boolean isDebugging) throws Exception {
        InputStream inputStream = LoadPriorPolarityFeature.class
                .getResourceAsStream("/features/" + LoadPriorPolarityFeature.SubjectivityLexiconFileName);
        InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
        String dictLine;

        int count = 0;
        try {
            //while (count++ < 5 && (dictLine = bufferedReader.readLine()) != null) {
            while ((dictLine = bufferedReader.readLine()) != null) {
                if (isDebugging)
                    System.out.println(dictLine);

                Pattern p = Pattern.compile(
                        "type=(\\w+).*word1=(\\w+).*pos1=(\\w+).*stemmed1=(\\w).*priorpolarity=(\\w+)");
                Matcher m = p.matcher(dictLine);
                if (!m.matches()) {
                    throw new DataFormatException("Prior Polarity Lexicon Format Match Exception at line: " + count);
                } else {
                    PriorPolarityValue priorPolarityValue = new PriorPolarityValue(m.group(5), m.group(1),
                            m.group(3), m.group(4));
                    priorPolarityMap.put(m.group(2), priorPolarityValue);

                    if (isDebugging) {
                        System.out.println("Inserted key=  " + m.group(2) + "   Value==> priPolar: "
                                + priorPolarityValue.priorPolarity + " subType: " + priorPolarityValue.subjectiveType
                                + " pos: " + priorPolarityValue.posTag + " stemmed: " + priorPolarityValue.isStemmed);
                    }
                }
                count++;
            }
            System.out.println("Total words in PriorPolarity Dict: " + count);
        } catch (IOException ioe) {
            ioe.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } finally {
            try {
                if (bufferedReader != null)
                    bufferedReader.close();
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }

        }
    }

    /**
     * Prior Polarity Map getter
     * @return   priorPolarityMap loaded from a subjectivity lexicon
     */
    public Map<String, PriorPolarityValue> getPriorPolarityMap() {
        return priorPolarityMap;
    }

    /**
     * Polarity Reversal Set getter
     * @return   polarityReversalSet loaded from a polar reversal dictionary
     */
    public Set<String> getPolarityReversalSet() {
        return polarityReversalSet;
    }

}
