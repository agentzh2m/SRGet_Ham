import com.sun.xml.internal.ws.policy.privateutil.PolicyUtils;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

public class concurDL{
    String url;
    String filename;
   public concurDL(String url_, String filename, int conn){
       Logger logger = Logger.getLogger("LogConstructor");
       url = url_;
       this.filename = filename;
       if (chkDL.contentLength == -1){
           System.out.println("The server does not support concurrent download dropping to one connection");
           mainDL DL = new mainDL(url_, filename);
           DL.newDL();
       }else {
           long splitSize = chkDL.contentLength / 10;
           //distribute file into array of content Length
           List<Long> partitionSize = new ArrayList<>();
           partitionSize.add(splitSize + (chkDL.contentLength % 10));
           logger.log(Level.INFO, "Split size is: " + splitSize);
           long curSize = splitSize + (chkDL.contentLength % 10);
           concurMeta metaFile = new concurMeta(10);
           metaFile.threadStartAt[0] = 0;
           for (int i = 1; i < 10; i++){
               metaFile.threadStartAt[i] = curSize;
               curSize += splitSize;
               partitionSize.add(curSize);
           }
           int c = 0;
           //List<Thread> runningThread = Collections.synchronizedList(new ArrayList<>());


           ExecutorService pool = Executors.newFixedThreadPool(conn);
           for (long size: partitionSize){
               newCon worker = new newCon(c, partitionSize.get(0), size, splitSize, metaFile);
               //Thread myTH = new Thread(TH);
               //runningThread.add(myTH);
               worker.fname = filename;
               logger.log(Level.INFO, String.format("Assign byte: %d, to Thread number: %d", size, c));
               pool.execute(worker);
               metaFile.workDoneOnEachPart[c] = splitSize;
               //myTH.start();
               c++;
           }
           metaFile.workDoneOnEachPart[0] = splitSize + chkDL.contentLength % 10;
           try {
               FileOutputStream fos = new FileOutputStream(filename + ".HEADC");
               ObjectOutputStream oos = new ObjectOutputStream(fos);
               oos.writeObject(metaFile);
               oos.close();
           }catch (Exception ex){
               ex.printStackTrace();
           }
           pool.shutdown();
           while (!pool.isTerminated()){
               try {
                   Thread.sleep(500);
               } catch (InterruptedException e) {
                   e.printStackTrace();
               }
           }
           File DATA = new File(filename + ".DATAC");
           System.out.println("Rename successful? : " + DATA.renameTo(new File(filename)));


//           chkThread cTH = new chkThread();
//           cTH.currentTH = pool;
//           cTH.numberOfThreads = partitionSize.size();
//           cTH.fname = filename;
//           Thread myCTH = new Thread(cTH);
//           myCTH.start();

       }
   }

    public class newCon implements Runnable{
        public newCon(int threadNum_, long initSize, long size_, long splitSize_, concurMeta metaF){
            ThreadNum = threadNum_;
            initialSize = initSize;
            size = size_;
            splitSize = splitSize_;
            metaFile = metaF;
        }
        concurMeta metaFile;
        long size;
        long initialSize;
        int ThreadNum;
        long splitSize;
        long headerContentLength;
        long startingPOS;
        String fname;
        FileOutputStream dataFile;
        public void run(){
            FileOutputStream fos = null;
            ObjectOutputStream oos = null;
            int currentByte = 0;
            try {
                this.dataFile = new FileOutputStream(fname+".DATAC");
                fos = new FileOutputStream(fname+".HEADC");
            } catch (IOException e) {
                e.printStackTrace();
            }
            FileChannel datafile = this.dataFile.getChannel();
            Logger logger = Logger.getLogger("ThreadLog");
            mainDL DL = new mainDL(url, filename);
            logger.log(Level.INFO, "start Thread this thread is: " + ThreadNum, Thread.currentThread());
            try {
                if (ThreadNum == 0){
                    DL.out.println(HelperFX.getPartContent(DL.url.getHost(), DL.url.getPath(), 0, initialSize));
                    datafile.position(0);
                    startingPOS = 0;
                }else{
                    DL.out.println(HelperFX.getPartContent(DL.url.getHost(), DL.url.getPath(), (size - splitSize) + 1, size));
                    datafile.position((size - splitSize) + 1);
                    startingPOS = (size - splitSize) + 1;
                }

                byte[] byteBuffer;
                boolean rcv = false;
                int totalByte = 0;
                while (currentByte != -1){
                    oos = new ObjectOutputStream(fos);
                    byteBuffer = new byte[1024];
                    currentByte = DL.sock.getInputStream().read(byteBuffer);
                    int totalHeadByte = 0;
                    if (!rcv){
                        String chunkByte = new String(byteBuffer);
                        for (String content: chunkByte.split("\n")) {
                            if (!rcv) {
                                totalHeadByte += content.length() + 1;
                                if (content.equals("\r")) {
                                    rcv = true;
                                }
                             
                                if (content.contains("Content-Length")){
                                    headerContentLength = Long.parseLong(content.split(": ")[1].replace("\r", ""));
                                }
                                System.out.println(content);
                            }else {
                                //datafile.position(startingPOS + totalByte);
                                datafile.write(ByteBuffer.wrap(byteBuffer, totalHeadByte, currentByte - totalHeadByte));
                                totalByte += currentByte - totalHeadByte;
                                this.metaFile.workDoneOnEachPart[ThreadNum] -= currentByte;
                                try {
                                    oos.writeObject(metaFile);
                                }catch (IOException ex){
                                    oos.close();
                                    fos.close();
                                }
                                break;
                            }
                        }
                    }else {
//                        logger.log(Level.INFO, String.format("Downloaded byte: %d, Thread number: %d, Split Size: %d, current chanel pos: %d",
//                                totalByte, ThreadNum, splitSize, datafile.position()));
                        //datafile.position(startingPOS + totalByte);
                        datafile.write(ByteBuffer.wrap(byteBuffer, 0, currentByte));
                        totalByte += currentByte;
                        this.metaFile.workDoneOnEachPart[ThreadNum] -= totalByte;
                        try {
                            oos.writeObject(metaFile);
                        }catch (IOException ex){
                            oos.close();
                            fos.close();
                        }
                    }

                    if (headerContentLength - totalHeadByte == totalByte){
                        logger.log(Level.INFO, "Thread finish downloading, thread number" + ThreadNum);
                        DL.sock.close();
                        this.dataFile.close();
                        oos.close();
                        break;
                    }
                    //oos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();

            }
        }
    }

    public class chkThread implements Runnable{
        ExecutorService currentTH;
        int numberOfThreads;
        String fname;
        @Override
        public void run(){
//            int deadThreads = 0;
//            while (numberOfThreads != deadThreads){
//                for (Thread thread: currentTH){
//                    if (!thread.isAlive()){
//                        deadThreads++;
//                        currentTH.remove(thread);
//                        Logger.getAnonymousLogger().log(Level.INFO,"Dead Thread detected");
//                        break;
//                    }
//                }
//                try {
//                    Thread.sleep(500);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
            while (!currentTH.isTerminated()){
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            File DATA = new File(filename + ".DATAC");
            System.out.println("Rename successful?: " + DATA.renameTo(new File(filename)));

        }
    }
}
