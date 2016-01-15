
import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;

/**
 * Created by Hamuel on 1/13/16.
 */
public class Main {
    public static void First() {

        String servHost = "www.pspmarine.com";
        int port = 80;
        SocketAddress servAdr = new InetSocketAddress(servHost, port);
        Socket s = new Socket();
        StringBuilder sb = new StringBuilder();
        int allSt = 0;
        int headerSize = 0;
        BufferedWriter output = null;
        try {
            output = new BufferedWriter(
                    new FileWriter(String.format("%s.txt", servHost.split("\\.")[1])));
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            s.connect(servAdr);
            String req = HelperFX.getDLRequest(servHost, "/");
            PrintWriter out =
                    new PrintWriter(s.getOutputStream(), true);
            BufferedReader in = new BufferedReader( new InputStreamReader(s.getInputStream()));
            out.println(req);
            String st = "" ;
            int content = 0;
            boolean startReceiving = false;
            boolean neverRecieve = true;
            while (true){
                s.setReceiveBufferSize(1024);
                st = in.readLine();
                System.out.println(st);
                if ((sb.length() > content) && startReceiving){
                     s.close();
                     output.write(sb.toString());
                     //System.out.println(sb);
                     System.out.println(allSt);
                     System.out.println(sb.length());
                     output.close();
                     System.out.println("Download completed successfully");
                     break;
                }
                if (startReceiving) {
                    allSt += st.getBytes().length;
                    sb.append(st + "\n");
                }
                if (st.contains("Content-Length")){
                    content = Integer.parseInt(st.split(": ")[1]);
                }

                if (st.isEmpty() && neverRecieve){
                    startReceiving = true;
                    neverRecieve = false;
                }


            }

        } catch (Exception e) {
            try {
                s.close();
                output.write(sb.toString());
                output.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            System.out.println(allSt);
            System.out.println(sb.length());
            System.out.println("no more data to retrieved assume file is completed");

        }

    }


}
