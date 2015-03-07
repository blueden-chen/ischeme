import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;

import java.util.Arrays;

/**
 * Created by chenwj on 2/11/15.
 */
public class SFunction extends SObject {
    public SExpression body;
    public String[] parameters;
    public SScope scope;

    public SFunction(SExpression body, String[] parameters, SScope scope) {
        this.body = body;
        this.parameters = parameters;
        this.scope = scope;
    }

    //判断是否只有部分参数
    public Boolean isPartial() {
        int len = this.computeFilledParameters().length;
        return len >= 1 && len <= this.parameters.length;
    }

    //
    public SObject evaluate(){
        String[] filledParameters = this.computeFilledParameters();
        if (filledParameters.length < parameters.length) {
            return this;
        } else {
            return this.body.evaluate(this.scope);
        }
    }

    //找到当前作用域定义了的参数数组
    public String[] computeFilledParameters() {
        return Arrays.asList(this.parameters).stream().filter(p -> this.scope.findInTop(p) != null).toArray(String[]::new);
    }
    public SFunction update(SObject[] arguments){
        SObject[] existingArguments = Arrays.asList(this.parameters).stream().map(p -> this.scope.findInTop(p)).filter(obj -> obj != null).toArray(SObject[]::new);
        SObject[] newArguments = (SObject[]) ArrayUtils.addAll(existingArguments, arguments);
        SScope newScope = this.scope.parent.spawnScopeWith(this.parameters, newArguments);
        return new SFunction(this.body, this.parameters, newScope);
    }

    public String toString() {
        return String.format("func(%s)(%s)", StringUtils.join(Arrays.asList(this.parameters).stream().map(p -> {
            SObject value = null;
            if ((value = this.scope.findInTop(p)) != null) {
                return p + ":" + value;
            }
            return p;
        }).toArray(String[]::new), " "), this.body);
    }
}
