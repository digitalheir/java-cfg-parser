package org.leibnizcenter.cfg.earleyparser.chart;

import org.leibnizcenter.cfg.Grammar;
import org.leibnizcenter.cfg.algebra.semiring.dbl.DblSemiring;
import org.leibnizcenter.cfg.category.terminal.Terminal;
import org.leibnizcenter.cfg.token.Token;
import org.leibnizcenter.cfg.token.TokenWithCategories;
import org.leibnizcenter.cfg.earleyparser.chart.state.State;
import org.leibnizcenter.cfg.earleyparser.parse.ScanProbability;
import org.leibnizcenter.cfg.errors.IssueRequest;

import java.util.Set;

/**
 * Scan phase of Earley
 *
 * Created by maarten on 31/10/16.
 */
@SuppressWarnings("WeakerAccess")
public final class Scan {
    /**
     * Don't instantiate
     */
    private Scan() {
        throw new Error();
    }

    static <T> void scan(final int tokenPosition, TokenWithCategories<T> tokenWithCategories, ScanProbability scanProbability, Grammar<T> grammar, StateSets stateSets) {
        if (tokenWithCategories == null) throw new IssueRequest("null token at index " + tokenPosition + ". This is a bug");

        final double scanProb = scanProbability == null ? Double.NaN : scanProbability.getProbability(tokenPosition);
        final DblSemiring sr = grammar.getSemiring();
        final Token<T> token = tokenWithCategories.getToken();
        tokenWithCategories.getCategories().forEach(terminalType ->{
        /*
         * Get all states that are active on a terminal
         *   O(|stateset(i)|) = O(|grammar|): For all states <code>i: X<sub>k</sub> → λ·tμ</code>, where t is a terminal that matches the given token...
         */
        // noinspection unchecked
            Set<State> statesActiveOnTerminals = stateSets.getStatesActiveOnTerminals(tokenPosition, terminalType);
            if(statesActiveOnTerminals!= null) statesActiveOnTerminals
                .forEach(preScanState -> {
                            //noinspection unchecked
                            if(!((Terminal<T>) preScanState.getActiveCategory()).hasCategory(token)) throw new IssueRequest("This is a bug.");
                // Create the state <code>i+1: X<sub>k</sub> → λt·μ</code>
                    /*
                     * All these methods are synchronized
                     */
                            final double preScanForward = stateSets.getForwardScore(preScanState);
                            final double preScanInner = stateSets.getInnerScore(preScanState);
                            // Note that this state is unique for each preScanState
                            final State postScanState = stateSets.getOrCreate(
                                    tokenPosition + 1, preScanState.getRuleStartPosition(),
                                    preScanState.advanceDot(),
                                    preScanState.getRule(),
                                    token
                            );

                            // Set forward score //synchronized
                            stateSets.setForwardScore(
                                    postScanState,
                                    calculateForwardScore(scanProb, sr, preScanForward)
                            );

                            // Get inner score (no side effects)
                            final double postScanInner = calculateInnerScore(scanProb, sr, preScanInner);
                            // Set inner score //synchronized
                            stateSets.setInnerScore(
                                    postScanState,
                                    postScanInner
                            );

                            // Set Viterbi score//synchronized
                            stateSets.setViterbiScore(new State.ViterbiScore(postScanInner, preScanState, postScanState, sr));
                        }
                );
    });}

    /**
     * Function to calculate the new inner score from given values
     *
     * @param scanProbability The probability of scanning this particular token
     * @param sr              The semiring to calculate with
     * @param previousInner   The previous inner score
     * @return The inner score for the new state
     */
    private static double calculateInnerScore(double scanProbability, DblSemiring sr, double previousInner) {
        if (Double.isNaN(scanProbability)) return previousInner;
        else return sr.times(previousInner, scanProbability);
    }

    /**
     * Function to compute the forward score for the new state after scanning the given token.
     *
     * @param scanProbability           The probability of scanning this particular token
     * @param sr                        The semiring to calculate with
     * @param previousStateForwardScore The previous forward score
     * @return Computed forward score for the new state
     */
    private static double calculateForwardScore(double scanProbability, DblSemiring sr, double previousStateForwardScore) {
        if (Double.isNaN(scanProbability)) return previousStateForwardScore;
        else return sr.times(previousStateForwardScore, scanProbability);
    }
}