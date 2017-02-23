package com.tencent.tenpay.cert;

/**
 * Created by BALDOOR on 2016/12/7.
 */

public class CertUtil {

    private native int get_last_error();

    private native int get_token_count();

    private native boolean is_cert_exist();

    private native boolean set_token();

    private native boolean usr_sig();
}
