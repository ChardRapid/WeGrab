package tk.qcute.wegrab;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

import static de.robv.android.xposed.XposedBridge.log;
import static de.robv.android.xposed.XposedHelpers.callMethod;
import static de.robv.android.xposed.XposedHelpers.callStaticMethod;
import static de.robv.android.xposed.XposedHelpers.findAndHookConstructor;
import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;
import static de.robv.android.xposed.XposedHelpers.findClass;
import static de.robv.android.xposed.XposedHelpers.findFirstFieldByExactType;
import static de.robv.android.xposed.XposedHelpers.getObjectField;
import static de.robv.android.xposed.XposedHelpers.newInstance;



public class QQ {
    //msg counter
    private static int msgCount = 1;
    //context
    private static Object ApplicationContext = null;
    private static Object HotChatManager = null;
    private static Object TicketManager = null;
    private static Object TroopManager = null;
    private static Object FriendManager = null;
    private static ClassLoader walletClassLoader = null;
    private static Class<?> pluginClass = null;
    //static encryption algorithm
    public static tencent.com.cftutils.DesEncUtil des;
    //message filed
    private static long msgUid;
    private static int istroop;
    private static String senderuin;
    private static String frienduin;
    private static String selfuin;
    //message list
    private static ArrayList<Tuple>msgList = new ArrayList<>();
    private static class Tuple {
        private long msgUid;
        private int istroop;
        private String senderuin;
        private String frienduin;
        private String selfuin;

        public Tuple(long msgUid, int istroop, String senderuin, String frienduin, String selfuin) {
            this.msgUid = msgUid;
            this.istroop = istroop;
            this.senderuin = senderuin;
            this.frienduin = frienduin;
            this.selfuin = selfuin;
        }
    }

