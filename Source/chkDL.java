import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.URL;

/**
 * Created by Hamuel on 1/22/16.
 */
public class chkDL {
    URL url;
    Socket sock;
    int port;
    PrintWriter out;
    public String keepNewLocation;
    public static int contentLength = -1;
    boolean redir = false;
    public chkDL(String url_) {
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
            out.println(HelperFX.getHeadReq(url.getHost(), url.getPath()));
            BufferedReader in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
            String line = "";
            System.out.println("Verifying header and download type");
            while ((line = in.readLine()) != null){
                if (line.isEmpty()){
                    break;
                }
               if (redir){
                   if (line.contains("Location")){
                       keepNewLocation = line.split(": ")[1];
                       System.out.println("Redirecting!!!");
                       break;
                   }
               }
                if (line.contains("Content-Length")){
                    contentLength = Integer.parseInt(line.split(": ")[1].replace("\r", ""));
                    System.out.println("Content Length detected");
                    System.out.println("Content Length is: " + contentLength);
                }
            }
            sock.close();
            System.out.println("Finish verifying");
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }


    public void chkredir(String stx){
        if (stx.contains("301 Moved Permanently") || stx.contains("302 Found")){
            redir = true;
        }
    }


}