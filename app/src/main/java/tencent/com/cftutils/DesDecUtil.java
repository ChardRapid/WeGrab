package tencent.com.cftutils;

/**
 * Created by BALDOOR on 2016/12/7.
 */

public class DesDecUtil  extends EncUtilBase{

    private native boolean decrypt_des(int paramInt, byte[] paramArrayOfByte);

    private native boolean decrypt_des_withstringkey(byte[] paramArrayOfByte1, byte[] paramArrayOfByte2);
}
