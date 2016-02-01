import java.io.File;

/**
 * Created by Hamuel on 1/22/16.
 */
public class interfaceDL {
    public static void main(String[] args) {
        interfaceDL start = new interfaceDL();
        if (args.length == 5){
            concurDL cDL = new concurDL(args[4], args[2], Integer.parseInt(args[3]));
        }else if (args.length == 3){
            start.startDL(args[2], args[1]);
        }else {
            System.out.println("Usage: srget -o <output file> [-c <number of connection>] <URL>");
        }
    }

    public void initDL(String url_, String out){
        chkDL cdl = new chkDL(url_);
        if (cdl.redir) {
            mainDL sdl = new mainDL(cdl.keepNewLocation, out);
                sdl.newDL();
        }else {
            mainDL sdl = new mainDL(url_, out);
                sdl.newDL();
        }
    }
    public void initResume(File head, File data , String out, String url_){
        mainDL sdl = new mainDL(url_, out);
        sdl.startResume(head, data);

    }

    public void startDL(String url, String filename){
        File headFile = new File(filename+".HEAD");
        File dataFile = new File(filename+".DATA");
        if (headFile.exists() && dataFile.exists()){
            initResume(headFile, dataFile, filename, url);
        }else {
            initDL(url, filename);
        }

    }
}
