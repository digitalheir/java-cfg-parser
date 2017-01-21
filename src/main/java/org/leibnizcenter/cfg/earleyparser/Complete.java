package org.leibnizcenter.cfg.earleyparser;

import org.leibnizcenter.cfg.algebra.semiring.dbl.DblSemiring;
import org.leibnizcenter.cfg.algebra.semiring.dbl.ExpressionSemiring;
import org.leibnizcenter.cfg.algebra.semiring.dbl.Resolvable;
import org.leibnizcenter.cfg.category.Category;
import org.leibnizcenter.cfg.category.nonterminal.NonTerminal;
import org.leibnizcenter.cfg.earleyparser.chart.state.State;
import org.leibnizcenter.cfg.earleyparser.chart.statesets.StateSets;
import org.leibnizcenter.cfg.errors.IssueRequest;
import org.leibnizcenter.cfg.grammar.Grammar;
import org.leibnizcenter.cfg.rule.Rule;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Complete stage
 * <p>
 * Created by maarten on 31/10/16.
 */
public class Complete {
    /**
     * Don't instantiate
     */
    private Complete() {
        throw new Error();
    }

    /**
     * Completes states exhaustively and makes resolvable expressions for the forward and inner scores.
     * Note that these expressions can only be resolved to actual values after finishing completion, because they may depend on one another.
     *
     * @param position         State position
     * @param states           Completed states to use for deducing what states to proceed
     * @param addForwardScores Container / helper for adding to forward score expressions
     * @param addInnerScores   Container / helper for adding to inner score expressions
     *                         //     * @param completedStatesAlreadyHandled The completed states that we don't want to reiterate.
     *                         //     * @param computationsForward           Container for forward score expressions. Probably superfluous.
     *                         //     * @param computationsInner             Container for inner score expressions. Probably superfluous.
     */
    private static <E> void completeNoViterbi(final int position,
                                              final Collection<State> states,
                                              final DeferredStateScoreComputations addForwardScores,
                                              final DeferredStateScoreComputations addInnerScores,
                                              final Grammar<E> grammar,
                                              final StateSets<E> stateSets,
                                              final ExpressionSemiring semiring
    ) {
        DeferredStateScoreComputations newStates = null;
        // For all states
        //      i: Y<sub>j</sub> → v·    [a",y"]
        //      j: X<sub>k</suv> → l·Zm  [a',y']
        //
        //  such that the R*(Z =*> Y) is nonzero
        //  and Y → v is not a unit production
        for (State completedState : states) {
            final int j = completedState.ruleStartPosition;
            final NonTerminal Y = completedState.rule.getLeft();

            DeferredValue unresolvedCompletedInner = addInnerScores.getOrCreate(
                    completedState,
                    stateSets.innerScores.get(completedState)
            );

            final Collection<State> statesToAdvance = stateSets.activeStates.getStatesActiveOnNonTerminalWithNonZeroUnitStarScoreToY(j, Y);
            if (statesToAdvance != null) for (State stateToAdvance : statesToAdvance) {
                if (j != stateToAdvance.position) throw new IssueRequest("Index failed. This is a bug.");

                // Make i: X_k → lZ·m
                DeferredValue prevInner = addInnerScores.getOrCreate(stateToAdvance, stateSets.innerScores.get(stateToAdvance));
                DeferredValue prevForward = addForwardScores.getOrCreate(stateToAdvance, stateSets.forwardScores.get(stateToAdvance));

                final Category Z = stateToAdvance.getActiveCategory();

                Resolvable unitStarScore = new Atom(grammar.getUnitStarScore(Z, Y));
                Resolvable fw = new ExpressionSemiring.Times(semiring,
                        new ExpressionSemiring.Times(semiring, unitStarScore, prevForward),
                        unresolvedCompletedInner
                );

                Resolvable inner = new ExpressionSemiring.Times(semiring,
                        new ExpressionSemiring.Times(semiring, unitStarScore, prevInner)
                        , unresolvedCompletedInner);

                Rule newStateRule = stateToAdvance.rule;
                int newStateDotPosition = stateToAdvance.advanceDot();
                int newStateRuleStart = stateToAdvance.ruleStartPosition;
                State s = State.create(position, newStateRuleStart, newStateDotPosition, newStateRule);

                addForwardScores.plus(
                        s,
                        fw
                );

                // If this is a new completed state that is no unit production, make a note of it it because we want to recursively call *complete* on these states
                if (
                        newStateRule.isPassive(newStateDotPosition)/*isCompleted*/
                                && !newStateRule.isUnitProduction()
                                && !stateSets.contains(s)) {
                    if (newStates == null) newStates = new DeferredStateScoreComputations(semiring);
                    newStates.plus(
                            s,
                            fw
                    );
                }

                addInnerScores.plus(s,
                        inner
                );
            }
        }

        if (newStates != null) {
            Set<State> newCompletedStates = newStates.states.keySet();
            newCompletedStates.forEach(stateSets::getOrCreate);
            if (newCompletedStates.size() > 0) Complete.completeNoViterbi(
                    position,
                    newCompletedStates,
                    addForwardScores,
                    addInnerScores,
                    grammar,
                    stateSets,
                    semiring
            );
        }
    }

