import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;

/**
 * Created by Hamuel on 1/14/16.
 */
public class TD {
    public static void main(String[] args) {
        //mainDL test01 = new mainDL("http://docs.oracle.com/javase/8/docs/api/overview-summary.html", "java8doc.txt");
        //test01.newDL();
//        TD test12 = new TD();
//        test12.startDL("http://pngimg.com/upload/chicken_PNG2145.png", "chicken.png");

        //TD test22 = new TD();
        //test22.startDL("http://n467us.com/Data%20Files/Seattle%20Sectional%20South.jpg", "bigimg2.jpg");
        //mainDL test03 = new mainDL("https://www.httpwatch.com/httpgallery/chunked/chunkedimage.aspx", "testimg.aspx");
        //test03.newDL();
        //TestSer.testFile();
        //test Resume
//        chkDL test411 = new chkDL("http://n467us.com/Data%20Files/Seattle%20Sectional%20South.jpg");
//        concurDL test41 = new concurDL("http://n467us.com/Data%20Files/Seattle%20Sectional%20South.jpg","TestIMG_C.jpg", 5);

            TD test55 = new TD();
            test55.ReadSerial("bigFileC.HEADC");


//        chkDL test42 = new chkDL("http://cs.muic.mahidol.ac.th/~ktangwon/bigfile.xyz");
//        concurDL test421 = new concurDL("http://cs.muic.mahidol.ac.th/~ktangwon/bigfile.xyz","bigFileC" , 4);

//        TD test001 = new TD();
//        test001.testChannel();
//        TD test002 = new TD();
//        System.out.println(test002.testThreadPool());

    }

    public void startDL(String url, String filename){
        mainDL DL = new mainDL(url, filename);
        File headFile = new File(filename+".HEAD");
        File dataFile = new File(filename+".DATA");
        if (headFile.exists() && dataFile.exists()){
            DL.startResume(headFile, dataFile);
        }else {
            DL.newDL();
        }

    }

    public int testThreadPool(){
        int AmountOfPrime = 0;
        List<Boolean> resultList = Collections.synchronizedList(new ArrayList<>());
        ExecutorService pool = Executors.newFixedThreadPool(1000);
        for (int i  = 1; i < 100000000; i++){
            TD td = new TD();
            isPrimeThread primer = new isPrimeThread(i , resultList);
            pool.execute(primer);
            //pool.submit(primer);
        }
        pool.shutdown();
        while (!pool.isTerminated()){

        }

        for (boolean x: resultList){
            if (x){
                AmountOfPrime++;
            }
        }

        return AmountOfPrime - 1;

    }

    public void testChannel(){
        try {
            RandomAccessFile test = new RandomAccessFile("output.txt", "rw");
            FileChannel fc = test.getChannel();
            byte[] bytes = {0x01, 0x02, 0x03, 0x04, 0x06, 0x07, 0x01, 0x02, 0x03, 0x04, 0x06, 0x07, 0x01, 0x02, 0x03, 0x04, 0x06, 0x07, 0x01, 0x02, 0x03, 0x04, 0x06, 0x07
            ,0x01, 0x02, 0x03, 0x04, 0x06, 0x07,0x01, 0x02, 0x03, 0x04, 0x06, 0x07,0x01, 0x02, 0x03, 0x04, 0x06, 0x07,0x01, 0x02, 0x03, 0x04, 0x06, 0x07};
            ByteBuffer bb = ByteBuffer.allocate(256);
            System.out.println(fc.position());
            fc.write(ByteBuffer.wrap(bytes, 0 , 4));
            fc.write(ByteBuffer.wrap(bytes, 5, 10));
            fc.position(20);
            System.out.println(fc.position());
            fc.write(ByteBuffer.wrap(bytes, 11, 20));


//            bb.flip();
//            fc.write(bb);
//            bb.clear();



        }catch (IOException ex){
            ex.printStackTrace();
        }
    }

    public class testThread implements Runnable {
        FileChannel tfc;

        @Override
        public void run(){

        }
    }

    public void ReadSerial(String filename) {
        try {
            FileInputStream fis = new FileInputStream(filename);
            ObjectInputStream ois = new ObjectInputStream(fis);
            concurMeta META = (concurMeta) ois.readObject();
            System.out.println("Start position");
            for (long x: META.threadStartAt){
                System.out.println(x);
            }
            System.out.println("Loaded stuff");
            for (long x: META.workDoneOnEachPart){
                System.out.println(x);
            }
        }catch (IOException ex){
            ex.printStackTrace();
        }catch (ClassNotFoundException ex){
            ex.printStackTrace();
        }
    }

    public class isPrimeThread implements Runnable{
        int n;
        List<Boolean> rst;
        public isPrimeThread(int n, List<Boolean> resultLst){
            this.n = n;
            rst = resultLst;
        }
        public void run(){
            int count = 0;
            for (int i = 1; i <= Math.sqrt(this.n); i++){
                if ( this.n % i == 0 ){
                    count++;
                }else if (this.n == 2) {
                    rst.add(true);
                    return;
                }else if (this.n == 1){
                    rst.add(false);
                    return;
                }
            }
            if (count > 1){
                rst.add(false);
            }else {
                rst.add(true);
            }

            //System.out.println("  " + this.n + "  " + count + "  " );

        }
    }
}
