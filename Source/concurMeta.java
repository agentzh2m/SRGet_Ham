import java.io.Serializable;

/**
 * Created by Hamuel on 2/3/16.
 */
public class concurMeta implements Serializable {
    long[] threadStartAt;
    long[] workDoneOnEachPart;

    public concurMeta(int TotalThread){
        threadStartAt = new long[TotalThread];
        workDoneOnEachPart = new long[TotalThread];
    }

    public void reCalculate(int NewTotalThread){

    }
}