    public static void hookPackage(final XC_LoadPackage.LoadPackageParam Param) throws Throwable {
        //message transmit
        findAndHookMethod("com.tencent.mobileqq.app.MessageHandlerUtils", Param.classLoader, "a",
                "com.tencent.mobileqq.app.QQAppInterface",
                "com.tencent.mobileqq.data.MessageRecord",
                Boolean.TYPE, new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        if (!PreferencesUtils.qqOpen()) return;
                        //hongbao message filter
                        int msgtype = (int) getObjectField(param.args[1], "msgtype");
                        if (msgtype == -2025) {
                            msgList.add(new Tuple(
                                    (long) getObjectField(param.args[1], "msgUid"),
                                    (int) getObjectField(param.args[1], "istroop"),
                                    String.valueOf(getObjectField(param.args[1], "senderuin")),
                                    String.valueOf(getObjectField(param.args[1], "frienduin")),
                                    String.valueOf(getObjectField(param.args[1], "selfuin"))
                            ));
                        }
                    }
                });

        //message process
        findAndHookMethod("com.tencent.mobileqq.data.MessageForQQWalletMsg", Param.classLoader, "doParse", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                //get msg list
                if(msgList.size()<=0)return;
                //get button index
                msgUid = msgList.get(0).msgUid;
                istroop =msgList.get(0).istroop;
                senderuin = msgList.get(0).senderuin;
                frienduin = msgList.get(0).frienduin;
                selfuin = msgList.get(0).selfuin;
                //remove bottom
                msgList.remove(0);

                //switcher
                if (!PreferencesUtils.qqOpen() || msgUid == 0) return;
                //red packet message type
                int messageType = (int) getObjectField(param.thisObject, "messageType");
                //grab with word
                if (messageType == 6 && PreferencesUtils.qqWord()) return;
                //send by self
                if(senderuin.equals(selfuin)&& PreferencesUtils.qqSelf())return;
                //red packet message
                Object mQQWalletRedPacketMsg = getObjectField(param.thisObject, "mQQWalletRedPacketMsg");
                String name = String.valueOf(getObjectField(callMethod(FriendManager, "c", selfuin), "name"));
                String authkey = String.valueOf(getObjectField(mQQWalletRedPacketMsg, "authkey"));
                String redPacketId = String.valueOf(getObjectField(mQQWalletRedPacketMsg, "redPacketId"));
                String redChannel = String.valueOf(getObjectField(mQQWalletRedPacketMsg,"redChannel"));
                String skey = String.valueOf(callMethod(TicketManager, "getSkey", selfuin));
                ///
                StringBuilder url = new StringBuilder();
                url.append("&uin=" + selfuin);
                url.append("&listid=" + redPacketId);
                url.append("&name=" + Uri.encode(name));
                url.append("&answer=");
                url.append("&groupid=" + (istroop == 0 ? selfuin : frienduin));
                url.append("&grouptype=" + getGroupType(istroop,frienduin));
                url.append("&groupuin=" + getGroupUin(messageType,senderuin,frienduin));
                url.append("&channel=" + redChannel);
                url.append("&authkey=" + authkey);
                url.append("&agreement=0");

                //ramdom field
                int random = msgCount % 16;

                //get class loader and load class from qwallet_plugin.apk
                Class plugin = findClass("com.tencent.mobileqq.pluginsdk.PluginStatic", Param.classLoader);
                //package : /data/data/com.tencent.mobileqq/app_installed_plugin/qwallet_plugin.apk
                walletClassLoader = (ClassLoader) callStaticMethod(plugin, "getOrCreateClassLoader", ApplicationContext, "qwallet_plugin.apk");
                pluginClass = findClass(Version.WalletPluginClass, walletClassLoader);
                //request text des/md5 encryption
                String reqText = "";
                try {
                    reqText = callStaticMethod(pluginClass, "a", ApplicationContext, random, false, url.toString()) + url.toString();
                } catch (Throwable t) {
                    reqText = callStaticMethod(pluginClass, "g", ApplicationContext) + url.toString();
                    reqText = des.getEncText(random, reqText);
                }
                //clear StringBuilder
                url.delete(0, url.length());
                url.append("https://mqq.tenpay.com/cgi-bin/hongbao/qpay_hb_na_grap.cgi?ver=2.0&chv=3");
                url.append("&req_text=" + reqText);
                url.append("&random=" + random);
                url.append("&skey_type=2");
                url.append("&skey=" + skey);
                url.append("&msgno=" + getMsgNo(selfuin));
                //send request
                Object d = newInstance(findClass("com.tenpay.android.qqplugin.b.d", walletClassLoader));
                callMethod(d, "a", url.toString());
            }
        });


        //get application context
        findAndHookMethod("com.tencent.mobileqq.activity.SplashActivity", Param.classLoader, "doOnCreate", Bundle.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                ApplicationContext = ((Context) param.thisObject).getApplicationContext();
            }
        });
        //get ticket manager
        findAndHookConstructor("mqq.app.TicketManagerImpl", Param.classLoader, "mqq.app.AppRuntime", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                TicketManager = param.thisObject;
            }
        });
        //get hot chat manager
        findAndHookConstructor("com.tencent.mobileqq.app.HotChatManager", Param.classLoader, "com.tencent.mobileqq.app.QQAppInterface", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                HotChatManager = param.thisObject;
            }
        });
        //get troop manager
        findAndHookConstructor("com.tencent.mobileqq.app.TroopManager", Param.classLoader, "com.tencent.mobileqq.app.QQAppInterface", new XC_MethodHook() {
            protected void afterHookedMethod(MethodHookParam methodHookParam) {
                TroopManager = methodHookParam.thisObject;
            }
        });
        //get friend manager
        findAndHookConstructor("com.tencent.mobileqq.app.FriendsManager", Param.classLoader, "com.tencent.mobileqq.app.QQAppInterface", new XC_MethodHook() {
            protected void afterHookedMethod(MethodHookParam methodHookParam) {
                FriendManager = methodHookParam.thisObject;
            }
        });
    }


    private static int getGroupType(int isTroop, String friendUin) throws IllegalAccessException {
        switch (isTroop) {
            case 0:return 0;
            case 3000:return 2;
            case 1000:return 3;
            case 1004:return 4;
            case 1001:return 6;
            case 1:
                if(HotChatManager!=null) {
                    Map map = (Map) findFirstFieldByExactType(HotChatManager.getClass(), Map.class).get(HotChatManager);
                    if (map != null & map.containsKey(friendUin)) {return 5;}
                }
                return 1;
            //default
            default:return 0;
        }
    }

    private static String getGroupUin(int messageType, String senderUin, String friendUin) {
        switch (messageType){
            case 1:{
                Object troopInfo = callMethod(TroopManager, "a", friendUin);
                return (String)getObjectField(troopInfo,"troopcode");
            }
            case 5:{
                Object hotChatInfo = callMethod(TroopManager, "a", friendUin);
                return (String)getObjectField(hotChatInfo,"troopCode");
            }
            default:return senderUin;
        }
    }

    private static String getMsgNo(String msg) {
        String format = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(msg);
        stringBuilder.append(format);
        int count = msgCount++;
        String value = String.valueOf(count);
        int length = (28 - stringBuilder.length()) - value.length();
        for (count = 0; count < length; count++) {stringBuilder.append("0");}
        stringBuilder.append(value);
        return stringBuilder.toString();
    }

}