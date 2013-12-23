package ru.mw.android.regexpparser;

import android.text.Editable;

/**
 * Created by nixan on 9/9/13.
 */
public class WordRegularExpressionItem extends RegularExpressionItem {

    public WordRegularExpressionItem(int minLength, int maxLength) {
        super(minLength, maxLength);
    }

    @Override
    public BlockType getBlockType() {
        return BlockType.WORD;
    }

    @Override
    public String toString() {
        return "\\w" + (isMaxLengthUnlimited() ? (getMinLength() == 0 ? "*" : "+")
                : (getMaxLength() == getMinLength() ? ("{" + String.valueOf(getMaxLength()) + "}")
                        : ("{" + String.valueOf(getMinLength()) + "," + String
                                .valueOf(getMaxLength()) + "}")));
    }

    @Override
    public int format(Editable input, int startPosition) {
        return format(input, startPosition, input.length());
    }

    @Override
    public int format(Editable input, int startPosition, int endPosition) {
        int position = 0;
        while ((isMaxLengthUnlimited() || position < getMaxLength())
                && startPosition + position < endPosition) {
            position++;
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
        boolean inputIsLonger = false;
        boolean inputIsShorter = false;
        if (string.length() < startPosition) {
            return MatchResult.NO.setNumberOfSymbolsMatched(numberOfSymbolsMatched);
        }
        if (string.length() < endPosition) {
            inputIsShorter = true;
            numberOfSymbolsMatched = string.length() - startPosition;
        }
        if (endPosition - startPosition < getMinLength()) {
            inputIsShorter = true;
            numberOfSymbolsMatched = endPosition - startPosition;
        }
        if (!isMaxLengthUnlimited() && endPosition - startPosition > getMaxLength()) {
            inputIsLonger = true;
            numberOfSymbolsMatched = endPosition - startPosition;
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
