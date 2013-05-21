package dependencytree;

import iitb.CRF.DataIter;
import iitb.CRF.DataSequence;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNull;

/**
 * Created by IntelliJ IDEA.
 * User: tao
 * Date: 5/14/13
 * Time: 1:28 PM
 */
public class SentimentDataIter implements DataIter {
    private static final String Train_Data_Folder = "/resource";
    private static final String Test_Data_Folder = "/test_files";
    private LoadSourceToTrainData loadSourceToTrainData;
    private LoadPriorPolarityFeature loadPriorPolarityFeature;
    private List<ParserToDependTree> parserToDependTreeList;
    private List<SentimentTrainSequence> trainSequenceList;
    private List<SentimentTestSequence> testSequenceList;
    private int curDataSeqInd;
    private final boolean isTrain;   // Indicate whether this is train/test data

    /**
     * SentimentDataIter constructor, load data files when calling
     * @param isTrainData  If true, load files from "/resource" folder, otherwise "test_files" folder
     * @param loadPriorPolarityFeature    This class must be allocated and two dictionaries read!
     */
    public SentimentDataIter(boolean isTrainData, LoadPriorPolarityFeature loadPriorPolarityFeature) {
        this.loadSourceToTrainData = new LoadSourceToTrainData();
        this.loadPriorPolarityFeature = loadPriorPolarityFeature;
        this.parserToDependTreeList = null;
        this.trainSequenceList = null;
        this.testSequenceList = null;
        this.curDataSeqInd = 0;
        this.isTrain = isTrainData;

        scanFilesLoadData(isTrainData);
        if (isTrainData) {
            Collections.shuffle(this.trainSequenceList);
        } else {
            Collections.shuffle(this.testSequenceList);
        }
    }

    /**
     * This function is for cross-validation
     * @param isTrainData
     * @param parentDataIter     DataIter, must be a Train sequence DataIter
     */
    public SentimentDataIter(boolean isTrainData, SentimentDataIter parentDataIter, int fromInd, int toInd) {
        this.loadSourceToTrainData = null;
        this.loadPriorPolarityFeature = null;
        this.parserToDependTreeList = null;
        this.isTrain = isTrainData;
        this.curDataSeqInd = 0;
        if (isTrainData) {  // This sub-list is for train data
            this.testSequenceList = null;
            this.trainSequenceList = parentDataIter.getTrainSequenceList().subList(fromInd, toInd);
        } else {
            this.trainSequenceList = null;
            this.testSequenceList = SentimentTestSequence.constrTestSeqsFromTrainSeqs(
                    parentDataIter.getTrainSequenceList().subList(fromInd, toInd));
        }
    }

    /**
     * This function is for cross-validation, only for generating 2-chunks TrainDataIter
     * The two chunks are [firFromInd, firToInd), [secFromInd, secToInd)
     * @param parentDataIter      DataIter, must be a Train sequence DataIter
     * @param firFromInd
     * @param firToInd
     * @param secFromInd
     * @param secToInd
     */
    public SentimentDataIter(SentimentDataIter parentDataIter, int firFromInd, int firToInd, int secFromInd,
                             int secToInd) {
        this.loadSourceToTrainData = null;
        this.loadPriorPolarityFeature = null;
        this.parserToDependTreeList = null;
        this.isTrain = true;   // This constructor is only for generating trainDataIter
        this.curDataSeqInd = 0;
        this.testSequenceList = null;
        this.trainSequenceList = new ArrayList<SentimentTrainSequence>(firToInd + secToInd - firFromInd - secFromInd);
        this.trainSequenceList.addAll(parentDataIter.getTrainSequenceList().subList(firFromInd, firToInd));
        this.trainSequenceList.addAll(parentDataIter.getTrainSequenceList().subList(secFromInd, secToInd));
    }

    /**
     * Scan train files under directory src/resource/ or src/test_files,
     * load train data to parserToDependTreeList and trainSequenceList
     * @param isTrainData   If true, load Train data, otherwise load Test data
     */
    public void scanFilesLoadData(boolean isTrainData) {
        System.out.println("isTrainData = " + isTrainData);
        String dataFolder;
        if (isTrainData)
            dataFolder = SentimentDataIter.Train_Data_Folder;
        else
            dataFolder = SentimentDataIter.Test_Data_Folder;
        System.out.println("Load data from folder: " + dataFolder);
        int dataLength = loadSourceToTrainData.readAllFilesInResource(Integer.MAX_VALUE,
                dataFolder,
                false);
        parserToDependTreeList = loadSourceToTrainData.getParserToDependTreeList();

        if (isTrainData) {
            trainSequenceList = new ArrayList<SentimentTrainSequence>(dataLength);
            for (ParserToDependTree parserToDependTree : parserToDependTreeList) {
                trainSequenceList.add(new SentimentTrainSequence(parserToDependTree,
                        loadPriorPolarityFeature));
            }
            System.out.println("Number of Train sequences loaded: " + dataLength);
        } else {
            testSequenceList = new ArrayList<SentimentTestSequence>(dataLength);
            for (ParserToDependTree parserToDependTree : parserToDependTreeList) {
                testSequenceList.add(new SentimentTestSequence(parserToDependTree,
                        loadPriorPolarityFeature));
            }
            System.out.println("Number of Test sequences loaded: " + dataLength);
        }

        // Assertions for correctness check
        assertEquals(dataLength, parserToDependTreeList.size());
        if (isTrainData)
            assertNull(testSequenceList);
        else
            assertNull(trainSequenceList);
    }

    /**
     * Reset curDataSeqInd to 0, in order to re-scan data
     */
    public void startScan() {
        curDataSeqInd = 0;
    }

    public boolean hasNext() {
        int size = (isTrain)? trainSequenceList.size() : testSequenceList.size();
        if (curDataSeqInd < size) {
            return true;
        } else {
            return false;
        }
    }

    public DataSequence next() {
        if (isTrain) {
            return trainSequenceList.get(curDataSeqInd++);
        } else {
            return testSequenceList.get(curDataSeqInd++);
        }
    }

    public List<ParserToDependTree> getParserToDependTreeList() {
        return parserToDependTreeList;
    }

    public List<SentimentTrainSequence> getTrainSequenceList() {
        return trainSequenceList;
    }
    
    public List<SentimentTestSequence> getTestSequenceList() {
        return testSequenceList;
    }

    public int getCurDataSeqInd() {
        return curDataSeqInd;
    }

    public boolean getIsTrain() {
        return isTrain;
    }
}
