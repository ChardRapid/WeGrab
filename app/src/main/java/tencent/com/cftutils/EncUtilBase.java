package tencent.com.cftutils;

/**
 * Created by BALDOOR on 2016/12/7.
 */

public class EncUtilBase {
    public EncUtilBase() {
        try {
            //Log.i("LoadLibrary","cftutils");
            System.load("/data/data/tk.qcute.wegrab/lib/libcftutils_v1.2.3.so");
        } catch (Throwable throwable) {
            throwable.printStackTrace();
            try {
                System.loadLibrary("cftutils_v1.2.3");
            }catch (Throwable e){e.printStackTrace();}
        }
    }
}