    /**
     * Makes completions in the specified chart at the given index.
     *
     * @param i The index to make completions at.
     */
    public static <E> void completeNoViterbi(
            final int i,
            final Grammar<E> grammar,
            final StateSets<E> stateSets
    ) {
        final ExpressionSemiring semiring = grammar.getSemiring();
        final DeferredStateScoreComputations addForwardScores = new DeferredStateScoreComputations(semiring);
        final DeferredStateScoreComputations addInnerScores = new DeferredStateScoreComputations(semiring);

        completeNoViterbi(
                i,
                stateSets.completedStates.getCompletedStatesThatAreNotUnitProductions(i),
                addForwardScores,
                addInnerScores,
                grammar, stateSets, semiring
        );

        // Resolve and set forward & inner scores
        addForwardScores.states.forEach(
                (s, score) ->
                        stateSets.forwardScores.put(
                                stateSets.getOrCreate(s, null),
                                score.resolve()//todo resolveFinal
                        )
        );

        addInnerScores.states.forEach(
                (s, score) ->
                        stateSets.innerScores.put(
                                stateSets.getOrCreate(s, null),
                                score.resolve()//todo resolveFinal
                        )
        );
    }

    /**
     * For finding the Viterbi path, we can't conflate production recursions (ie can't use the left star corner),
     * exactly because we need it to find the unique Viterbi path.
     * Luckily, we can avoid looping over unit productions because it only ever lowers probability
     * (assuming p = [0,1] and Occam's razor). ~This method does not guarantee a left most parse.~
     *
     * @param completedState Completed state to calculate Viterbi score for
     * @param sr             Semiring to use for calculating
     */
    @SuppressWarnings("WeakerAccess")
    public static <T> void computeViterbiScores(
            final State completedState,
            final DblSemiring sr,
            StateSets<T> stateSets
    ) {
        Collection<State> newStates = null; // init as null to avoid list creation
        Collection<State> newCompletedStates = null; // init as null to avoid list creation

        if (stateSets.viterbiScores.get(completedState) == null)
            throw new IssueRequest("Expected Viterbi score to be set on completed state. This is a bug.");

        final double completedViterbi = stateSets.viterbiScores.get(completedState).getScore();
        final NonTerminal Y = completedState.rule.getLeft();
        //Get all states in j <= i, such that <code>j: X<sub>k</sub> →  λ·Yμ</code>
        int completedPos = completedState.position;
        final Set<State> statesToAdvance = stateSets.activeStates.getStatesActiveOnNonTerminal(Y, completedState.ruleStartPosition, completedPos);
        if (statesToAdvance != null) for (State stateToAdvance : statesToAdvance) {
            if (stateToAdvance.position > completedPos || stateToAdvance.position != completedState.ruleStartPosition)
                throw new IssueRequest("Index failed. This is a bug.");
            int ruleStart = stateToAdvance.ruleStartPosition;
            int nextDot = stateToAdvance.advanceDot();
            Rule rule = stateToAdvance.rule;
            State resultingState = State.create(completedPos, ruleStart, nextDot, rule);
            if (!stateSets.contains(resultingState)) {
                if (newStates == null) newStates = new HashSet<>(20);
                newStates.add(resultingState);
            }
            final State.ViterbiScore viterbiScore = stateSets.viterbiScores.get(resultingState);
            final State.ViterbiScore prevViterbi = stateSets.viterbiScores.get(stateToAdvance);
            if (prevViterbi == null) throw new Error("Expected viterbi to be set for " + stateToAdvance);
            final double prev = prevViterbi.getScore();
            final State.ViterbiScore newViterbiScore = new State.ViterbiScore(sr.times(completedViterbi, prev), completedState, resultingState, sr);

            if (viterbiScore == null || viterbiScore.compareTo(newViterbiScore) < 0) {
                stateSets.viterbiScores.put(newViterbiScore);

                if (resultingState.isCompleted()) {
                    if (newCompletedStates == null) newCompletedStates = new HashSet<>(20);
                    newCompletedStates.add(resultingState);
                }
            }
        }

        // Add new states to chart
        if (newStates != null)
            newStates.forEach(stateSets::addIfNew);

        // Recurse with new states that are completed
        if (newCompletedStates != null)
            for (State resultingState : newCompletedStates)
                computeViterbiScores(resultingState, sr, stateSets);

    }
}