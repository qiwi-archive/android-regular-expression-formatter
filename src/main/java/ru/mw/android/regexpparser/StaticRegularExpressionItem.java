package ru.mw.android.regexpparser;

import android.graphics.Color;
import android.text.Editable;
import android.text.Spannable;
import android.text.style.ForegroundColorSpan;

/**
 * Created by nixan on 9/9/13.
 */
public class StaticRegularExpressionItem extends RegularExpressionItem {

    private final String mStaticData;

    public StaticRegularExpressionItem(String data) {
        super(data.length(), data.length());
        mStaticData = data;
    }

    public String getStaticData() {
        return mStaticData;
    }

    @Override
    public BlockType getBlockType() {
        return BlockType.STATIC;
    }

    @Override
    public String toString() {
        return mStaticData;
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
            if (input.charAt(position + startPosition) != mStaticData.charAt(position)) {
                input.insert(startPosition + position,
                        String.valueOf(mStaticData.charAt(position)));
                endPosition++;
            }
            position++;
        }
        if (position != 0) {
            input.setSpan(new ForegroundColorSpan(Color.GRAY), startPosition,
                    startPosition + position, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        return position;
    }

    public int formatFromTheEnd(Editable input, int startPosition, int endPosition) {
        int position = 0;
        String reversedString = toReversedString();
        while (position + startPosition < endPosition && (isMaxLengthUnlimited()
                || position < getMaxLength())) {
            if (input.charAt(position + startPosition) != reversedString.charAt(position)) {
                input.insert(startPosition + position,
                        String.valueOf(reversedString.charAt(position)));
                endPosition++;
            }
            position++;
        }
        if (position != 0) {
            input.setSpan(new ForegroundColorSpan(Color.GRAY), startPosition,
                    startPosition + position, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        return position;
    }

    public int formatFromTheEnd(Editable input, int startPosition) {
        return formatFromTheEnd(input, startPosition, input.length());
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
        }
        if (endPosition - startPosition < getMinLength()) {
            inputIsShorter = true;
        }
        String lookup = string.substring(startPosition, Math.min(string.length(), endPosition));
        String target = mStaticData.substring(0, Math.min(mStaticData.length(), lookup.length()));
        if (!lookup.equals(target)) {
            for (int i = 0; i < lookup.length(); i++) {
                if (lookup.charAt(i) == target.charAt(i)) {
                    numberOfSymbolsMatched++;
                }
            }
            return MatchResult.NO.setNumberOfSymbolsMatched(numberOfSymbolsMatched);
        } else {
            numberOfSymbolsMatched = lookup.length();
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
        String straightforward = toString();
        StringBuilder reversed = new StringBuilder();
        for (char ch : straightforward.toCharArray()) {
            reversed.insert(0, ch);
        }
        return reversed.toString();
    }

    @Override
    public RegularExpressionItem reverse() {
        return new StaticRegularExpressionItem(toReversedString());
    }

}
