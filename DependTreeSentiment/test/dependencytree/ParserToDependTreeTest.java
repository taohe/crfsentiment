package dependencytree;

import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.ling.TaggedWord;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.trees.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class ParserToDependTreeTest {

    LexicalizedParser lp;
    Tree parseTree;

    @Before
    public void setUp() throws Exception {

    }

    @After
    public void tearDown() throws Exception {
    }


    @Test
    public void shouldGetHead() {
        String str = "I do not think the new ipad is worth to buy.";
        String str2 = "iPad is a magical window where nothing comes between you and what you love.";
        String str3 = "iPhone works better with the case.";

        System.out.println(getHead(str));
        System.out.println(getHead(str2));
        System.out.println(getHead(str3));
    }

    @Test
    /*
     * This is the main test function for reading a full sentence and convert it to word list, head index list
     * arrays and also set sentence polarity (from input)
     */
    public void parseASentenceUpdatePhraseListArgu() {
        String aSentence = "It prevents cancer and heart disease.";
        //String aSentence = "i recently purchased the canon powershot g3 and am extremely satisfied with the purchase .";
        ParserToDependTree parserToDependTree = new ParserToDependTree(aSentence, true, true);

        assertEquals(true, parserToDependTree.getPhraseListArgu().isPos);
        for (int i = 0; i < parserToDependTree.getPhraseListArgu().phraseList.length; i++) {
            System.out.println("Word: " + parserToDependTree.getPhraseListArgu().phraseList[i]
            + "  ind: " + i + "  parentInd: " + parserToDependTree.getPhraseListArgu().headIndList[i]
            + "  rel: " + parserToDependTree.getPhraseListArgu().grammarRel[i]);
        }
    }

    @Test
    public void parseASentenceGetTypedDependencies() {
        String aSentence = "It prevents cancer and heart disease.";
        ParserToDependTree parserToDependTree = new ParserToDependTree(aSentence, true, true);
        Tree tree = parserToDependTree.getParseTree();

        TreebankLanguagePack tlp = new PennTreebankLanguagePack();
        GrammaticalStructureFactory gsf = tlp.grammaticalStructureFactory();
        GrammaticalStructure gs = gsf.newGrammaticalStructure(tree);
        Collection<TypedDependency> tdl = gs.typedDependenciesCollapsedTree();
        System.out.println(tdl);
        for (TypedDependency td : tdl) {
            System.out.println("rel:" + td.reln() + "  gov:" + td.gov().label().value()
                    + "   dep:" + td.dep().label().value() + "  ind: " + td.dep().label().index());
        }
    }

    @Test
    public void directlyParseASentencePrintTree() {
        String aSentence = "It prevents cancer and heart disease.";
        ParserToDependTree parserToDependTree = new ParserToDependTree(aSentence, true, true);
        Tree tree = parserToDependTree.getParseTree();
        System.out.println("My new tree:----------------");
        tree.pennPrint();
        System.out.println("----------------");
    }

    @Test
    public void shouldBeAbleToSpecifyPOStag() {
        String[] tokens = {"I", "love", "cars"};
        ParserToDependTree sentenceParser = new ParserToDependTree(tokens);
        Tree tree = sentenceParser.getParseTree();
        tree.pennPrint();
        for (Tree leaf : tree.getLeaves()) {
            if (leaf.label().toString().equals("iPad")) {
                assertEquals("NN", leaf.parent().label().toString());
            }
        }

        System.out.println(tree);


        String[] anotherTokens = {"I","love","dd", "iPad->JJ"};
        sentenceParser = new ParserToDependTree(anotherTokens);
        tree = sentenceParser.getParseTree();
        tree.pennPrint();


//        List<TypedDependency> typedDependencies = sentenceParser.getTypedDependencies();
//        System.out.println(typedDependencies);
//        TreePrint.print(typedDependencies,new PrintWriter(System.out));

//        tree.pennPrint();
        for (Tree leaf : tree.getLeaves()) {
            if (leaf.label().toString().equals("iPad")) {
                assertEquals("JJ", leaf.parent().label().toString());
            }
        }

    }

    private String getHead(String str) {
        str = str.substring(0, str.length() - 1);
        ParserToDependTree parser = new ParserToDependTree(str.split(" "));
        parser.showTree();
        return parser.getHead();
    }

    @Test
    public void show() {
        LexicalizedParser lp = LexicalizedParser.loadModel("edu/stanford/nlp/models/lexparser/englishPCFG.ser.gz");
        //lp.setOptionFlags(new String[]{"-maxLength", "80", "-retainTmpSubcategories"});

//        String str="I do not like ipad .";
        String str = "I am passionate about Microsoft technologies especially Silverlight";
        str = "What kind of book do you like ?";
        String[] sent = str.split(" ");
        List<HasWord> rawWords = new ArrayList<HasWord>();
        for (String word : sent) {
            CoreLabel l = new CoreLabel();
            if (word.equals("iPad")) {
                TaggedWord tword = new TaggedWord(word, "NN");
                rawWords.add(tword);
            } else {
                l.setWord(word);
                rawWords.add(l);
            }
        }

        Tree parse = lp.apply(rawWords);

        //parse.pennPrint();

        TreebankLanguagePack tlp = new PennTreebankLanguagePack();
        GrammaticalStructureFactory gsf = tlp.grammaticalStructureFactory();
        GrammaticalStructure gs = gsf.newGrammaticalStructure(parse);
        List<TypedDependency> tdl = gs.typedDependenciesCCprocessed();
        System.out.println(tdl);
        for (TypedDependency td : tdl) {
            System.out.println("rel:" + td.reln() + "  gov:" + td.gov().label().value() + "   dep:" + td.dep().label().value());
        }

        //System.out.println();

        // TreePrint tp = new TreePrint("penn,typedDependenciesCollapsed");
        //tp.printTree(parse);
    }

}