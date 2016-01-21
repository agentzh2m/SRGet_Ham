/**
 * Created by Hamuel on 1/15/16.
 */
public class HelperFX {
    public static String getDLRequest( String serv, String objname ){
        return String.format("GET %s HTTP/1.1\r\n" + "Host: %s" + "\r\n\r\n", objname, serv );
    }

    public static String getResumeReq(String serv, String objname, long Range ){
        return String.format("GET %s HTTP/1.1\r\n" + "Host: %s\r\n" +"Range: bytes=%d-\r\n"
                +"\r\n\r\n", objname, serv, Range );
    }


    public static String unEscapeString(String s){
        StringBuilder sb = new StringBuilder();
        for (int i=0; i<s.length(); i++)
            switch (s.charAt(i)){
                case '\n': sb.append("\\n"); break;
                case '\t': sb.append("\\t"); break;
                case '\r': sb.append("\\r"); break;
                default: sb.append(s.charAt(i));
            }
        return sb.toString();
    }
}
