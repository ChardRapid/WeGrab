package tencent.com.cftutils;

/**
 * Created by BALDOOR on 2016/12/7.
 */

public class PassWdEncUtil extends EncUtilBase {

    private native boolean encrypt_passwd(byte paramByte, byte[] paramArrayOfByte);

    private native boolean encrypt_passwd1(byte paramByte, byte[] paramArrayOfByte);

    private native boolean encrypt_passwd2(byte[] paramArrayOfByte);

    private native boolean encrypt_passwd3(byte[] paramArrayOfByte);
}
