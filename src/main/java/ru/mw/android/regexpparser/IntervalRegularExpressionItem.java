package ru.mw.android.regexpparser;

import android.text.Editable;

import java.util.ArrayList;

/**
 * Created by nixan on 9/9/13.
 */
public class IntervalRegularExpressionItem extends RegularExpressionItem {

    private interface Condition {

        public boolean check(char item);
    }

    private static class IntervalCondition implements Condition {

        private final char mFrom;

        private final char mTo;

        public IntervalCondition(char from, char to) {
            mFrom = from;
            mTo = to;
        }

        @Override
        public String toString() {
            return mFrom + "-" + mTo;
        }

        @Override
        public boolean check(char item) {
            return mFrom <= item && item <= mTo;
        }

    }

    private static class StaticCondition implements Condition {

        private final char mStatic;

        public StaticCondition(char staticChar) {
            mStatic = staticChar;
        }

        @Override
        public String toString() {
            return String.valueOf(mStatic);
        }

        @Override
        public boolean check(char item) {
            return mStatic == item;
        }
    }

    private final ArrayList<Condition> mConditions;

    public IntervalRegularExpressionItem(String data, int minLength, int maxLength) {
        super(minLength, maxLength);
        mConditions = parseConditions(data);
    }

    private ArrayList<Condition> parseConditions(String data) {
        ArrayList<Condition> result = new ArrayList<Condition>();
        int i = 0;
        while (i < data.length()) {
            char thisChar = data.charAt(i);
            if (data.length() > i + 1 && data.charAt(i) == '\\' && data.charAt(i + 1) == 's') {
                result.add(new StaticCondition(' '));
                i += 2;
            } else if (data.length() > i + 1 && data.charAt(i) == '\\') {
                result.add(new StaticCondition(data.charAt(i + 1)));
                i += 2;
            } else if (data.length() > i + 1 && data.charAt(i + 1) == '-'
                    && data.length() > i + 2) {
                char nextChar = data.charAt(i + 2);
                result.add(new IntervalCondition(thisChar, nextChar));
                i += 3;
            } else {
                result.add(new StaticCondition(thisChar));
                i++;
            }
        }
        return result;
    }

    @Override
    public BlockType getBlockType() {
        return BlockType.INTERVAL;
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        result.append('[');
        for (Condition condition : mConditions) {
            result.append(condition.toString());
        }
        result.append(']');
        if (isMaxLengthUnlimited()) {
            if (getMinLength() == 0) {
                result.append('*');
            } else {
                result.append('+');
            }
        } else {
            if (getMaxLength() == getMinLength()) {
                result.append('{').append(getMaxLength()).append('}');
            } else {
                result.append('{').append(getMinLength()).append(',').append(getMaxLength())
                        .append('}');
            }
        }
        return result.toString();
    }

    private boolean check(char item) {
        for (Condition condition : mConditions) {
            if (condition.check(item)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int format(Editable input, int startPosition) {
        return format(input, startPosition, input.length());
    }

    @Override
    public int format(Editable input, int startPosition, int endPosition) {
        int position = 0;
        while (position + startPosition < endPosition && (isMaxLengthUnlimited()
                || position < getMaxLength())) {
            boolean check = check(input.charAt(position + startPosition));
            if (check) {
                position++;
            } else if (!check && position < getMinLength()) {
                input.delete(startPosition + position, startPosition + position + 1);
                endPosition--;
            } else {
                return position;
            }
        }
        return position;
    }

    public int formatFromTheEnd(Editable input, int startPosition, int endPosition) {
        return format(input, startPosition, endPosition);
    }

    public int formatFromTheEnd(Editable input, int startPosition) {
        return format(input, startPosition);
    }

    @Override
    public MatchResult matches(String string, int startPosition, int endPosition) {
        int numberOfSymbolsMatched = 0;
        boolean inputIsShorter = false;
        boolean inputIsLonger = false;
        if (string.length() < startPosition) {
            return MatchResult.NO.setNumberOfSymbolsMatched(numberOfSymbolsMatched);
        }
        if (string.length() < endPosition) {
            inputIsShorter = true;
        }
        if (endPosition - startPosition < getMinLength()) {
            inputIsShorter = true;
        }
        if (!isMaxLengthUnlimited() && endPosition - startPosition > getMaxLength()) {
            inputIsLonger = true;
        }
        for (int i = startPosition; i < Math.min(string.length(), endPosition); i++) {
            if (!check(string.charAt(i))) {
                return MatchResult.SHORTER.setNumberOfSymbolsMatched(numberOfSymbolsMatched);
            } else {
                numberOfSymbolsMatched++;
            }
        }
        if (inputIsLonger) {
            return MatchResult.LONGER.setNumberOfSymbolsMatched(numberOfSymbolsMatched);
        } else if (inputIsShorter) {
            return MatchResult.SHORTER.setNumberOfSymbolsMatched(numberOfSymbolsMatched);
        } else {
            return MatchResult.FULL.setNumberOfSymbolsMatched(numberOfSymbolsMatched);
        }
    }

    @Override
    public String toReversedString() {
        return toString();
    }

    @Override
    public RegularExpressionItem reverse() {
        return this;
    }
}
