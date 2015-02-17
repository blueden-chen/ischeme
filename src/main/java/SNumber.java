/**
 * Created by chenwj on 2/11/15.
 */
public class SNumber extends SObject {
    public Long value;

    public SNumber(long value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return this.value.toString();
    }
}
