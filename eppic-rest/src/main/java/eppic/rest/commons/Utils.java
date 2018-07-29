package eppic.rest.commons;

import eppic.commons.util.IntervalSet;
import java.util.SortedSet;

public class Utils {

    /**
     * Parses a string of integer ids into the expanded composing Set of integers.
     * @param idsString a String with a list of integer ids comma separated and summarized
     *                  by use of hyphens, e.g. "1-5,9,12-15"
     * @return
     * a sorted set of all integers contained in given string
     * or null (meaning all ids requested)
     */
    public static SortedSet<Integer> parseIdsString(String idsString) {

        // If null or '*', return null (all)

        if (idsString == null || idsString.equals("*")) {
            // null -> return null, meaning all available ids
            return null;

        }

        return new IntervalSet(idsString).getMergedIntervalSet().getIntegerSet();
    }

}
