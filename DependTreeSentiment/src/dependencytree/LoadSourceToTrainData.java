package dependencytree;

import org.apache.commons.io.Charsets;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by IntelliJ IDEA.
 * User: tao
 * Date: 5/10/13
 * Time: 5:54 PM
 * This class load original review data from "resource" folder and convert
 * then output formatted train data
 */
public class LoadSourceToTrainData {
    private static int maxLines;
    private List<ParserToDependTree> parserToDependTreeList;

    public LoadSourceToTrainData() {
        parserToDependTreeList = new LinkedList<ParserToDependTree>();
    }

    /**
     * Returns total number of labeled-sentences extracted from source-data file
     *
     *
     * @param  maxLinePerFile   restriction on number of labeled-sentenced can be
     *                          extracted from each source-data-file
     * @param folderStr            Read all files under "folderStr"
     *                          For train: folderStr= "/resource"; For test: folderStr= "/test_files"
     * @param  isDebugging      If true, go for verbose mode
     * @return                  total number of labeled-sentences extracted
     */
    public int readAllFilesInResource(int maxLinePerFile, String folderStr, boolean isDebugging) {
        URL folderURL = LoadSourceToTrainData.class.getResource(folderStr);
        String folderPath = folderURL.getPath();
        File folder = new File(folderPath);
        File[] listOfFiles = folder.listFiles();
        System.out.println("Num of train data files = " + listOfFiles.length);
        int totalAnnotatedSentences = 0;
        LoadSourceToTrainData.maxLines = maxLinePerFile;

        for (int i = 0; i < listOfFiles.length; i++) {
        //for (int i = 0; i < 1; i++) {
            File file = listOfFiles[i];
            if (file.isFile() && file.getName().endsWith(".txt")) {
                System.out.println("Loading data from file: " + file.getName());

                try {
                    List<String> contents = FileUtils.readLines(file, Charsets.UTF_8);
                    int outputLineCount = 0;
                    for (String aLine : contents) {
                        Pattern p = Pattern.compile(".*\\[([+-]\\d+)\\].*##\\W*(.*)");
                        Matcher m = p.matcher(aLine);
                        if (!aLine.isEmpty() && m.matches()) {
                            if (outputLineCount < LoadSourceToTrainData.maxLines) {
                                System.out.println("Original:   " + aLine);
                                System.out.println("Extracted:   Score: " + m.group(1) + "  ==> " + m.group(2));
                                if (Integer.parseInt(m.group(1)) > 0) {  // A positive review
                                    parserToDependTreeList.add(new ParserToDependTree(m.group(2), true, isDebugging));
                                } else {
                                    parserToDependTreeList.add(new ParserToDependTree(m.group(2), false, isDebugging));
                                }
                                totalAnnotatedSentences++;
                                outputLineCount++;
                            }
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
            }
            System.out.println("TotalAnnotatedSentenced = " + totalAnnotatedSentences + "\n");
        }

        return totalAnnotatedSentences;
    }

    /**
     * ParserToDependTreeList getter
     * @return     parserToDependTreeList of type List
     */
    public List<ParserToDependTree> getParserToDependTreeList() {
        return parserToDependTreeList;
    }

    public static void main(String[] args) {
        LoadSourceToTrainData loadSourceToTrainData = new LoadSourceToTrainData();
        int totalTrainData = loadSourceToTrainData.readAllFilesInResource(Integer.MAX_VALUE, "/resource", false);
        System.out.println("Total trained data = " + totalTrainData);
    }
}
