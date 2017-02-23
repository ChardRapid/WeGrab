package tencent.com.cftutils;

/**
 * Created by BALDOOR on 2016/12/7.
 */

public class DesEncUtil extends EncUtilBase {

    private static final String CHARSET = "UTF-8";
    public byte[] enc_buf;
    public byte[] key_buf;
    public byte[] raw_buf;

    public native boolean encrypt_des(int paramInt, byte[] paramArrayOfByte);

    public native boolean encrypt_des_withstringkey(byte[] paramArrayOfByte1, byte[] paramArrayOfByte2);

    public native boolean encrypt_des_withstringkey_onedes(byte[] paramArrayOfByte1, byte[] paramArrayOfByte2);

    public String getEncText(int i, String text) {
        boolean result = false;
            if (text.length() > 0) {
                try {
                    this.raw_buf = text.getBytes(CHARSET);
                    result = encrypt_des(i, this.raw_buf);
                } catch (Throwable throwable) {
                    throwable.printStackTrace();
                }
            }
        if(result){
            if (this.enc_buf != null) {
                try {
                    return new String(this.enc_buf, CHARSET);
                } catch (Throwable throwable) {
                    throwable.printStackTrace();
                }
            }
        }
        return "";
    }

}
