package tk.qcute.wegrab;

import android.database.Cursor;
import android.net.Uri;

import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

import static de.robv.android.xposed.XposedHelpers.callMethod;
import static de.robv.android.xposed.XposedHelpers.callStaticMethod;
import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;
import static de.robv.android.xposed.XposedHelpers.findClass;
import static de.robv.android.xposed.XposedHelpers.getObjectField;
import static de.robv.android.xposed.XposedHelpers.newInstance;


public class WeChat {
    //ab param class
    private static Class<?>abClass;
    //network n
    private static Object nClass;
    //msg list
    private static ArrayList<Tuple>msgList = new ArrayList<>();
    private static class Tuple{
        private int msgType;
        private int channelId;
        private String sendId;
        private String nativeUrlString;
        private String talker;
        public Tuple(int msgType,int channelId, String sendId, String nativeUrlString, String talker){
            this.msgType = msgType;
            this.channelId = channelId;
            this.sendId = sendId;
            this.nativeUrlString = nativeUrlString;
            this.talker = talker;
        }
    }


    public static void hookPackage(final LoadPackageParam Param) throws Throwable {

        findAndHookMethod(Version.attachMessageClass, Param.classLoader, "a", int.class, String.class, JSONObject.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {

                if(!Version.hasTimingIdentifier)return;
                JSONObject json = (JSONObject)param.args[2];
                //handle json
                String newTimingIdentifier = json.optString("timingIdentifier");
                if(newTimingIdentifier==null)return;
                if(msgList.size()<=0)return;
                Object ab=newInstance(
                        abClass, msgList.get(0).msgType, msgList.get(0).channelId,
                        msgList.get(0).sendId, msgList.get(0).nativeUrlString, "", "\u0020", msgList.get(0).talker,
                        "v1.0",newTimingIdentifier);
                //get it
                callMethod(nClass, "a", ab, 0);
                //remove bottom
                msgList.remove(0);
            }});
        //hook wechat get message function
        findAndHookMethod(Version.messageClass, Param.classLoader, Version.messageMethod, Cursor.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                //open switch
                if (!PreferencesUtils.wechatOpen()) return;
                //lucky money message type
                int field_type = (int) getObjectField(param.thisObject, "field_type");
                if (field_type != 436207665 && field_type != 469762097) return;
                //msg status
                int field_status = (int) getObjectField(param.thisObject, "field_status");
                if (field_status == 4) return;
                //get talker type(is in chat room)
                String field_talker = (String)getObjectField(param.thisObject, "field_talker");
                //get message type (is send by self)
                int field_isSend = (int) getObjectField(param.thisObject, "field_isSend");
                if (PreferencesUtils.wechatSelf() && field_isSend != 0) return;
                //group talk
                if (!field_talker.endsWith("@chatroom") && field_isSend != 0) return;
                //get notification type(in chat room) mute or not
                if (field_talker.endsWith("@chatroom") && PreferencesUtils.wechatMute()) {
                    Object msgInfo = callStaticMethod(findClass("com.tencent.mm.storage.ak", Param.classLoader), "F", param.thisObject);
                    boolean mute = (boolean) callStaticMethod(findClass("com.tencent.mm.booter.notification.c", Param.classLoader), "a", field_talker, msgInfo, 3, false);
                    if (!mute) return;
                }
                //get url message
                String content = (String)getObjectField(param.thisObject, "field_content");
                String nativeUrlString = handleMessage(content, "nativeurl");
                Uri nativeUrl = Uri.parse(nativeUrlString);

                String MsgType = nativeUrl.getQueryParameter("msgtype");
                int msgType = MsgType==null ? 1 : Integer.parseInt(MsgType);

                String ChannelID = nativeUrl.getQueryParameter("channelid");
                int channelId = ChannelID==null ? 1 : Integer.parseInt(ChannelID);

                String sendId = nativeUrl.getQueryParameter("sendid");

                nClass = callStaticMethod(findClass(Version.networkClass, Param.classLoader), Version.networkFunction);
                abClass = findClass("com.tencent.mm.plugin.luckymoney.c.ab", Param.classLoader);
                if(Version.hasTimingIdentifier){
                    Class<?> aeClass = findClass("com.tencent.mm.plugin.luckymoney.c.ae", Param.classLoader);
                    callMethod(nClass, "a", newInstance(aeClass, channelId, sendId, nativeUrlString, 0, "v1.0"), 0);
                    msgList.add(new Tuple(msgType, channelId, sendId, nativeUrlString, field_talker));
                }else{
                    Object ab=newInstance(abClass, msgType, channelId, sendId, nativeUrlString, "", "\u0020", field_talker, "v1.0");
                    callMethod(nClass, "a", ab, 0);
                }
            }
        });
    }
    //prase xml to url
    private static String handleMessage(String msg, String node) throws XmlPullParserException, IOException {
        //nativeurl
        XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
        factory.setNamespaceAware(true);
        XmlPullParser parser = factory.newPullParser();
        StringReader input = new StringReader(msg.substring(msg.indexOf("<msg>")));
        parser.setInput(input);
        int type = parser.getEventType();
        String result = "";
        while (type != XmlPullParser.END_DOCUMENT) {
            if (type == XmlPullParser.START_TAG) {
                if (parser.getName().equals(node)) {
                    parser.nextToken();
                    result = parser.getText();
                    break;
                }
            }
            type = parser.next();
        }
        return result;
    }
}