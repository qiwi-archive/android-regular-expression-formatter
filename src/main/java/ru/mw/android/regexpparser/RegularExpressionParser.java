package ru.mw.android.regexpparser;

import android.text.TextUtils;

/**
 * Created by nixan on 20.12.13.
 */
public class RegularExpressionParser {


    public static final RegularExpressionItem parseRegularExpression(String regularExpression) {
        return parseRegularExpression(regularExpression,
                RegularExpressionItem.RegularExpressionLength.LENGTH_UNLIMITED);
    }

    public static final RegularExpressionItem parseRegularExpression(String regularExpression,
            int maximumLength) {
        if (TextUtils.isEmpty(regularExpression)) {
            return new WordRegularExpressionItem(1, maximumLength);
        }

        RegularExpression result = new RegularExpression();

        String[] variantsOfExpression = regularExpression.split("\\|");
        if (variantsOfExpression.length == 1) {
            result.addAll(internalParseOneWayExpression(variantsOfExpression[0], maximumLength));
        } else {
            LogicalRegularExpressionItem logicalRegExpItem = new LogicalRegularExpressionItem();
            for (String variant : variantsOfExpression) {
                logicalRegExpItem.addVariant(internalParseOneWayExpression(variant, maximumLength));
            }
            result.addItem(logicalRegExpItem);
        }
        return result;
    }

    private static final RegularExpression internalParseOneWayExpression(
            String regularExpressionString, int maximumLength) {
        int position = 0;
        if (regularExpressionString.startsWith("^")) {
            regularExpressionString = regularExpressionString.substring(1);
        }
        if (regularExpressionString.endsWith("$")) {
            regularExpressionString = regularExpressionString
                    .substring(0, regularExpressionString.length() - 1);
        }
        RegularExpression result = new RegularExpression();
        while (position < regularExpressionString.length()) {
            char c = regularExpressionString.charAt(position);
            RegularExpressionItem item = null;
            switch (c) {
                case '\\':
                    position++;
                    char typeCharacter = regularExpressionString.charAt(position);
                    int minTypeLength = 1;
                    int maxTypeLength = 1;
                    position++;
                    if (position < regularExpressionString.length()) {
                        c = regularExpressionString.charAt(position);
                        if (c == '*') {
                            minTypeLength = 0;
                            maxTypeLength
                                    = RegularExpressionItem.RegularExpressionLength.LENGTH_UNLIMITED;
                        } else if (c == '+') {
                            minTypeLength = 1;
                            maxTypeLength
                                    = RegularExpressionItem.RegularExpressionLength.LENGTH_UNLIMITED;
                        } else if (c == '?') {
                            minTypeLength = 0;
                            maxTypeLength = 1;
                        } else if (c == '{') {
                            StringBuilder lengthString = new StringBuilder();
                            position++;
                            while (position < regularExpressionString.length()
                                    && regularExpressionString.charAt(position) != '}') {
                                lengthString.append(regularExpressionString.charAt(position));
                                position++;
                            }
                            String[] lengths = lengthString.toString().split(",");
                            if (lengths.length == 1) {
                                minTypeLength = Integer.parseInt(lengths[0].trim());
                                maxTypeLength = minTypeLength;
                            } else if (lengths.length == 2 && TextUtils
                                    .isEmpty(lengths[1].trim())) {
                                minTypeLength = Integer.parseInt(lengths[0].trim());
                                maxTypeLength
                                        = RegularExpressionItem.RegularExpressionLength.LENGTH_UNLIMITED;
                            } else if (lengths.length == 2) {
                                minTypeLength = Integer.parseInt(lengths[0].trim());
                                maxTypeLength = Integer.parseInt(lengths[1].trim());
                            }
                        }
                        position++;
                    }
                    switch (typeCharacter) {
                        case 'd':
                            item = new DigitRegularExpressionItem(minTypeLength, maxTypeLength);
                            break;
                        case 'w':
                            item = new WordRegularExpressionItem(minTypeLength, maxTypeLength);
                            break;
                        default:
                            item = new StaticRegularExpressionItem(String.valueOf(typeCharacter));
                            break;
                    }
                    break;
                case '[':
                    position++;
                    StringBuilder variantConditions = new StringBuilder();
                    while (position < regularExpressionString.length()
                            && regularExpressionString.charAt(position) != ']') {
                        variantConditions.append(regularExpressionString.charAt(position));
                        position++;
                    }
                    int minVariantLength = 1;
                    int maxVariantLength = 1;
                    if (position < regularExpressionString.length()) {
                        c = regularExpressionString.charAt(++position);
                        if (c == '*') {
                            minVariantLength = 0;
                            maxVariantLength
                                    = RegularExpressionItem.RegularExpressionLength.LENGTH_UNLIMITED;
                            position++;
                        } else if (c == '+') {
                            minVariantLength = 1;
                            maxVariantLength
                                    = RegularExpressionItem.RegularExpressionLength.LENGTH_UNLIMITED;
                            position++;
                        } else if (c == '?') {
                            minVariantLength = 0;
                            maxVariantLength = 1;
                            position++;
                        } else if (c == '{') {
                            StringBuilder lengthString = new StringBuilder();
                            position++;
                            while (position < regularExpressionString.length()
                                    && regularExpressionString.charAt(position) != '}') {
                                lengthString.append(regularExpressionString.charAt(position));
                                position++;
                            }
                            String[] lengths = lengthString.toString().split(",");
                            if (lengths.length == 1) {
                                minVariantLength = Integer.parseInt(lengths[0].trim());
                                maxVariantLength = minVariantLength;
                            } else if (lengths.length == 2 && TextUtils
                                    .isEmpty(lengths[1].trim())) {
                                minVariantLength = Integer.parseInt(lengths[0].trim());
                                maxVariantLength
                                        = RegularExpressionItem.RegularExpressionLength.LENGTH_UNLIMITED;
                            } else if (lengths.length == 2) {
                                minVariantLength = Integer.parseInt(lengths[0].trim());
                                maxVariantLength = Integer.parseInt(lengths[1].trim());
                            }
                            if (regularExpressionString.charAt(position) == '}') {
                                position++;
                            }
                        }
                    }
                    item = new IntervalRegularExpressionItem(variantConditions.toString(),
                            minVariantLength, maxVariantLength);
                    break;
                default:
                    StringBuilder constantString = new StringBuilder();
                    constantString.append(c);
                    position++;
                    while (position < regularExpressionString.length()
                            && regularExpressionString.charAt(position) != '\\'
                            && regularExpressionString.charAt(position) != '[') {
                        constantString.append(regularExpressionString.charAt(position));
                        position++;
                    }
                    item = new StaticRegularExpressionItem(constantString.toString());
                    break;
            }
            if (item != null) {
                result.addItem(item);
            }
        }
        return result;
    }

}
