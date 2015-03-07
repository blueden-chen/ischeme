import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by chenwj on 2/11/15.
 */
public class SExpression {
    public String value;
    public List<SExpression> children;
    public SExpression parent;

    public SExpression(String value, SExpression parent) {
        this.value = value;
        this.children = new ArrayList<>();
        this.parent = parent;
    }

    @Override
    public String toString() {
        if (this.value == "(")
            return "(" + StringUtils.join(this.children, " ") + ")";
        else
            return this.value;
    }

    public SList sLists(SExpression current, SScope scope) {
        List list = current.children.stream().skip(1).map(exp -> {
            return exp.evaluate(scope);
        }).collect(Collectors.toList());
        return new SList(list);
    }

    //java lambda need param be final
    public SObject[] sObjects(SExpression current, SScope scope) {
        return current.children.stream().skip(1).map(s -> {
            return s.evaluate(scope);

        }).toArray(SObject[]::new);
    }

    public SObject evaluate(SScope scope) {
        SExpression current = this;
        while (true) {

            //如果是数字，就直接返回SObject对象
            if (current.children.size() == 0) {
                Long number;
                try {
                    return new SNumber(Long.parseLong(this.value));
                } catch (NumberFormatException e) {
                    return scope.find(current.value);
                }
            } else {
                SExpression first = current.children.get(0);
                switch (first.value) {
                    case "if": {
                        SBool condition = (SBool) (current.children.get(1).evaluate(scope));
                        return condition.value ? current.children.get(2).evaluate(scope) : current.children.get(3).evaluate(scope);
                    }
                    case "def": {
                        return scope.define(current.children.get(1).value, this.children.get(2).evaluate(new SScope(scope)));
                    }
                    case "begin": {
                        SObject result = null;
                        SExpression[] arguments = current.children.stream().skip(1).toArray(SExpression[]::new);
                        for (SExpression expression : arguments) {
                            result = expression.evaluate(scope);
                        }
                        return result;
                    }
                    case "func": {
                        SExpression body = current.children.get(2);
                        String[] parameters = current.children.get(1).children.stream().map(exp -> exp.value).toArray(String[]::new);
                        //当前的scope变为newScope的父scope
                        SScope newScope = new SScope(scope);
                        return new SFunction(body, parameters, newScope);
                    }
                    case "list": {
                        return sLists(current, scope);
                    }
                    default: {
                        if (SScope.builtinFunctions.containsKey(first.value)) {
                            SObject[] arguments = sObjects(current, scope);
                            return SScope.builtinFunctions.get(first.value).apply(arguments, scope);
                        } else {
                            SFunction function = first.value.equals("(") ? (SFunction) first.evaluate(scope) : (SFunction) scope.find(first.value);
                            SObject[] arguments = sObjects(current, scope);

                            SFunction newFuction = function.update(arguments);
                            if (newFuction.isPartial()) {
                                return newFuction.evaluate();
                            } else {
                                current = newFuction.body;
                                scope = newFuction.scope;
                            }
                        }
                    }
                }
            }

        }
    }
}
