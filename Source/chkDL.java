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
    StringBuilder allHead;
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
            while ((line = in.readLine()) != null){
                allHead.append(line + "\n");
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public boolean chkOK() {
        return allHead.toString().contains("200 OK");
    }

    public boolean chkredir(){
        return allHead.toString().contains("301 Moved Permanently");
    }
}