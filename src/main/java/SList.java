import org.apache.commons.lang.StringUtils;

import java.util.List;

/**
 * Created by chenwj on 2/11/15.
 */
public class SList extends SObject{
    public List<SObject> values;
    public SList(List<SObject> values) {
        this.values = values;
    }

    public @Override String toString(){
        return "(list " + StringUtils.join(this.values, " ") + ")";
    }

}
