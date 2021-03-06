
package org.leibnizcenter.cfg.earleyparser;

import org.junit.Assert;
import org.junit.Test;
import org.leibnizcenter.cfg.category.Category;
import org.leibnizcenter.cfg.category.nonterminal.NonTerminal;
import org.leibnizcenter.cfg.category.terminal.Terminal;
import org.leibnizcenter.cfg.category.terminal.stringterminal.ExactStringTerminal;
import org.leibnizcenter.cfg.grammar.Grammar;

import java.util.LinkedList;

/**
 */
public class ParseTreeTest {
    private static final NonTerminal S = Category.nonTerminal("S");
    private static final NonTerminal NP = Category.nonTerminal("NP");
    private static final NonTerminal VP = Category.nonTerminal("VP");
    private static final NonTerminal Det = Category.nonTerminal("Det");
    private static final NonTerminal N = Category.nonTerminal("N");

    private static final NonTerminal VS = Category.nonTerminal("VS");
    private static final NonTerminal VI = Category.nonTerminal("VI");
    private static final NonTerminal VT = Category.nonTerminal("VT");
    private static final Terminal
            saw = new ExactStringTerminal("saw");
    private static final Terminal duck = new ExactStringTerminal("duck");
    private static final Terminal her = new ExactStringTerminal("her");
    private static final Terminal he = new ExactStringTerminal("he");


    @Test
    public final void testParseTrees() {
//    final Grammar<String> myGrammar = new Grammar.Builder<String>("test")
//            .addRule(S, NP, VP)
//            .addRule(NP, he)
//            .addRule(NP, her)
//            .addRule(NP, Det, N)
//            .addRule(VT, saw)
//            .addRule(VS, saw)
//            .addRule(VI, duck)
//            .addRule(N, duck)
//            .addRule(Det, her)
//            .addRule(VP, VT, NP)
//            .addRule(VP, VS, S)
//            .addRule(VP, VI).build();
//        List<Token<String>> tokens = new ArrayList<>();
//        tokens.add(new Token<>("he"));
//        tokens.add(new Token<>("saw"));
//        tokens.add(new Token<>("her"));
//        tokens.add(new Token<>("duck"));
//
//         TODO implement treewalker
//        parseTrees = new Parser<>(myGrammar).getParses(S, tokens);
//        // structural ambiguity reflected?
//        Assert.assertEquals("problem with parse trees: " + parseTrees,
//                2, parseTrees.size());
//
//        // sub trees
//        Set<ParseTree> vpSubTrees = parse.getParseTreesFor(VP, 1, 4);
//        Assert.assertEquals("problem with VP sub trees: " + vpSubTrees,
//                2, vpSubTrees.size());
//
//        // should contain all parse trees at index 4
//        parse.chart.getEdges(4).stream()
//                .filter(edge -> edge.origin == 1 && edge.isPassive() && edge.rule.left.equals(VP))
//                .forEach(edge -> {
//                    ParseTree pt = parse.getParseTreeFor(edge);
//                    Assert.assertTrue("VP sub trees does not contain: " + pt,
//                            vpSubTrees.contains(pt));
//                });
//
//        // VI "duck" at end
//        Set<ParseTree> viSubTrees = parse.getParseTreesFor(VI, 3, 4);
//        Assert.assertEquals("problem with VI sub trees: " + viSubTrees,
//                1, viSubTrees.size());
//
//        Set<ParseTree> npSubTrees = parse.getParseTreesFor(NP, 0, 1);
//        Assert.assertEquals("problem with NP sub trees: " + npSubTrees,
//                1, npSubTrees.size());
//
//        // NP "her duck"
//        Set<ParseTree> npSubTrees2 = parse.getParseTreesFor(NP, 2, 4);
//        Assert.assertEquals("problem with NP sub trees: " + npSubTrees2,
//                1, npSubTrees2.size());
//
//        // sentential complement "her duck"
//        Set<ParseTree> sSubTrees = parse.getParseTreesFor(S, 2, 4);
//        Assert.assertEquals("problem with S sub trees: " + sSubTrees,
//                1, sSubTrees.size());
//
//        ParseTree sSubTree = sSubTrees.iterator().next();
//        ParseTree[] sChildren = sSubTree.getChildren();
//        //Iterator<ParseTree> sci = sChildren.iterator();
//        Assert.assertEquals(NP, sChildren[0].getCategory());
//        ParseTree sVPSubTree = sChildren[1];
//        Assert.assertEquals(VP, sVPSubTree.getCategory());
//        ParseTree viSubTree = sVPSubTree.getChildren()[0];
//        Assert.assertEquals(VI, viSubTree.getCategory());
//        ParseTree duckSubTree = viSubTree.getChildren()[0];
//        Assert.assertEquals(duck, duckSubTree.getCategory());
//
//        // back up
//        Assert.assertEquals(viSubTree, duckSubTree.getParent());
//        Assert.assertEquals(sVPSubTree, viSubTree.getParent());
//        Assert.assertEquals(sSubTree, sVPSubTree.getParent());
//        Assert.assertTrue(sSubTree.getParent() == null);
//
//        // wrong stuff in seed
//        Assert.assertEquals(Collections.emptySet(),
//                parse.getParseTreesFor(NP, 0, tokens.size()));
    }

    @Test
    public final void testNewParseTree() {
        final LinkedList<ParseTree> children1 = new LinkedList<>();
        final LinkedList<ParseTree> children2 = new LinkedList<>();
        final ParseTree tree1 = new ParseTree.NonLeaf(S, children1);
        final ParseTree tree2 = new ParseTree.NonLeaf(S, children2);

        Assert.assertEquals(tree1, tree2);
        children1.add(new ParseTree.NonLeaf(VS));
        children1.add(new ParseTree.NonLeaf(VT));
        Assert.assertNotEquals(tree1, tree2);
        children2.add(new ParseTree.NonLeaf(VS));
        children2.add(new ParseTree.NonLeaf(VT));
        Assert.assertEquals(tree1, tree2);

        Assert.assertEquals(tree1.getCategory(), S);
        Assert.assertEquals(tree1.getChildren(), children2);
    }
}
