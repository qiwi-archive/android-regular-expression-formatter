package ru.mw.android.regexpparser;

import android.text.Editable;

/**
 * Created by nixan on 9/9/13.
 */
public abstract class RegularExpressionItem {

    public static class RegularExpressionLength {

        public RegularExpressionLength() {

        }

        public static final int LENGTH_UNLIMITED = -1;

        private int mMinLength = LENGTH_UNLIMITED;

        private int mMaxLength = LENGTH_UNLIMITED;

        public int getMinLength() {
            return mMinLength;
        }

        public int getMaxLength() {
            return mMaxLength;
        }

        public boolean isFixedLength() {
            return getMinLength() == getMaxLength() && !isMaxLengthUnlimited();
        }

        public void setMinLength(int minLength) {
            mMinLength = minLength;
        }

        public void setMaxLength(int maxLength) {
            mMaxLength = maxLength;
        }

        public void setMaxLengthUnlimited() {
            mMaxLength = LENGTH_UNLIMITED;
        }

        public void setMinLengthUnlimited() {
            mMinLength = LENGTH_UNLIMITED;
        }

        public boolean isMinLengthUnlimited() {
            return mMinLength == LENGTH_UNLIMITED;
        }

        public boolean isMaxLengthUnlimited() {
            return mMaxLength == LENGTH_UNLIMITED;
        }
    }

    public enum BlockType {
        WORD, DIGIT, STATIC, INTERVAL, COMPOUND, SET
    }

    public enum MatchResult {
        NO, FULL, SHORTER, LONGER;

        private int mScore = 0;

        private int mNumberOfSymbolsMatched = 0;

        public MatchResult setNumberOfSymbolsMatched(int numberOfSymbolsMatched) {
            mNumberOfSymbolsMatched = numberOfSymbolsMatched;
            return this;
        }

        public int getNumberOfSymbolsMatched() {
            return mNumberOfSymbolsMatched;
        }

        public MatchResult setScore(int score) {
            mScore = score;
            return this;
        }

        public int getScore() {
            return mScore;
        }

    }

    private final RegularExpressionLength mRegularExpressionLength
            = createNewRegularExpressionLength();

    public RegularExpressionItem() {

    }

    public RegularExpressionItem(int minLength, int maxLength) {
        mRegularExpressionLength.setMinLength(minLength);
        mRegularExpressionLength.setMaxLength(maxLength);
    }

    protected RegularExpressionLength createNewRegularExpressionLength() {
        return new RegularExpressionLength();
    }

    public int getMinLength() {
        return mRegularExpressionLength.getMinLength();
    }

    public int getMaxLength() {
        return mRegularExpressionLength.getMaxLength();
    }

    public boolean isMaxLengthUnlimited() {
        return mRegularExpressionLength.isMaxLengthUnlimited();
    }

    public boolean isMinLengthUnlimited() {
        return mRegularExpressionLength.isMinLengthUnlimited();
    }

    public abstract BlockType getBlockType();

    public abstract int format(Editable input, int startPosition, int endPosition);

    public abstract int format(Editable input, int startPosition);

    public abstract int formatFromTheEnd(Editable input, int startPosition, int endPosition);

    public abstract int formatFromTheEnd(Editable input, int startPosition);

    public abstract MatchResult matches(String string, int startPosition, int endPosition);

    public abstract String toReversedString();

    public abstract RegularExpressionItem reverse();

}
