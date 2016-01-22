import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

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
    boolean cte = false;
    StringBuilder headerContent;
    Socket sock;
    PrintWriter out;
    FileOutputStream dataFile;

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
            SocketAddress servAdr = new InetSocketAddress(url.getHost(), port);
            sock.connect(servAdr);
            out = new PrintWriter(sock.getOutputStream(), true); //open a stream to write data to send to socket
            sock.setReceiveBufferSize(1024);
        } catch (Exception e) {
            e.printStackTrace();
        }
        fname = Filename;


    }

    public void newDL() {
        currentData = new byte[1024];
        try {
            BufferedWriter headFile = new BufferedWriter(new FileWriter(fname + ".HEAD", false));
            dataFile = new FileOutputStream(fname + ".DATA", true);
            out.println(HelperFX.getDLRequest(url.getHost(), url.getPath()));
            int currentByte = 0;
            System.out.println("Download Starto!!");
            while (currentByte != -1) {
                currentData = new byte[1024];
                currentByte = sock.getInputStream().read(currentData);
                totalByte += currentByte;
                //extract header content
                if (!rcv) {
                    headerContent.append(new String(currentData));
                    for (String content : headerContent.toString().split("\n")) {
                        if (!rcv) {
                            System.out.println(content);
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
                    System.out.println(String.format("Download %f percent",(double)(((double)totalByte - (double)totalHeadByte)/(double)headerContentLength) * 100.00)  );

                }
                //kill the download if the byte downloaded equals content length
                if (totalByte - totalHeadByte == headerContentLength) {
                    System.out.println("Download Completed!");
                    dataFile.close();
                    sock.close();
                    break;
                }
                //implementing chunked transfer encoding support (not finish will finish it after finishing resume)
                if (cte) {
                    String st = new String(currentData);
                    for (String stx : st.split("/r/n")) {
                        System.out.println(stx);
                        System.out.println("this length of this is: " + stx.length());
                    }
                }

            }
            //delete and rename files after finish downloading completely
            File HFile = new File(fname + ".HEAD");
            File DFile = new File(fname + ".DATA");
            HFile.delete();
            DFile.renameTo(new File(fname));
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public void startResume(File head, File data){
        boolean checkContentLength = false;
        boolean checkETag = true;
        boolean checkLastMod = true;
        try {
            long currentSize = data.length();
            BufferedReader readHead = new BufferedReader(new FileReader(head));
            currentData = new byte[1024];
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
            currentData = new byte[1024];
            int currentByte = 0;
            dataFile = new FileOutputStream(data, true);
            out.println(HelperFX.getResumeReq(url.getHost(), url.getPath(), currentSize));
            long currentStartSize = currentSize;
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
            readHead.close();
            dataFile.close();
            System.out.println("Cleaning up and renaming!");
            System.out.println(head.delete());
            System.out.println(data.renameTo(new File(fname)));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void ectDL(){
        //ect have no resume support since we don't know the content length
    }
}
