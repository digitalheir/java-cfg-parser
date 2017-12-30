package org.leibnizcenter.cfg.util;

import org.leibnizcenter.cfg.category.nonterminal.NonTerminal;
import org.leibnizcenter.cfg.earleyparser.chart.state.State;

import java.util.*;

/**
 * Collection utils
 * Created by maarten on 8-2-17.
 */
public class Collections2 {
    public static <T> boolean nullOrEmpty(final Collection<T> collection) {
        return collection == null || collection.isEmpty();
    }

    public static <T> boolean isFilled(final Collection<T> collection) {
        return !nullOrEmpty(collection);
    }

    public static <T> Collection<T> emptyIfNull(final Collection<T> collection) {
        return collection == null ? Collections.emptySet() : collection;
    }

    /**
     * Runs in amortized constant time
     */
    public static <T> void add(final List<Set<T>> states, final int position, final T state) {
        Set<T> stateSet = getOrInitEmptySet(states, position);
        stateSet.add(state);
    }

    /**
     * Runs in amortized constant time
     */
    public static <T> T addSafe(final List<T> states, final int position, final T e) {
        if (states.size() < position) {
            states.addAll(Collections.nCopies(position - states.size() + 1, null));
            states.set(position, e);
        } else if (states.size() == position) states.add(e);
        else {
            final T el = states.get(position);
            if (el == null) {
                states.set(position, e);
            }
            return el;
        }
        return null;
    }


    /**
     * Runs in amortized constant time
     */
    public static <T, V> void add(final List<MyMultimap<T, V>> states, final int position, final T state, final V v) {
        final MyMultimap<T, V> stateSet = getOrInitEmptyMultimap(states, position);
        stateSet.put(state, v);
    }

    private static <T, V> MyMultimap<T, V> getOrInitEmptyMultimap(final List<MyMultimap<T, V>> states, final int position) {
        MyMultimap<T, V> stateSet;
        if (states.size() < position) {
            stateSet = new MyMultimap<>();
            states.addAll(Collections.nCopies(position - states.size() + 1, null));
            states.set(position, stateSet);
        } else if (states.size() == position) {
            stateSet = new MyMultimap<>();
            states.add(stateSet);
        } else {
            stateSet = states.get(position);
            if (stateSet == null) {
                stateSet = new MyMultimap<>();
                states.set(position, stateSet);
            }
        }
        return stateSet;
    }


    public static <T, V> Map<T, V> getOrInitEmptyMap(final List<Map<T, V>> states, final int position) {
        Map<T, V> stateSet;
        if (states.size() < position) {
            stateSet = new HashMap<>();
            states.addAll(Collections.nCopies(position - states.size() + 1, null));
            states.set(position, stateSet);
        } else if (states.size() == position) {
            stateSet = new HashMap<>();
            states.add(stateSet);
        } else {
            stateSet = states.get(position);
            if (stateSet == null) {
                stateSet = new HashMap<>();
                states.set(position, stateSet);
            }
        }
        return stateSet;
    }

    public static <T> Set<T> getOrInitEmptySet(final List<Set<T>> states, final int position) {
        Set<T> stateSet;
        if (states.size() < position) {
            stateSet = new HashSet<>();
            states.addAll(Collections.nCopies(position - states.size() + 1, null));
            states.set(position, stateSet);
        } else if (states.size() == position) {
            stateSet = new HashSet<>();
            states.add(stateSet);
        } else {
            stateSet = states.get(position);
            if (stateSet == null) {
                stateSet = new HashSet<>();
                states.set(position, stateSet);
            }
        }
        return stateSet;
    }
}
