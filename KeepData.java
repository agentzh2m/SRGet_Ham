import java.io.Serializable;

public class KeepData implements Serializable {
    public static StringBuilder currentData = new StringBuilder();
    public static int CurrentByteTransfered = 0;

    public static int getByte(){
        return CurrentByteTransfered;
    }

    public static StringBuilder getData (){
        return currentData;
    }

    public static void storeByte(int X){
        CurrentByteTransfered = X;
    }

    public static void storeData(StringBuilder X){
        currentData = X;
    }



}
