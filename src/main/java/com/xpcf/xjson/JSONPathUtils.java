package com.xpcf.xjson;

import jdk.nashorn.internal.runtime.regexp.joni.ast.StateNode;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author XPCF
 * @version 1.0
 * @date 3/21/2021 9:13 PM
 */
public class JSONPathUtils {

    private static Map<String, Object> jsonMap;

    private static PathNode endNode = new PathNode();

    private static PathNode startNode = new PathNode();

    public static void main(String[] args) {
        String path = "a[0].b";
        String js = "{\n" +
                "  \"a\": [\n" +
                "    {\"b\": \"c\"}\n" +
                "  ]\n" +
                "}";

        Object read = read(js, path);
        System.out.println(read);
    }

    static {
//        init endNode
        endNode.type = NodeEnum.EndNode;
        endNode.pre = startNode;
        endNode.next = null;

//        init startNode
        startNode.type = NodeEnum.StartNode;
        startNode.pre = null;
        startNode.next = endNode;
    }

    private static int index;

    private static int size;

    private static char[] path;

    static class PathNode {
        String index;
        NodeEnum type;
        PathNode pre;
        PathNode next;

        PathNode() {

        }

        PathNode(NodeEnum type, String index, PathNode next, PathNode pre) {
            this.type = type;
            this.index = index;
            this.next = next;
            this.pre = pre;
        }
    }

    static enum NodeEnum {

        CommonNode(1),
        ArrayNode(2),
        EndNode(4),
        StartNode(8);
        int code;

        NodeEnum(int code) {
            this.code = code;
        }

    }


    public static Object read(String json, String jsonPath) {
        jsonMap = JSONUtils.parseJSON(json);
        path = jsonPath.toCharArray();
        size = jsonPath.length();
        resolveJSONPath();

        return getValueByPath();
    }

    private static Object getValueByPath() {
        Object operateObj = jsonMap;
        PathNode curNode = startNode;
        while (null != curNode.next && curNode.next != endNode) {
            curNode = curNode.next;
            if (curNode.type.equals(NodeEnum.CommonNode)) {
                operateObj = ((HashMap<String, Object>)operateObj).get(curNode.index);
            } else if (curNode.type.equals(NodeEnum.ArrayNode)) {
                operateObj = ((List<Object>)operateObj).get(Integer.parseInt(curNode.index));
            }
        }

        return operateObj;
    }

    public static void resolveJSONPath() {

        while (index < size) {
            switch (path[index]) {
                case '[':
                    insertNode(resolveArray());
                    break;
                case '.':
                    insertNode(resolveCommon());
                    break;
                default:
                    insertNode(resolveCommon());
            }
        }
    }

    private static PathNode resolveCommon() {
        PathNode pathNode = new PathNode();
        if (path[index] == '.') {
            ++index;
        }

        StringBuilder sb = new StringBuilder();
        while (path[index] != '[' && path[index] != '.' && index < size) {
            sb.append(path[index]);

            if (index + 1 < size) {
                ++index;
            } else {
                ++index;
                break;
            }
        }
        pathNode.type = NodeEnum.CommonNode;
        pathNode.index = sb.toString();
        return pathNode;
    }

    private static PathNode resolveArray() {
        PathNode pathNode = new PathNode();
        StringBuilder sb = new StringBuilder();
        ++index;
        while (path[index] != ']') {
            sb.append(path[index]);
            ++index;
        }
        ++index;
        pathNode.type = NodeEnum.ArrayNode;
        pathNode.index = sb.toString();
        return pathNode;
    }

    private static void insertNode(PathNode node) {

        node.pre = endNode.pre;
        endNode.pre.next = node;

        endNode.pre = node;
        node.next = endNode;

    }
}
