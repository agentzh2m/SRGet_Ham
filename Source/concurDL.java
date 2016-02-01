import java.io.*;
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
           int splitSize = chkDL.contentLength / conn;
           //distribute file into array of content Length
           List<Integer> partitionSize = new ArrayList<>();
           partitionSize.add(splitSize + chkDL.contentLength % conn);
           int curSize = splitSize;
           RandomAccessFile dataFile = null;
           try {
               dataFile = new RandomAccessFile(filename+".DATAC","rwd");
               dataFile.setLength(chkDL.contentLength);
           } catch (IOException e) {
               e.printStackTrace();
           }
           for (int i = 1; i < conn; i++){
               curSize += splitSize;
               partitionSize.add(curSize);
           }
           logger.log(Level.INFO, "Array list of size", partitionSize);
           int c = 0;
           for (int size: partitionSize){
               newCon TH = new newCon();
               Thread myTH = new Thread(TH);
               TH.initalSize = partitionSize.get(0);
               TH.size = size;
               TH.dataFile = dataFile;
               TH.ThreadNum = c;
               myTH.start();
               c++;
           }
       }
   }

    public class newCon implements Runnable{
        int size;
        int initalSize;
        int ThreadNum;
        RandomAccessFile dataFile;
        public void run(){
            Logger logger = Logger.getLogger("ThreadLog");
            mainDL DL = new mainDL(url, filename);
            logger.log(Level.ALL, "start Thread this thread is", Thread.currentThread());
            DL.out.println(HelperFX.getResumeReq(DL.url.getHost(), DL.url.getPath(), size));
            try {
                int currentByte = 0;
                byte[] byteBuffer = new byte[1024];
                boolean rcv = false;
                dataFile.seek(size - initalSize);
                int totalByte = 0;
                while (currentByte != -1){
                    currentByte = DL.sock.getInputStream().read(byteBuffer);
                    logger.log(Level.INFO, String.format("Total DL for this Thread: %d , On Thread Number: %d", totalByte, ThreadNum));
                    int totalHeadByte = 0;
                    if (!rcv){
                        String chunkByte = new String(byteBuffer);
                        for (String content: chunkByte.split("\n")) {
                            if (!rcv) {
                                totalHeadByte += content.length() + 1;
                                if (content.equals("\r")) {
                                    rcv = true;
                                }
                            }else {
                                logger.log(Level.FINE, "This thread finish parsing head", Thread.currentThread());
                                logger.log(Level.INFO, String.format("TotalHeadByte: %d, currentByte: %d, ", totalHeadByte, currentByte));
                                dataFile.write(byteBuffer, totalHeadByte, currentByte - totalHeadByte);
                                totalByte += currentByte - totalHeadByte;
                            }
                        }
                    }else {
                        totalByte += currentByte;
                        dataFile.write(byteBuffer, 0, currentByte);
                    }

                    if (totalByte == size){
                        logger.log(Level.INFO, "Thread finish downloading");
                        DL.sock.close();
                        break;

                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
