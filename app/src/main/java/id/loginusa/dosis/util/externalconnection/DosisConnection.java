package id.loginusa.dosis.util.externalconnection;

import java.io.IOException;
import java.util.Map;

import id.loginusa.dosis.util.Logging;

/**
 * Created by mfachmirizal on 10-May-16.
 */
public class DosisConnection {

    public static void sendRequest(Koneksi kon,Map<String,String> parameter) throws IOException{
        kon.startConnection(parameter);
    }
}