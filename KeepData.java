import java.io.Serializable;

public class KeepData implements Serializable {
    public StringBuilder currentData = new StringBuilder();
    public int CurrentByteTransfered = 0;

    public int getByte(){
        return CurrentByteTransfered;
    }

    public  StringBuilder getData (){
        return currentData;
    }

    public void storeByte(int X){
        CurrentByteTransfered = X;
    }

    public void storeData(StringBuilder X){
        currentData = X;
    }

    public void appendByte(int X){
        CurrentByteTransfered+=X;
    }

    public void appendData(StringBuilder X){
        currentData.append(X);
    }

    public void appendData(String X){
        currentData.append(X);
    }

}
