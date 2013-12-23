package ru.mw.android.regexpparser;

import android.text.Editable;
import android.text.InputType;

import java.util.ArrayList;

/**
 * Created by nixan on 9/9/13.
 */
public class RegularExpression extends RegularExpressionItem {

    private class SetOfRegularExpressionLength extends RegularExpressionLength {

        public SetOfRegularExpressionLength() {
            setMinLength(0);
            setMaxLengthUnlimited();
        }

        @Override
        public int getMinLength() {
            if (mRegularExpressionItems.isEmpty()) {
                return 0;
            } else {
                int result = 0;
                for (RegularExpressionItem item : mRegularExpressionItems) {
                    int minLength = item.getMinLength();
                    if (minLength == LENGTH_UNLIMITED) {
                        return LENGTH_UNLIMITED;
                    } else {
                        result += minLength;
                    }
                }
                return result;
            }
        }

        @Override
        public int getMaxLength() {
            if (mRegularExpressionItems.isEmpty()) {
                return LENGTH_UNLIMITED;
            } else {
                int result = 0;
                for (RegularExpressionItem item : mRegularExpressionItems) {
                    int maxLength = item.getMaxLength();
                    if (maxLength == LENGTH_UNLIMITED) {
                        return LENGTH_UNLIMITED;
                    } else {
                        result += maxLength;
                    }
                }
                return result;
            }
        }
    }

    private final ArrayList<RegularExpressionItem> mRegularExpressionItems
            = new ArrayList<RegularExpressionItem>();

    public RegularExpression() {
        super();
    }

    @Override
    protected RegularExpressionLength createNewRegularExpressionLength() {
        return new SetOfRegularExpressionLength();
    }

    public void addItem(RegularExpressionItem item) {
        mRegularExpressionItems.add(item);
    }

    public void addItem(int position, RegularExpressionItem item) {
        mRegularExpressionItems.add(position, item);
    }

    public void addAll(ArrayList<RegularExpressionItem> items) {
        mRegularExpressionItems.addAll(items);
    }

    public void addAll(RegularExpression regularExpression) {
        mRegularExpressionItems.addAll(regularExpression.mRegularExpressionItems);
    }

    public RegularExpressionItem getItem(int position) {
        return mRegularExpressionItems.get(position);
    }

    public int getSize() {
        return mRegularExpressionItems.size();
    }


    @Override
    public BlockType getBlockType() {
        return BlockType.SET;
    }

    @Override
    public int format(Editable input, int startPosition, int endPosition) {
        int start = startPosition;
        int itemPosition = 0;
        while (start < endPosition && itemPosition < getSize()) {
            int end = -1;
            if (getItem(itemPosition).isMaxLengthUnlimited() && itemPosition + 1 < getSize()
                    && getItem(itemPosition + 1).getBlockType()
                    == RegularExpressionItem.BlockType.STATIC) {
                end = input.toString().indexOf(
                        ((StaticRegularExpressionItem) getItem(itemPosition + 1)).getStaticData());
                if (end == -1) {
                    String lookupString = ((StaticRegularExpressionItem) getItem(itemPosition + 1))
                            .getStaticData();
                    while (lookupString.length() > 0 && end == -1) {
                        if (input.toString().endsWith(lookupString)) {
                            end = input.length() - lookupString.length();
                        } else {
                            lookupString = lookupString.substring(0, lookupString.length() - 1);
                        }
                    }
                }
            }
            int formattingSize = 0;
            if (end == -1) {
                formattingSize = getItem(itemPosition).format(input, start);
            } else {
                formattingSize = getItem(itemPosition).format(input, start, end);
            }

            start += formattingSize;
            itemPosition++;
            endPosition = input.length();
        }
        if (getSize() - itemPosition == 1
                && getItem(itemPosition).getBlockType() == RegularExpressionItem.BlockType.STATIC) {
            input.append(((StaticRegularExpressionItem) getItem(itemPosition)).getStaticData());
            itemPosition++;
        }
        if (!isMaxLengthUnlimited() && getMaxLength() < input.length()) {
            input.delete(getMaxLength(), input.length());
        }
        if (itemPosition == getSize() && start < input.length()) {
            input.delete(start, input.length());
        }
        return start - startPosition;
    }

    @Override
    public int format(Editable input, int startPosition) {
        return format(input, startPosition, input.length());
    }

    public RegularExpression reverse() {
        RegularExpression result = new RegularExpression();
        for (RegularExpressionItem item : mRegularExpressionItems) {
            result.addItem(0, item.reverse());
        }
        return result;
    }

