
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
       File ExistData = new File(filename+".DATAC");
       File ExistHead = new File(filename+".HEADC");
       ExecutorService pool = null;
       concurMeta metaFile = null;
       concurMeta currentMETA = null;
       if (chkDL.contentLength == -1){
           System.out.println("The server does not support concurrent download dropping to one connection");
           mainDL DL = new mainDL(url_, filename);
           DL.newDL();
       }else if (ExistData.exists() && ExistHead.exists()){
           try {
               FileInputStream fis = new FileInputStream(ExistHead.getName());
               ObjectInputStream ois = new ObjectInputStream(fis);
               currentMETA = (concurMeta) ois.readObject();
               pool = Executors.newFixedThreadPool(conn);
               for (int i = 0; i < currentMETA.StartPosThread.size(); i++){
                   if (currentMETA.EndPosThread.get(i) - currentMETA.StartPosThread.get(i) >= 0) {
                       System.out.println("Retrieving chunk number: " + i);
                       newCon worker = new newCon(i, currentMETA.StartPosThread.get(i), currentMETA.EndPosThread.get(i), currentMETA);
                       worker.fname = filename;
                       pool.execute(worker);
                   }
               }
           } catch (Exception e) {
               e.printStackTrace();
           }
       }
       else {
           metaFile = new concurMeta();
           //distribute file into array of content Length of 1 MB
           long sSize = 0;
           long endSize = chkDL.contentLength;
           while (chkDL.contentLength > 0){
               if (chkDL.contentLength > Math.pow(2,20)) {
                   metaFile.StartPosThread.add(sSize);
                   chkDL.contentLength -= Math.pow(2, 20);
                   metaFile.EndPosThread.add(sSize + (long) Math.pow(2,20));
                   sSize+= Math.pow(2,20) + 1;
               }else {
                   metaFile.StartPosThread.add(sSize);
                   metaFile.EndPosThread.add(endSize);
                   chkDL.contentLength -= Math.pow(2, 20);
               }
           }
           pool = Executors.newFixedThreadPool(conn);
           for (int i = 0; i < metaFile.StartPosThread.size(); i++){
               newCon worker = new newCon(i, metaFile.StartPosThread.get(i), metaFile.EndPosThread.get(i), metaFile);
               worker.fname = filename;
//               logger.log(Level.INFO, String.format("Byte Range: %d-%d, to Thread number: %d", metaFile.StartPosThread.get(i),
//                       metaFile.EndPosThread.get(i), i));
               pool.execute(worker);
           }
       }
       try {
           FileOutputStream fos = new FileOutputStream(filename + ".HEADC");
           ObjectOutputStream oos = new ObjectOutputStream(fos);
           if (metaFile != null) {
               oos.writeObject(metaFile);
           }else {
               oos.writeObject(currentMETA);
           }
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
       File HEAD = new File(filename + ".HEADC");
       System.out.println("Rename successful? : " + DATA.renameTo(new File(filename)));
       System.out.println("Meta file deleted or not? :" + HEAD.delete());
   }

    public class newCon implements Runnable{
        public newCon(int threadNum_, long sPOS, long ePOS, concurMeta metaF){
            ThreadNum = threadNum_;
            startingPOS = sPOS;
            endPos = ePOS;
            metaFile = metaF;
        }
        concurMeta metaFile;
        int ThreadNum;
        long headerContentLength;
        long startingPOS;
        long endPos;
        String fname;
        RandomAccessFile dataFile;
        public void run(){
            FileOutputStream fos = null;
            ObjectOutputStream oos = null;
            int currentByte = 0;
            try {
                this.dataFile = new RandomAccessFile(fname+".DATAC", "rw");
                fos = new FileOutputStream(fname+".HEADC");
            } catch (IOException e) {
                e.printStackTrace();
            }
            FileChannel datafile = this.dataFile.getChannel();
            Logger logger = Logger.getLogger("ThreadLog");
            mainDL DL = new mainDL(url, filename);
            logger.log(Level.INFO, "start Thread this thread is: " + ThreadNum, Thread.currentThread());
            try {
                DL.out.println(HelperFX.getPartContent(DL.url.getHost(), DL.url.getPath(), startingPOS, endPos));
                datafile.position(startingPOS);
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
                                //System.out.println(content);
                            }else {
                                datafile.write(ByteBuffer.wrap(byteBuffer, totalHeadByte, currentByte - totalHeadByte));
                                totalByte += currentByte - totalHeadByte;
                                this.metaFile.StartPosThread.set(ThreadNum, this.metaFile.StartPosThread.get(ThreadNum) + currentByte - totalHeadByte);
//                                if (ThreadNum == 0) {
//                                    System.out.println("For thread number 0, Current file pos: " + datafile.position() + " byte downloaded: " + totalByte);
//                                }
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
                        datafile.write(ByteBuffer.wrap(byteBuffer, 0, currentByte));
                        totalByte += currentByte;
                        this.metaFile.StartPosThread.set(ThreadNum, this.metaFile.StartPosThread.get(ThreadNum) + currentByte);
                        if (ThreadNum == 0) {
                            System.out.println("For thread number 0, Current file pos: " + datafile.position() + " byte downloaded: " + totalByte);
                        }
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
                        datafile.close();
                        oos.close();
                        break;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();

            }
        }
    }

}
