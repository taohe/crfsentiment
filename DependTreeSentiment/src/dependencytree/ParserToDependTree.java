/**
 * Created by IntelliJ IDEA.
 * User: tao
 * Date: 5/10/13
 * Time: 12:02 PM
 */
package dependencytree;

import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.ling.TaggedWord;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.trees.*;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ParserToDependTree {
    private static Logger log= Logger.getLogger(ParserToDependTree.class);
    private static LexicalizedParser lexicalizedParser =
            LexicalizedParser.loadModel("edu/stanford/nlp/models/lexparser/englishPCFG.ser.gz");

    private String[] sentTokens;
    private Tree parseTree;
    private PhraseListArgu phraseListArgu = null;  // Auto set by the second constructor

    public ParserToDependTree(String[] sentTokens) {
        this.sentTokens = sentTokens;
        List<HasWord> rawWords = new ArrayList<HasWord>();
        for (String word : sentTokens) {
            if (word.contains("->")) {
                //the word is already tagged, like iPad->NN
                String[] items = word.split("->");
                String tagOfWord = items[1];
                String actualWord = items[0];
                TaggedWord tword = new TaggedWord(actualWord, tagOfWord);
                rawWords.add(tword);
            } else {
                CoreLabel l = new CoreLabel();
                l.setWord(word);
                rawWords.add(l);
            }
        }
        parseTree = lexicalizedParser.apply(rawWords);
    }

    // Get a parsed dependence tree object directly from a sentence
    public ParserToDependTree(String aSentence, boolean isPositive, boolean isDebugging) {
        this(aSentence.split(" "));
        String[] inputWordList = aSentence.split(" ");
        this.phraseListArgu = new PhraseListArgu(inputWordList.length, isPositive);
        this.phraseListArgu.phraseList[0] = "ROOT";   // Index starts from zero
        for (int i = 1; i < inputWordList.length + 1; i++) {
            this.phraseListArgu.phraseList[i] = inputWordList[i-1];
        }
        this.phraseListArgu.isPos = isPositive;
        if (isDebugging) {
            System.out.println("Sentiment:  " + this.phraseListArgu.isPos);
        }
        this.setPhraseListArgu(isDebugging);
    }

    /*
     * Use stanford-parser to generate typedDependence tree, then set
     * phrases list, head index list and sentence polarity to phraseListArgu object
     */
    public void setPhraseListArgu(boolean isDebugging) {
        Tree tree = this.getParseTree();

        TreebankLanguagePack tlp = new PennTreebankLanguagePack();
        GrammaticalStructureFactory gsf = tlp.grammaticalStructureFactory();
        GrammaticalStructure gs = gsf.newGrammaticalStructure(tree);
        Collection<TypedDependency> tdl = gs.typedDependenciesCollapsedTree();
        if (isDebugging) {
            System.out.println(tdl + "\n");
        }
        for (TypedDependency td : tdl) {
            if (td.dep().label().index() != td.gov().label().index()) {  // Omit-self loop -- Tree structure
                this.phraseListArgu.headIndList[td.dep().label().index()] =
                        td.gov().label().index();
            }
            this.phraseListArgu.grammarRel[td.dep().label().index()] = td.reln().toString();
            //System.out.println("rel:" + td.reln() + "  gov:" + td.gov().label().value()
            //        + "   dep:" + td.dep().label().value() + "  ind: " + td.dep().label().index());

        }
    }

    public String getHead() {
        return parseTree.headTerminal(new SemanticHeadFinder()).toString();
    }

    public List<TypedDependency> getTypedDependencies() {
        TreebankLanguagePack tlp = new PennTreebankLanguagePack();
        GrammaticalStructureFactory gsf = tlp.grammaticalStructureFactory();
        GrammaticalStructure gs = gsf.newGrammaticalStructure(parseTree);
        List<TypedDependency> tdl = gs.typedDependenciesCCprocessed();
        return tdl;
    }

    public Tree getParseTree() {
        return this.parseTree;
    }

    public PhraseListArgu getPhraseListArgu() {
        return this.phraseListArgu;
    }

    public String toString() {
        return parseTree.toString();
    }

    public void showTree() {
        log.info("--------------------");
        parseTree.pennPrint();
        log.info("....................");

        log.info(parseTree.flatten());
    }
}
