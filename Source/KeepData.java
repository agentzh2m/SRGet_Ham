import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public class KeepData implements Serializable {
    public StringBuilder currentData = new StringBuilder();
    public String ETag;
    public LocalDateTime DateMod;
    public int CurrentByteTransfered = 0;
    public int ContentLength;

    public int getByte(){
        return CurrentByteTransfered;
    }

    public  StringBuilder getData (){
        return currentData;
    }

    public int getContentLength (){return ContentLength;}

    public LocalDateTime getDate(){return DateMod;}

    public String getTag(){return ETag;}

    public void storeByte(int X){
        CurrentByteTransfered = X;
    }

    public void storeData(StringBuilder X){
        currentData = X;
    }

    public void storeContentLength(String X){
        ContentLength = Integer.parseInt(X);
    }

    public void storeDate(String s){
        DateMod = LocalDateTime.parse(s,DateTimeFormatter.RFC_1123_DATE_TIME);
    }

    public void storeTag(String s){
        ETag = s;
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
