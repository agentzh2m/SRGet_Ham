import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

public class TestSer {
    public static void testSerIn(String FileName){
        try {
            KeepData rs = new KeepData();
            FileInputStream SerFileIn = new FileInputStream(FileName);
            ObjectInputStream SerIn = new ObjectInputStream(SerFileIn);
            rs = (KeepData) SerIn.readObject();
            SerIn.close();
            SerFileIn.close();
            System.out.println(rs.getByte());
            System.out.println(rs.getData().toString());
        }catch (IOException e){
            e.printStackTrace();
        }catch (ClassNotFoundException ex){
            ex.printStackTrace();
        }
    }

    public static void test1(){
        File fs = new File("oracle.ser");
        System.out.println(fs.exists());
    }
}
