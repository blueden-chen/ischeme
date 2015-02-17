/**
 * Created by chenwj on 2/11/15.
 */
public class SBool extends SObject {
    public static SBool False = new SBool(false);
    public static SBool True = new SBool(true);
    public Boolean value;

    public SBool(Boolean value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return this.value.toString();
    }
}