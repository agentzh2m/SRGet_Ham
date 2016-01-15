
import com.sun.tools.javac.parser.UnicodeReader;
import com.sun.tools.javac.util.StringUtils;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Hamuel on 1/14/16.
 */
public class Main2 {
    public static void Second() {
        String servHost = "www.muic.mahidol.ac.th";
        int port = 80;
        SocketAddress servAdr = new InetSocketAddress(servHost, port);
        Socket s = new Socket();
        try {
            s.connect(servAdr);
            PrintWriter out = new PrintWriter(s.getOutputStream(), true);
            BufferedWriter output = new BufferedWriter(
                    new FileWriter(String.format("%s.txt", servHost.split("\\.")[1])));
            out.println(HelperFX.getDLRequest(servHost, "/eng/"));


            int totalByte = 0;
            int currentByte = 0;
            int headerSize = 0;
            boolean startRecv = false;
            String contentLength = null;
            StringBuilder Data = new StringBuilder();
            StringBuilder headerContent = new StringBuilder();
            int c = 0;
            s.setReceiveBufferSize(1024);
            while (true){
                byte[] bb = new byte[1024];
                currentByte = s.getInputStream().read(bb);
                totalByte += currentByte;
                String stt = new String(bb, StandardCharsets.UTF_8);

                if (startRecv){
                    Data.append(stt);
                    Data.append("");
                }

                if (stt.contains("Content-Length")){
                    String xx[] = stt.split("\n");
                    boolean headerExtracted = false;
                    for (String x: xx ){
                        if (!startRecv) {
                            headerSize += x.getBytes().length + 1;
                            headerContent.append(x + "\n");
                        }
                        if (headerExtracted){
                            Data.append(x+ "\n");
                        }
                        if (x.contains("Content-Length")){
                            contentLength = x.split(": ")[1];
                        }
                        if (x.equals("\r")){
                            startRecv = true;
                            headerExtracted = true;
                       }
                    }
                }


                if (startRecv && (totalByte - headerSize >= Integer.parseInt(contentLength.replace("\r", "")))){
                    System.out.println("<-----Below is Header Content ----->");
                    System.out.println(headerContent);
                    System.out.println("<-----Below is Data Content ----->");
                    System.out.println(Data);
                    output.write(Data.toString());
                    System.out.println(String.format("The total amount of data recieve is %d byte ", totalByte ));
                    //System.out.println(String.format("The header size is %d", headerSize) );
                    System.out.println("The actual content length is: " + contentLength);
                    System.out.println(String.format("Total Data Content is %d byte", totalByte - headerSize));
                    System.out.println("Download Completed!");
                    output.close();
                    s.close();
                    break;
                }



            }

        } catch (IOException ex) {
            ex.printStackTrace();
        }

    }



}
