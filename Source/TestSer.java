import org.omg.CORBA.portable.OutputStream;

import java.io.*;

public class TestSer {
    public static void testFile() {
        try {
            FileOutputStream test = new FileOutputStream("abcd.test");
            byte[] testBlank = new byte[1024];
            test.write(testBlank);
            test.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static void test1(){
        File fs = new File("oracle.ser");
        System.out.println(fs.exists());
    }
}
