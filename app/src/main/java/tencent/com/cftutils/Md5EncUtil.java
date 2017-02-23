package tencent.com.cftutils;


/**
 * Created by BALDOOR on 2016/11/30.
 */

public class Md5EncUtil extends EncUtilBase {

    public byte[] enc_buf;
    public byte[] raw_buf;

    public native boolean encrypt_md5(int paramInt, byte[] paramArrayOfByte);
}
