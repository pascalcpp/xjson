package com.xpcf.xjson;

import java.io.IOException;
import java.util.*;

/**
 * @author XPCF
 * @version 1.0
 * @date 3/21/2021 5:22 AM
 */
public class JSONUtils {
    public static void main(String[] args) throws IOException {

        String js = "{\n" +
                "  \"code\": 200,\n" +
                "  \"student\": {\n" +
                "    \"name\": \"xpcf\",\n" +
                "    \"age\": 22\n" +
                "  },\n" +
                "  \"goods\": [\n" +
                "    \"good1\",\n" +
                "    2,\n" +
                "    12\n" +
                "  ]\n" +
                "}";

        System.out.println(parseJSON(js));
    }

    public static Map<String, Object> parseJSON(String json) {
        size = json.length();
        JSONUtils.json = json.toCharArray();
        return parseObject();
    }



    /**
     * current json char array cursor
     */
    private static int index = 0;


    /**
     * current json char array length
     */
    private static int size = 0;



    /**
     *  store json data
     */
    private static char[] json;



    /**
     * pares object
     * @return
     */
    public static Map<String, Object> parseObject() {
        Map<String, Object> result = new HashMap<>();
        if (json[index] == '{') {
            ++index;
            skipWhitespace();
            boolean initial = true;

            while (index < size && json[index] != '}') {

                if (!initial) {

                    if (json[index] == ',') {
                        eatComma();
                        skipWhitespace();
                    } else {
                        skipWhitespace();
                        break;
                    }


                }

                String key = parseString();
                skipWhitespace();
                eatColon();
                Object value = parseValue();
                result.put(key, value);
                initial = false;
            }

            expectNotEndOfInput('}');
            ++index;
        }
        return result;
    }


    /**
     * pares value
     * @return
     */
    private static Object parseValue() {
        skipWhitespace();

        switch (json[index]) {
            case '{':
                return parseObject();
            case '"':
                return parseString();
            case '[':
                return parseArray();
            default:
                return parseNumber();
        }

    }

    private static Object parseArray() {
        if (json[index] == '[') {
            ++index;
            skipWhitespace();
            List<Object> list = new ArrayList<>();
            boolean initial = true;
            while (index < size && json[index] != ']') {
                if (!initial) {
                    if (json[index] == ',') {
                        eatComma();
                        skipWhitespace();
                    } else {
                        skipWhitespace();
                        break;
                    }
                }
                Object value = parseValue();
                list.add(value);
                initial = false;
            }
            expectNotEndOfInput(']');
            ++index;
            return list;
        }
        throw new RuntimeException("Parse Array Error");
    }

    private static Object parseNumber() {
        int start = index;
        if (json[index] == '-') {
            ++index;
            expectDigit();
        }

        if (json[index] == '0') {
            ++index;
        } else if (json[index] >= '1' && json[index] <= '9') {
            ++index;
            while (json[index] >= '0' && json[index] <= '9') {
                ++index;
            }
        }

        if (json[index] == '.') {
            ++index;
            expectDigit();
            while (json[index] >= '0' && json[index] <= '9') {
                ++index;
            }
        }

        if (json[index] == 'e' || json[index] == 'E') {
            ++index;
            if (json[index] == '-' || json[index] == '+') {
                ++index;
            }
            expectDigit();
            while (json[index] >= '0' && json[index] <= '9') {
                ++index;
            }
        }

        if (index > start) {
            return Double.parseDouble(new String(Arrays.copyOfRange(json, start, index)));
        }
        throw new RuntimeException("parseNumber Exception");
    }

    private static void expectDigit(char... ch) {
        if (!(json[index] >= '0' && json[index] <= '9')) {
            throw new RuntimeException("Expect Digit");
        }

    }


    private static String parseString() {
        StringBuilder sb = new StringBuilder();
        if (json[index] == '"') {
            ++index;
        }
        while (index < size && json[index] != '"') {
            if (json[index] == '\\') {

                sb.append("\\");
                ++index;
                char ch = json[index + 1];

                if (ch == '"' || ch == '\\' || ch == '/' || ch == 'b'
                        || ch == 'f' || ch == 'n' || ch == 'r' || ch == 't') {
                    sb.append(ch);
                    ++index;
                } else if (ch == 'u') {
                    if (isHexadecimal(json[index + 2]) && isHexadecimal(json[index + 3]) && isHexadecimal(json[index + 4]) && isHexadecimal(json[index + 5])) {
                        StringBuilder builder = new StringBuilder();
                        builder.append("\\u");
                        for (int i = index + 2; i <= index + 5; i++) {
                            builder.append(json[i]);
                        }
                        sb.append(unicode2String(builder.toString()));
                        index += 5;
                    } else {
                        index += 2;
                        expectEscapeUnicode();
                    }

                } else {
                    expectEscapeCharacter();
                }
            } else {
                sb.append(json[index++]);
            }
        }
        expectNotEndOfInput('"');
        ++index;
        return sb.toString();
    }

    private static void expectEscapeCharacter() {
        throw new RuntimeException("Expect Escape Character");
    }

    private static void expectEscapeUnicode() {
        throw new RuntimeException("Expect Escape Unicode");
    }

    private static boolean isHexadecimal(char ch) {
        return (ch >= '0' && ch <= '9') || (Character.toLowerCase(ch) >= 'a' && Character.toLowerCase(ch) <= 'f');
    }

    private static void expectNotEndOfInput(char c) {
        if (index >= size) {
            throw new RuntimeException("Unexpect End of Input");
        }
    }

    private static void skipWhitespace() {
        while (json[index] == ' ' || json[index] == '\r' || json[index] == '\n' || json[index] == '\t') {
            ++index;
        }
    }

    private static void eatColon() {
        expectCharacter(':');
        ++index;
    }

    private static void eatComma() {
        expectCharacter(',');
        ++index;
    }

    private static void expectCharacter(char ch) {
        if (json[index] != ch) {
            throw new RuntimeException("Unexpect token");
        }
    }

    private static String unicode2String(String unicode) {
        StringBuilder sb = new StringBuilder();

        String[] hex = unicode.split("\\\\u");

        for (int i = 1; i < hex.length; i++) {

            // ???????????????????????????
            int data = Integer.parseInt(hex[i], 16);

            // ?????????string
            sb.append((char) data);

        }
        return sb.toString();
    }


}
