import com.sun.deploy.util.StringUtils;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class mainDL {
    public String fname;
    public URL url;
    int port;
    int totalByte = 0;
    int totalHeadByte = 0;
    int headerContentLength = 0;
    String headerETag = "";
    String headerLastMod = "";
    byte[] currentData;
    boolean rcv = false;
    boolean etc = false;
    StringBuilder headerContent;
    Socket sock;
    PrintWriter out;
    FileOutputStream dataFile;
    SocketAddress servAdr;

    public mainDL(String url_, String Filename) {

        headerContent = new StringBuilder();
        try {
            url = new URL(url_);
            if (url.getPort() == -1) {
                port = 80;
            } else {
                port = url.getPort();
            }
            sock = new Socket();
            servAdr = new InetSocketAddress(url.getHost(), port);
            sock.connect(servAdr);
            out = new PrintWriter(sock.getOutputStream(), true); //open a stream to write data to send to socket
            sock.setReceiveBufferSize(8192);
            //sock.setSoTimeout(3000);
        } catch (Exception e) {
            try {
                sock.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            e.printStackTrace();
        }
        fname = Filename;


    }

    public void newDL() {
        long TotalStartTime = System.nanoTime();
        currentData = new byte[8192];
        try {
            BufferedWriter headFile = null;
            if (chkDL.contentLength != -1) {
                headFile = new BufferedWriter(new FileWriter(fname + ".HEAD", false));
                dataFile = new FileOutputStream(fname + ".DATA", true);
                out.println(HelperFX.getDLRequest(url.getHost(), url.getPath()));
            }else {
                out.println(HelperFX.getCloseReq(url.getHost(), url.getPath()));
            }

            int currentByte = 0;
            System.out.println("Download Starto!!");
            while (currentByte != -1) {
                currentData = new byte[8192];
                currentByte = sock.getInputStream().read(currentData);
                totalByte += currentByte;
                //extract header content
                if (!rcv) {
                    headerContent.append(new String(currentData));
                    for (String content : headerContent.toString().split("\n")) {
                        if (!rcv) {
                            //System.out.println(content);
                            totalHeadByte += content.length() + 1;
                            if (content.contains("Content-Length")) {
                                headerContentLength = Integer.parseInt(content.split(": ")[1].replace("\r", ""));
                            }
                            if (content.contains("ETag")) {
                                headerETag = content.split(": ")[1].replace("\r", "");
                            }
                            if (content.contains("Last-Modified")) {
                                headerLastMod = content.split(": ")[1].replace("\r", "");
                            }
                            if (content.equals("\r")) {
                                rcv = true;
                            }
                            if (content.contains("Transfer-Encoding: chunked")){
                                ectDL();
                                etc = true;
                                return;
                            }
                        } else {
                            byte[] tempData = new byte[currentData.length - totalHeadByte];
                            int j = 0;
                            for (int i = totalHeadByte; i < currentData.length; i++) {
                                tempData[j] = currentData[i];
                                j++;
                            }
                            dataFile.write(tempData);
                            String tempST = String.format("headerContentLength: %d\nheaderETag: %s\nheadLastMod: %s",
                                    headerContentLength, headerETag, headerLastMod);
                            headFile.write(tempST);
                            headFile.close();
                            System.out.println("Header Extracted and Kept in .HEAD");
                            break;
                        }
                    }
                    //write data into a .DATA file for resume support
                } else {
                    dataFile.write(currentData, 0, currentByte);
                    if (headerContentLength != -1) {
                        System.out.println(String.format("Download %f percent", (double) (((double) totalByte - (double) totalHeadByte) / (double) headerContentLength) * 100.00));
                    }
                }
                //kill the download if the byte downloaded equals content length
                if (totalByte - totalHeadByte == headerContentLength) {
                    System.out.println("Download Completed!");
                    dataFile.close();
                    sock.close();
                    break;
                }
                //implementing chunked transfer encoding support (not finish will finish it after finishing resume)
                if (etc){
                    break;
                }

            }
            //delete and rename files after finish downloading completely
            long TotalEndTime = System.nanoTime();
            File HFile = new File(fname + ".HEAD");
            File DFile = new File(fname + ".DATA");
            HFile.delete();
            DFile.renameTo(new File(fname));
            System.out.println("Total Running time: " + (TotalEndTime - TotalStartTime)/1e9);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public void startResume(File head, File data){
        boolean checkContentLength = false;
        boolean checkETag = true;
        boolean checkLastMod = true;
        try {
            if (!sock.isConnected()) {
                sock.connect(servAdr);
            }
            long currentSize = data.length();
            BufferedReader readHead = new BufferedReader(new FileReader(head));
            currentData = new byte[8192];
            String line = "";
            while ((line = readHead.readLine()) != null){
                if (line.contains("headerContentLength")){
                    headerContentLength = Integer.parseInt(line.split(": ")[1]);
                    System.out.println("The header content Length is: " + line.split(": ")[1]);
                }
                if (line.contains("headerETag")){
                    headerETag = line.split(": ")[1];
                }
                if (line.contains("headLastMod")){
                    headerLastMod = line.split(": ")[1];
                }
            }
            System.out.println("Finish recovering head info");
            currentData = new byte[8192];
            int currentByte = 0;
            dataFile = new FileOutputStream(data, true);
            out.println(HelperFX.getResumeReq(url.getHost(), url.getPath(), currentSize));
            long currentStartSize = currentSize;
            boolean finished = false;
            while ((currentByte = sock.getInputStream().read(currentData)) != -1){
                currentSize += currentByte;
                //validating header
                if (!rcv){
                    String byteContent = new String(currentData);
                    for (String content: byteContent.split("\n")){
                        if (!rcv) {
                            totalHeadByte += content.length() + 1;
                            if (content.contains("Content-Length")) {
                                checkContentLength = (long) headerContentLength == currentStartSize + Long.parseLong(content.split(": ")[1].replace("\r", ""));
                                System.out.println(currentSize + Long.parseLong(content.split(": ")[1].replace("\r", "")));
                            }
                            if (content.contains("ETag")&& !headerETag.isEmpty()) {
                                checkETag = headerETag.equals(content.split(": ")[1].replace("\r", ""));
                            }
                            if (content.contains("Last-Modified") && !headerLastMod.isEmpty()) {
                                checkLastMod = headerLastMod.equals(content.split(": ")[1].replace("\r", ""));
                            }
                            if (content.equals("\r")) {
                                rcv = true;
                            }
                        }else{
                            byte[] tempData = new byte[currentData.length - totalHeadByte];
                            int j = 0;
                            for (int i = totalHeadByte; i < currentData.length; i++) {
                                tempData[j] = currentData[i];
                                j++;
                            }
                            dataFile.write(tempData);
                            System.out.println("Header Extracted from resume phase");
                            System.out.println(String.format("start resuming file, current file size %d, actual header file size %d ", currentSize, headerContentLength));
                            break;
                        }
                    }
                }else {
                    if (checkContentLength && checkETag && checkLastMod){
                        dataFile.write(currentData, 0, currentByte);
                        System.out.println(String.format("Download %f percent",(double)(((double)currentSize - (double)totalHeadByte)/(double)headerContentLength) *100.00)  );
                        if (currentSize - totalHeadByte == headerContentLength ){
                            System.out.println("Download Completed");
                            dataFile.close();
                            sock.close();
                            finished = true;
                            break;
                        }
                    }else {
                        System.out.println("The file you resume have changed therefore we will download a new file instead");
                        System.out.println(head.delete());
                        System.out.println(data.delete());
                        System.out.println("Starting the redownload!!");
                        newDL();
                        break;
                    }
                }

            }
            if (finished) {
                readHead.close();
                dataFile.close();
                System.out.println("Cleaning up and renaming!");
                System.out.println(head.delete());
                System.out.println(data.renameTo(new File(fname)));
            }else {
                System.out.println("Conneciton Lost!! you can resume again to redownload");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void ectDL(){
        //ect have no resume support since we don't know the content length
        totalHeadByte = 0;
        System.out.println("Starting CTE Download!");
        try {
            dataFile = new FileOutputStream(fname + ".HAMUEL");
            rcv = false;
            currentData = new byte[8192];
            int currentByte = 0;
            while ((currentByte = sock.getInputStream().read(currentData)) != -1) {
                System.out.println(currentByte);
                System.out.println(currentByte);
                dataFile.write(currentData, 0, currentByte);
            }
            dataFile.close();
            File DFile = new File(fname + ".HAMUEL");
            System.out.println("ECT Raw file downloaded!");
            FileInputStream decodedFile = new FileInputStream(fname + ".HAMUEL");
            FileOutputStream out = new FileOutputStream(fname + ".CTE");
            byte[] buffer = new byte[2048];
            int curB = 0;
            int leftOverByte = 0;
            int processByte = 0;
            //starting the decoding process
            System.out.println("Starting the decoding process");
            while ((curB = decodedFile.read(buffer)) != -1){
                String currentData = new String(buffer);
                if (leftOverByte > 0){
                    out.write(buffer, 0, leftOverByte);
                    leftOverByte = 0;
                }
                for (String chunk: currentData.split("\r\n")){
                    if (HelperFX.isNumeric(chunk)){
                        if (processByte <= 2048) {
                            out.write(buffer, chunk.length() + processByte + 2, Integer.parseInt(chunk));
                            processByte += curB + Integer.parseInt(chunk);
                        }else {
                            leftOverByte = processByte - (curB + 2 + Integer.parseInt(chunk));
                            processByte = 0;
                            break;
                        }
                    }
                }
            }
            out.close();
            decodedFile.close();
            System.out.println("Cleaning up");
            System.out.println(new File(fname + ".HAMUEL").delete());
            System.out.println(new File(fname+".CTE").renameTo(new File(fname)));
        }catch (IOException ex){
            System.out.println(new File(fname + ".HAMUEL").delete());
            System.out.println(new File(fname+".CTE").delete());
            ex.printStackTrace();
        }

    }
}