    @Override
    public int formatFromTheEnd(Editable input, int startPosition, int endPosition) {
        reverseEditable(input);
        ArrayList<RegularExpressionItem> items = new ArrayList<RegularExpressionItem>();
        for (RegularExpressionItem item : mRegularExpressionItems) {
            items.add(0, item);
        }
        int start = 0;
        int itemPosition = 0;
        while (start < input.length() && itemPosition < items.size()) {
            int end = -1;
            if (items.get(itemPosition).isMaxLengthUnlimited() && itemPosition + 1 < items.size()
                    && items.get(itemPosition + 1).getBlockType()
                    == RegularExpressionItem.BlockType.STATIC) {
                end = input.toString().indexOf(items.get(itemPosition + 1).toString());
                if (end == -1) {
                    String lookupString = items.get(itemPosition + 1).toString();
                    while (lookupString.length() > 0 && endPosition == -1) {
                        if (input.toString().endsWith(lookupString)) {
                            end = input.length() - lookupString.length();
                        } else {
                            lookupString = lookupString.substring(0, lookupString.length() - 1);
                        }
                    }
                }
            }
            if (end == -1) {
                start += items.get(itemPosition).formatFromTheEnd(input, start);
            } else {
                start += items.get(itemPosition).formatFromTheEnd(input, start, endPosition);
            }
            itemPosition++;
        }
        if (items.size() - itemPosition == 1 && items.get(itemPosition).getBlockType()
                == RegularExpressionItem.BlockType.STATIC) {
            input.append(items.get(itemPosition).toString());
            start += items.get(itemPosition).toString().length();
            itemPosition++;
        }
        if (!isMaxLengthUnlimited() && getMaxLength() < input.length()) {
            input.delete(getMaxLength(), input.length());
        }
        if (itemPosition == items.size() && start < input.length()) {
            input.delete(start, input.length());
        }
        reverseEditable(input);
        return start - startPosition;
    }

    @Override
    public int formatFromTheEnd(Editable input, int startPosition) {
        return formatFromTheEnd(input, startPosition, input.length());
    }

    @Override
    public MatchResult matches(String string, int startPosition, int endPosition) {
        int start = 0;
        int itemPosition = 0;
        int numberOfSymbolsMatched = 0;
        while (start < string.length() && itemPosition < getSize()) {
            int end = -1;
            if (itemPosition + 1 < getSize() && getItem(itemPosition + 1).getBlockType()
                    == RegularExpressionItem.BlockType.STATIC) {
                end = string.indexOf(
                        ((StaticRegularExpressionItem) getItem(itemPosition + 1)).getStaticData(),
                        start);
            } else if (itemPosition + 1 < getSize()
                    && getItem(itemPosition).getMaxLength() == getItem(itemPosition).getMinLength()
                    && !getItem(itemPosition).isMaxLengthUnlimited()) {
                end = start + getItem(itemPosition).getMaxLength();
            } else if (itemPosition + 1 == getSize()) {
                end = string.length();
            }
            if (end == -1) {
                return MatchResult.NO.setScore(itemPosition)
                        .setNumberOfSymbolsMatched(numberOfSymbolsMatched);
            } else {
                MatchResult subResult = getItem(itemPosition).matches(string, start, end);
                numberOfSymbolsMatched += subResult.getNumberOfSymbolsMatched();
                if (subResult == MatchResult.FULL) {
                    start = end;
                } else {
                    return subResult.setScore(itemPosition)
                            .setNumberOfSymbolsMatched(numberOfSymbolsMatched);
                }
            }
            itemPosition++;
        }
        if (itemPosition == getSize()) {
            return MatchResult.FULL.setScore(itemPosition)
                    .setNumberOfSymbolsMatched(numberOfSymbolsMatched);
        } else {
            return MatchResult.SHORTER.setScore(itemPosition)
                    .setNumberOfSymbolsMatched(numberOfSymbolsMatched);
        }
    }

    @Override
    public String toReversedString() {
        return null;
    }

    public int getInputType() {
        if (getSize() == 1 && getItem(0).getBlockType() == RegularExpressionItem.BlockType.WORD) {
            return InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS;
        } else {
            boolean onlyDigits = getSize() == 0 ? false : true;
            for (RegularExpressionItem regularExpressionItem : mRegularExpressionItems) {
                if (regularExpressionItem.getBlockType() != RegularExpressionItem.BlockType.DIGIT
                        && regularExpressionItem.getBlockType()
                        != RegularExpressionItem.BlockType.STATIC) {
                    onlyDigits = false;
                }
            }
            return onlyDigits ? InputType.TYPE_CLASS_NUMBER
                    : InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS;
        }
    }

    public int getNonStaticMaxLength() {
        int result = 0;
        for (RegularExpressionItem item : mRegularExpressionItems) {
            if (item.getBlockType() != BlockType.STATIC) {
                if (item.isMaxLengthUnlimited()) {
                    return RegularExpressionLength.LENGTH_UNLIMITED;
                } else {
                    result += item.getMaxLength();
                }
            }
        }
        return result;
    }

    @Override
    public String toString() {
        if (mRegularExpressionItems.isEmpty()) {
            return "\\w+";
        } else {
            StringBuilder result = new StringBuilder();
            for (RegularExpressionItem item : mRegularExpressionItems) {
                result.append(item.toString());
            }
            return result.toString();
        }
    }

    public static void reverseEditable(Editable input) {
        for (int i = 0; i < input.length(); i++) {
            input.replace(i, i, input.subSequence(input.length() - 1, input.length()));
            input.delete(input.length() - 1, input.length());
        }
    }

}
