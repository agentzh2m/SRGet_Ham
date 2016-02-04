import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Hamuel on 2/3/16.
 */
public class concurMeta implements Serializable {
    List<Long> StartPosThread;
    List<Long> EndPosThread;

    public concurMeta(){
        StartPosThread = new ArrayList<>();
        EndPosThread = new ArrayList<>();
    }

}
