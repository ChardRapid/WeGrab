package com.tenpay.cert;


/**
 * Created by BALDOOR on 2016/12/7.
 */

public class CertUtil extends tencent.com.cftutils.EncUtilBase{

    private native boolean encrypt();

    private native boolean gen_cert_apply_csr();

    private native boolean gen_qrcode();

    private native boolean get_certid();

    private native int get_last_error();

    private native boolean get_token();

    private native int get_token_count();

    private native boolean import_cert();

    private native boolean is_cert_exist();

    private native boolean set_token();

    private native boolean usr_sig();
}
