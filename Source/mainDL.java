import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class mainDL {
    String fname;
    URL url;
    int port;
    int totalByte = 0;
    int totalHeadByte;
    int headerContentLength;
    String headerETag;
    String headerLastMod;
    byte[] currentData;
    boolean rcv = false;
    boolean cte = false;
    StringBuilder headerContent;
    Socket sock;
    PrintWriter out;

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
            FileOutputStream dataFile = new FileOutputStream(fname + ".DATA", true);
            out.println(HelperFX.getDLRequest(url.getHost(), url.getPath()));
            int currentByte = 0;
            System.out.println("Download Starto!!");
            while (currentByte != 1) {
                currentData = new byte[1024];
                currentByte = sock.getInputStream().read(currentData);
                totalByte += currentByte;
                if (!rcv) {
                    headerContent.append(new String(currentData));
                    for (String content : headerContent.toString().split("\n")) {
                        if (!rcv) {
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
                            if (content.contains("Transfer-Encoding")) {
                                cte = content.split(": ")[1].replace("\r", "").equals("chunked");
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
                } else {
                    dataFile.write(currentData, 0, currentByte);

                }
                if (totalByte - totalHeadByte == headerContentLength) {
                    System.out.println("Download Completed!");
                    dataFile.close();
                    break;
                }

                if (cte) {
                    String st = new String(currentData);
                    for (String stx : st.split("/r/n")) {
                        System.out.println(stx);
                        System.out.println("this length of this is: " + stx.length());
                    }
                }

            }
            File HFile = new File(fname + ".HEAD");
            File DFile = new File(fname + ".DATA");
            HFile.delete();
            DFile.renameTo(new File(fname));
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public void startResume(File head, File data){
        try {
            long currentSize = data.getFreeSpace();
            BufferedReader readHead = new BufferedReader(new FileReader(head));
            FileOutputStream writeData = new FileOutputStream(data, true);
            String stx = "";
            currentData = new byte[1024];
            while (!stx.equals(null)){
                stx = readHead.readLine();
                if (stx.contains("headerContentLength")){
                    headerContentLength = Integer.parseInt(stx.split(": ")[1]);
                }
                if (stx.contains("headerETag")){
                    headerETag = stx.split(": ")[1];
                }
                if (stx.contains("headLastMod")){
                    headerLastMod = stx.split(": ")[1];
                }
            }
            currentData = new byte[1024];
            int currentByte = 0;
            while (currentByte != -1){
                currentByte = sock.getInputStream().read(currentData);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
