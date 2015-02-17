import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
/**
 * Created by chenwj on 2/11/15.
 */

@FunctionalInterface
interface BiFunctionWithThrow<T, U, R> {
    R apply(T t, U u) throws Exception;
}

public class SScope {
    public SScope parent;
    private Map<String, SObject> variableTable;

    //内置方法表
    public static Map<String, BiFunctionWithThrow<SObject[], SScope, SObject>> builtinFunctions = new HashMap<>();

    public SScope(SScope parent) {
        this.parent = parent;
        this.variableTable = new HashMap<>();
    }

    //生成AST
    public static SExpression parseAsIscheme(String code) throws Exception {
        SExpression program = new SExpression("", null);
        SExpression current = program;
        for (String lex : Lexer.tokenize(code)) {
            switch (lex) {
                case "(":
                    SExpression newNode = new SExpression("(", current);
                    current.children.add(newNode);
                    current = newNode;
                    break;
                case ")":
                    current = current.parent;
                    break;
                default:
                    current.children.add(new SExpression(lex, current));
            }
        }
        return program.children.get(0);
    }

    //在作用域链表中查找对应符号
    public SObject find(String name) throws Exception {
        SScope current = this;
        while (current != null) {
            if (current.variableTable.containsKey(name)) {
                return current.variableTable.get(name);
            }
            current = current.parent;
        }
        throw new Exception(name + " is not defined.");
    }

    //只查找当前作用域
    public SObject findInTop(String name) {
        if (variableTable.containsKey(name)) return variableTable.get(name);
        return null;
    }

    //构建新作用域
    public SScope spawnScopeWith(String[] names, SObject[] values) throws Exception {
        if (names.length < values.length) {
            throw new Exception("Too many arguments");
        }
        SScope scope = new SScope(this);
        for (int i = 0; i < values.length; i++) {
            scope.variableTable.put(names[i], values[i]);
        }
        return scope;
    }

    //添加变量到符号表中
    public SObject define(String name, SObject value) {
        this.variableTable.put(name, value);
        return value;
    }

    //添加内置方法
    public SScope buildIn(String name, BiFunctionWithThrow<SObject[], SScope, SObject> builtinFunction) {
        SScope.builtinFunctions.put(name, builtinFunction);
        return this;
    }

    //console模式
    public void keepInterpretingInConsole(BiFunctionWithThrow<String, SScope, SObject> evaluate) {
        while (true) {
            try {
                System.out.print(">> ");
                System.out.flush();
                String code;
//                Scanner scanner = new Scanner(System.in);
                BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
                code = in.readLine();
                if (!(code).isEmpty()) {
                    System.out.println(">> " + evaluate.apply(code, this));
                }
            } catch (Exception e) {
                System.out.println(">> " + e.getMessage());
            }
        }
    }

    public static SBool chainRelation(SObject[] arguments, BiFunctionWithThrow<SNumber, SNumber, Boolean> relation) throws Exception {
        if (arguments.length <= 1) throw new Exception("chainRelation exception...");
        SNumber current = (SNumber) arguments[0];

        for (int i = 1; i < arguments.length; i++) {
            SNumber next = (SNumber) arguments[i];
            if (relation.apply(current, next)) {
                current = next;
            } else return SBool.False;
        }
        return SBool.True;
    }

    public static SList retrieveSList(SObject[] arguments, SScope scope, String operationName) throws Exception {
        SList list = null;
        if (arguments.length != 1 || (list = ((SList) arguments[0])) == null)
            throw new Exception("#{operationName} must apply to a list.");
        return list;
    }

    public static void main(String[] args) {

        new SScope(null).buildIn("+", (arguments, scope) -> {
            Long sum = 0L;
            for (int i = 0; i < arguments.length; i++) {
                sum += ((SNumber) arguments[i]).value;
            }
            return new SNumber(sum);
        }).buildIn("-", (arguments, scope) -> {
            Long firstValue = ((SNumber) arguments[0]).value;
            if (arguments.length == 1) return new SNumber(-firstValue);
            Long sum = 0L;
            for (int i = 1; i < arguments.length; i++) {
                sum += ((SNumber) arguments[i]).value;
            }
            return new SNumber(firstValue - sum);
        }).buildIn("*", (arguments, scope) -> {
            Long product = 1L;
            for (int i = 0; i < arguments.length; i++) {
                product *= ((SNumber) arguments[i]).value;
            }
            return new SNumber(product);
        }).buildIn("/", (arguments, scope) -> {
            Long firstValue = ((SNumber) arguments[0]).value;
            Long product = 1L;
            for (int i = 0; i < arguments.length; i++) {
                product *= ((SNumber) arguments[i]).value;
            }
            return new SNumber(firstValue / product);
        }).buildIn("%", (arguments, scope) -> {
            if (arguments.length != 2) throw new Exception("parameters count in mod should be 2");
            return new SNumber(((SNumber) arguments[0]).value % ((SNumber) arguments[1]).value);
        }).buildIn("and", (arguments, scope) -> {
            if (arguments.length <= 0) throw new Exception("");
            for (SObject b : arguments) {
                if (!((SBool) b).value) return SBool.False;
            }
            return SBool.True;
        }).buildIn("or", (arguments, scope) -> {
            if (arguments.length <= 0) throw new Exception("");
            for (SObject b : arguments) {
                if (((SBool) b).value) return SBool.True;
            }
            return SBool.False;
        }).buildIn("not", (arguments, scope) -> {
            if (arguments.length != 1) throw new Exception("");
            return new SBool(!((SBool) arguments[0]).value);
        }).buildIn("=", (arguments, scope) -> {
            return chainRelation(arguments, (s1, s2) -> s1.value == s2.value);
        }).buildIn(">", (arguments, scope) -> {
            return chainRelation(arguments, (s1, s2) -> s1.value > s2.value);
        }).buildIn("<", (arguments, scope) -> {
            return chainRelation(arguments, (s1, s2) -> s1.value < s2.value);
        }).buildIn(">=", (arguments, scope) -> {
            return chainRelation(arguments, (s1, s2) -> s1.value >= s2.value);
        }).buildIn("<=", (arguments, scope) -> {
            return chainRelation(arguments, (s1, s2) -> s1.value <= s2.value);
        }).buildIn("first", (arguments, scope) -> {
            SList list = retrieveSList(arguments, scope, "first");
            return list.values.get(0);
        }).buildIn("rest", (arguments, scope) -> {
            SList list = retrieveSList(arguments, scope, "rest");
            List sublist = list.values.subList(1, list.values.size());
            return new SList(sublist);
        }).buildIn("append", (arguments, scope) -> {
            SList list0 = null, list1 = null;
            if (arguments.length != 2 || (list0 = (SList) arguments[0]) == null || (list1 = (SList) arguments[1]) == null)
                throw new Exception("Input must be two lists");
            list0.values.addAll(list1.values);
            return list0;
        }).buildIn("empty?", (arguments, scope) -> {
            SList list = retrieveSList(arguments, scope, "empty?");
            if (list.values.size() == 0) return SBool.True;
            return SBool.False;
        }).keepInterpretingInConsole((code, scope) -> parseAsIscheme(code).evaluate(scope));
    }
}
