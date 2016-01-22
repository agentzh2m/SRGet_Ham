import java.io.File;

/**
 * Created by Hamuel on 1/22/16.
 */
public class interfaceDL {
    public static void main(String[] args) {

    }

    public void initDL(String url_, String out){
        chkDL cdl = new chkDL(url_);
        if (cdl.chkOK()){
            mainDL sdl = new mainDL(url_,out);
            sdl.newDL();
        }else if (cdl.chkredir()){

        }
    }

    public void initResume(File head, File data){

    }
}
