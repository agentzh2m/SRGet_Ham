

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;

/**
 * Created by Hamuel on 1/14/16.
 */
public class Main2 {
    static String servHost;
    static int port;
    static String path;
    static String FileName;

    public static void Second(String url) {
        KeepData rs = new KeepData();
        //extracting URL Data
        try {
            URL TheUrl = new URL(url);
            servHost = TheUrl.getHost();
            if (TheUrl.getPort() == -1){
                port = 80;
            }else {
                port = TheUrl.getPort();
            }
            path = TheUrl.getPath();

        } catch (MalformedURLException e) {
            System.out.println("The URL format is incorrect");
            //e.printStackTrace();
        }

        FileName = servHost.split("\\.")[1];
        File fs = new File(FileName+".ser");
        //Insert Socket Detail and open the socket
        SocketAddress servAdr = new InetSocketAddress(servHost, port);
        Socket s = new Socket();

        //init values to track how much have we downloaded the data
        int totalByte = 0;
        int currentByte = 0;
        int headerSize = 0;
        boolean startRecv = false;
        String contentLength = null;
        StringBuilder Data = new StringBuilder();
        StringBuilder headerContent = new StringBuilder();

        try {
            //Read the serialize file if the file exist
            if (fs.exists()) {
                FileInputStream SerFileIn = new FileInputStream(FileName + ".ser");
                ObjectInputStream SerIn = new ObjectInputStream(SerFileIn);
                rs = (KeepData) SerIn.readObject();
                SerIn.close();
                SerFileIn.close();
                totalByte = rs.getByte();
                Data = rs.getData();
            }

            s.connect(servAdr); //connect the socket to server
            PrintWriter out = new PrintWriter(s.getOutputStream(), true); //open a stream to write data to send to socket
            BufferedWriter output = new BufferedWriter(
                    new FileWriter(String.format("%s.txt", FileName))); //create a new file to keep "data"
            out.println(HelperFX.getDLRequest(servHost, path )); // send request to server



            int c = 0;
            s.setReceiveBufferSize(1024);

            while (true){
                //create Serialize file for resume support
                FileOutputStream tempDL = new FileOutputStream(FileName+".ser"); //create new file for storing serializable
                ObjectOutputStream SerOut = new ObjectOutputStream(tempDL); //open stream to write serializable object

                byte[] bb = new byte[1024];
                currentByte = s.getInputStream().read(bb); //store byte in bb and byte number in currentByte
                totalByte += currentByte; //add currentByte to the total Byte
                String stt = new String(bb, StandardCharsets.UTF_8); //convert byte to String
                //check that the header have been read already or not if yes start appending data
                if (startRecv){
                    Data.append(stt);
                    Data.append("");
                }
                //header manipulation to get Content-Length in Byte
                if (stt.contains("Content-Length")){
                    String xx[] = stt.split("\n");
                    boolean headerExtracted = false;
                    for (String x: xx ){
                        if (!startRecv) {
                            headerSize += x.getBytes().length + 1; //calculating total headSize
                            headerContent.append(x + "\n"); //keep header content
                        }
                        if (headerExtracted){
                            Data.append(x+ "\n"); //Keep Data that is not a part of the header
                        }
                        if (x.contains("Content-Length")){
                            contentLength = x.split(": ")[1]; //extract content length in Byte
                        }
                        //detect the head of the header
                        if (x.equals("\r")){
                            startRecv = true;
                            headerExtracted = true;
                       }
                    }
                }

                rs.storeByte(totalByte);
                rs.storeData(Data);
                SerOut.writeObject(rs);
                SerOut.flush();
                SerOut.close();

                if (startRecv && (totalByte - headerSize >= Integer.parseInt(contentLength.replace("\r", "")))){
                    SerOut.close();
                    tempDL.close();

                    boolean SerDel = fs.delete();
                    System.out.println("<-----Below is Header Content ----->");
                    System.out.println(headerContent);
                    System.out.println("<-----Below is Data Content ----->");
                    System.out.println(Data);
                    output.write(Data.toString());
                    System.out.println(String.format("The serialize data have been deleted %b", SerDel));
                    System.out.println(String.format("The total amount of data recieve is %d byte ", totalByte ));
                    //System.out.println(String.format("The header size is %d", headerSize) );
                    System.out.println("The actual content length is: " + contentLength);
                    System.out.println(String.format("Total Data Content is %d byte", totalByte - headerSize));
                    System.out.println("Download Completed!");
                    output.close();
                    s.close();
                    break;
                }
                //Keep Data in case of resume




            }

        } catch (IOException ex) {
            ex.printStackTrace();
        }catch (ClassNotFoundException exx){
            exx.printStackTrace();
        }

    }



}
