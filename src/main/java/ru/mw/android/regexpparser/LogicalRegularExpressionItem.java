package ru.mw.android.regexpparser;

import android.text.Editable;
import android.text.SpannableStringBuilder;

import java.util.ArrayList;

/**
 * Created by nixan on 9/10/13.
 */
public class LogicalRegularExpressionItem extends RegularExpressionItem {

    private class LogicalRegularExpressionLength extends RegularExpressionLength {

        @Override
        public int getMinLength() {
            int minimum = LENGTH_UNLIMITED;
            for (RegularExpression expression : mVariants) {
                int minLength = expression.getMinLength();
                if (expression.isMinLengthUnlimited() && (minLength < minimum
                        || minimum == LENGTH_UNLIMITED)) {
                    minimum = minLength;
                }
            }
            return minimum;
        }

        @Override
        public int getMaxLength() {
            int maximum = LENGTH_UNLIMITED;
            for (RegularExpression expression : mVariants) {
                if (expression.isMaxLengthUnlimited()) {
                    return LENGTH_UNLIMITED;
                } else if (expression.getMaxLength() > maximum) {
                    maximum = expression.getMaxLength();
                }
            }
            return maximum;
        }

    }

    private ArrayList<RegularExpression> mVariants = new ArrayList<RegularExpression>();

    public void addVariant(RegularExpression regularExpression) {
        mVariants.add(regularExpression);
    }

    @Override
    public BlockType getBlockType() {
        return BlockType.COMPOUND;
    }

    @Override
    public int format(Editable input, int startPosition, int endPosition) {
        RegularExpression regularExpression = findMatched(mVariants, input.toString(),
                startPosition, endPosition);
        if (regularExpression != null) {
            return regularExpression.format(input, startPosition, endPosition);
        }
        return 0;
    }

    @Override
    public int format(Editable input, int startPosition) {
        RegularExpression regularExpression = findMatched(mVariants, input.toString(),
                startPosition, input.length());
        if (regularExpression != null) {
            return regularExpression.format(input, startPosition);
        }
        return 0;
    }

    @Override
    public int formatFromTheEnd(Editable input, int startPosition, int endPosition) {
        Editable reversedInput = new SpannableStringBuilder(input);
        RegularExpression.reverseEditable(reversedInput);
        RegularExpression regularExpression = findMatched(reverse().mVariants,
                reversedInput.toString(), startPosition, endPosition);
        if (regularExpression != null) {
            return regularExpression.formatFromTheEnd(input, startPosition, endPosition);
        }
        return 0;
    }

    @Override
    public int formatFromTheEnd(Editable input, int startPosition) {
        Editable reversedInput = new SpannableStringBuilder(input);
        RegularExpression.reverseEditable(reversedInput);
        RegularExpression regularExpression = findMatched(reverse().mVariants,
                reversedInput.toString(), startPosition, reversedInput.length());
        if (regularExpression != null) {
            return regularExpression.formatFromTheEnd(input, startPosition);
        }
        return 0;
    }

    private RegularExpression findMatched(ArrayList<RegularExpression> variants, String input,
            int startPosition, int endPosition) {
        int maximumScoreNumber = -1;
        int maximumScoreIndex = -1;
        int maximumNumberOfSymbolsMatched = 0;
        for (int i = 0; i < variants.size(); i++) {
            MatchResult result = variants.get(i).matches(input, startPosition, endPosition);
            if (result == MatchResult.FULL && (result.getScore() > maximumScoreNumber || (
                    result.getScore() == maximumScoreNumber && result.getNumberOfSymbolsMatched()
                            > maximumNumberOfSymbolsMatched))) {
                maximumScoreIndex = i;
                maximumScoreNumber = result.getScore();
                maximumNumberOfSymbolsMatched = result.getNumberOfSymbolsMatched();
            }
        }
        for (int i = 0; i < variants.size(); i++) {
            MatchResult result = variants.get(i).matches(input, startPosition, endPosition);
            if (result == MatchResult.NO && (result.getScore() > maximumScoreNumber || (
                    result.getScore() == maximumScoreNumber && result.getNumberOfSymbolsMatched()
                            > maximumNumberOfSymbolsMatched))) {
                maximumScoreNumber = result.getScore();
                maximumScoreIndex = i;
                maximumNumberOfSymbolsMatched = result.getNumberOfSymbolsMatched();
            }
        }
        for (int i = 0; i < variants.size(); i++) {
            MatchResult result = variants.get(i).matches(input, startPosition, endPosition);
            if ((result == MatchResult.LONGER || result == MatchResult.SHORTER) && (
                    maximumScoreNumber < result.getScore() || (
                            result.getScore() == maximumScoreNumber
                                    && result.getNumberOfSymbolsMatched()
                                    > maximumNumberOfSymbolsMatched))) {
                maximumScoreIndex = i;
                maximumScoreNumber = result.getScore();
                maximumNumberOfSymbolsMatched = result.getNumberOfSymbolsMatched();
            }
        }
        if (maximumScoreIndex == -1) {
            return null;
        } else {
            return variants.get(maximumScoreIndex);
        }
    }

    @Override
    public MatchResult matches(String string, int startPosition, int endPosition) {
        int maximumFullOccuranciesNumber = -1;
        int maximumFullOccuranciesIndex = -1;
        for (int i = 0; i < mVariants.size(); i++) {
            MatchResult matchResult = mVariants.get(i).matches(string, startPosition, endPosition);
            if (matchResult == MatchResult.FULL) {
                return MatchResult.FULL;
            } else if ((matchResult == MatchResult.LONGER || matchResult == MatchResult.SHORTER || (
                    matchResult == MatchResult.NO && matchResult.getScore() > 0))
                    && maximumFullOccuranciesNumber < matchResult.getScore()) {
                maximumFullOccuranciesIndex = i;
                maximumFullOccuranciesNumber = matchResult.getScore();
            }
        }
        if (maximumFullOccuranciesIndex == -1) {
            return MatchResult.NO;
        } else {
            return MatchResult.SHORTER;
        }
    }

    @Override
    public String toReversedString() {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < mVariants.size(); i++) {
            result.append(mVariants.get(i).toReversedString());
            if (i < mVariants.size() - 1) {
                result.append("|");
            }
        }
        return result.toString();
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < mVariants.size(); i++) {
            result.append(mVariants.get(i).toString());
            if (i < mVariants.size() - 1) {
                result.append("|");
            }
        }
        return result.toString();
    }

    @Override
    public LogicalRegularExpressionItem reverse() {
        LogicalRegularExpressionItem result = new LogicalRegularExpressionItem();
        for (RegularExpression regularExpression : mVariants) {
            result.addVariant(regularExpression.reverse());
        }
        return result;
    }
}
