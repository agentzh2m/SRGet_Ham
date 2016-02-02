import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;
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
           long splitSize = chkDL.contentLength / conn;
           //distribute file into array of content Length
           List<Long> partitionSize = new ArrayList<>();
           partitionSize.add(splitSize + (chkDL.contentLength % conn));
           logger.log(Level.INFO, "Split size is: " + splitSize);
           RandomAccessFile dataFile = null;
           try {
               dataFile = new RandomAccessFile(filename+".DATAC","rw");
           } catch (IOException e) {
               e.printStackTrace();
           }
           long curSize = splitSize + (chkDL.contentLength % conn);
           for (int i = 1; i < conn; i++){
               curSize += splitSize;
               partitionSize.add(curSize);
           }
           int c = 0;
           List<Thread> runningThread = new ArrayList<>();
           for (long size: partitionSize){
               newCon TH = new newCon(c, partitionSize.get(0), size, splitSize);
               Thread myTH = new Thread(TH);
               runningThread.add(myTH);
               TH.dataFile = dataFile;
               logger.log(Level.INFO, String.format("Assign byte: %d, to Thread number: %d", size, c));
               myTH.start();
               c++;
           }
           chkThread cTH = new chkThread();
           cTH.currentTH = runningThread;
           cTH.numberOfThreads = partitionSize.size();
           cTH.dataFile = dataFile;
           Thread myCTH = new Thread(cTH);
           myCTH.start();


       }
   }

    public class newCon implements Runnable{
        public newCon(int threadNum_, long initSize, long size_, long splitSize_){
            ThreadNum = threadNum_;
            initialSize = initSize;
            size = size_;
            splitSize = splitSize_;
        }
        long size;
        long initialSize;
        int ThreadNum;
        long splitSize;
        long headerContentLength;
        long startingPOS;
        RandomAccessFile dataFile;
        public void run(){
            int currentByte = 0;
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
                    //logger.log(Level.INFO, "This thread seek dataFile to: " + (size - splitSize) + 1 +", this is thread number: " + ThreadNum);
                }

                byte[] byteBuffer;
                boolean rcv = false;
                int totalByte = 0;
                while (currentByte != -1){
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
                                    //datafile.lock(size-splitSize, headerContentLength, true);
                                }
                                System.out.println(content);
                            }else {
                                if (ThreadNum == 1){
                                    //System.out.println(datafile.position());
                                }
                                datafile.position(startingPOS + totalByte);
                                datafile.write(ByteBuffer.wrap(byteBuffer, totalHeadByte, currentByte - totalHeadByte));
                                totalByte += currentByte - totalHeadByte;
                                break;
                            }
                        }
                    }else {
//                        logger.log(Level.INFO, String.format("Downloaded byte: %d, Thread number: %d, Split Size: %d, current chanel pos: %d",
//                                totalByte, ThreadNum, splitSize, datafile.position()));
                        datafile.position(startingPOS + totalByte);
                        datafile.write(ByteBuffer.wrap(byteBuffer, 0, currentByte));
                        totalByte += currentByte;
                    }

                    if (headerContentLength - totalHeadByte == totalByte){
                        logger.log(Level.INFO, "Thread finish downloading, thread number" + ThreadNum);
                        DL.sock.close();
                        break;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();

            }
        }
    }

    public class chkThread implements Runnable{
        List<Thread> currentTH;
        int numberOfThreads;
        RandomAccessFile dataFile;
        @Override
        public void run(){
            int deadThreads = 0;
            while (numberOfThreads != deadThreads){
                for (Thread thread: currentTH){
                    if (!thread.isAlive()){
                        deadThreads++;
                        currentTH.remove(thread);
                        Logger.getAnonymousLogger().log(Level.INFO,"Dead Thread detected");
                        break;
                    }
                }
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            try {
                dataFile.close();
                File DATA = new File(filename + ".DATAC");
                System.out.println("Rename successful?: " + DATA.renameTo(new File(filename)));
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }
}
