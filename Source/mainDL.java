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
    StringBuilder headerContent;
    public mainDL(String url_, String Filename){

        headerContent = new StringBuilder();
        try {
            url = new URL(url_);
            if (url.getPort() == -1) {
                port = 80;
            }else {
                port = url.getPort();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        fname = Filename;
    }
    public void newDL(){
        currentData = new byte[1024];
        try {
            BufferedWriter headFile =new BufferedWriter(new FileWriter(fname + ".HEAD", false));
            FileOutputStream dataFile = new FileOutputStream(fname + ".DATA", true);
            SocketAddress servAdr = new InetSocketAddress(url.getHost(), port);
            Socket sock = new Socket();
            sock.connect(servAdr);
            PrintWriter out = new PrintWriter(sock.getOutputStream(), true); //open a stream to write data to send to socket
            out.println(HelperFX.getDLRequest(url.getHost(), url.getPath()));
            sock.setReceiveBufferSize(1024);
            int currentByte = 0;
            while (true){
                currentData = new byte[1024];
                currentByte=sock.getInputStream().read(currentData);
                totalByte += currentByte;
                if (!rcv){
                    headerContent.append(new String(currentData));
                    for (String content: headerContent.toString().split("\n")){
                        if (!rcv){
                            totalHeadByte+= content.length() + 1;
                            if (content.contains("Content-Length")){
                                headerContentLength = Integer.parseInt(content.split(": ")[1].replace("\r", ""));
                            }
                            if (content.contains("ETag")){
                                headerETag = content.split(": ")[1].replace("\r", "");
                            }
                            if (content.contains("Last-Modified")){
                                headerLastMod = content.split(": ")[1].replace("\r", "");
                            }
                            if (content.equals("\r")){
                                rcv = true;
                            }
                        }else {
                            byte[] tempData = new byte[currentData.length-totalHeadByte];
                            int j = 0;
                            for (int i = totalHeadByte; i < currentData.length; i++ ){
                                tempData[j] = currentData[i];
                                j++;
                            }
                            dataFile.write(tempData);
                            String tempST = String.format("headerContentLength: %d\nheaderETag: %s\nheadLastMod: %s",
                                    headerContentLength, headerETag, headerLastMod);
                            headFile.write(tempST);
                            headFile.close();
                            break;
                        }
                    }
                }else {
                    dataFile.write(currentData, 0, currentByte);

                }
                if (totalByte - totalHeadByte == headerContentLength){
                    System.out.println("Download Completed!");
                    dataFile.close();
                    break;
                }

            }
            File HFile = new File(fname + ".HEAD");
            File DFile = new File(fname + ".DATA");
            HFile.delete();
            DFile.renameTo(new File(fname));
        }catch (IOException ex){
            ex.printStackTrace();
        }
    }
}
