package tk.qcute.wegrab;


import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.ResolveInfo;
import android.os.Binder;

import java.util.Iterator;
import java.util.List;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;


public class ModuleHide {

    public static void hookPackage(final XC_LoadPackage.LoadPackageParam Param) throws Throwable {

        newHide(Param, "tencent");

    }
    //new hide mode
    /**
     * @param Param
     * @param callingName  : not include calling name
     * */
    private static void newHide(XC_LoadPackage.LoadPackageParam Param, final String callingName) {
        //find the package manager service
        Class<?> service = XposedHelpers.findClass("com.android.server.pm.PackageManagerService", Param.classLoader);
        XposedBridge.hookAllMethods(service, "getInstalledApplications", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                //compare calling name
                String name = (String) XposedHelpers.callMethod(param.thisObject, "getNameForUid", Binder.getCallingUid());
                //not include xposed call
                if(!name.contains(callingName))return;
                // this object is android.content.pm.ParceledListSlice
                Object object = param.getResult();
                //convert to application info
                List<ApplicationInfo> list = (List<ApplicationInfo>) XposedHelpers.getObjectField(object, "mList");
                Iterator<ApplicationInfo> iterator = list.iterator();
                //remove query package from ApplicationInfo
                while (iterator.hasNext()) {
                    String queryName = iterator.next().packageName;
                    if (isQueryThis(queryName)) {
                        xLog("ModuleHide Package(getInstalledApplications): " + queryName + "   Calling by: " + name);
                        iterator.remove();
                    }
                }
                XposedHelpers.setObjectField(object, "mList", list);
            }
        });
        XposedBridge.hookAllMethods(service, "getInstalledPackages", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                //compare calling name
                String name = (String) XposedHelpers.callMethod(param.thisObject, "getNameForUid", Binder.getCallingUid());
                //not include xposed call
                if(!name.contains(callingName))return;
                // this object is android.content.pm.ParceledListSlice
                Object object = param.getResult();
                //convert to application info
                List<PackageInfo> list = (List<PackageInfo>) XposedHelpers.getObjectField(object, "mList");
                Iterator<PackageInfo> iterator = list.iterator();
                //remove query package from PackageInfo
                while (iterator.hasNext()) {
                    String queryName = iterator.next().packageName;
                    if (isQueryThis(queryName)) {
                        xLog("ModuleHide Package(getInstalledPackages): " + queryName + "   Calling by: " + name);
                        iterator.remove();
                    }
                }
                //set result
                XposedHelpers.setObjectField(object, "mList", list);
            }
        });
        XposedBridge.hookAllMethods(service, "queryIntentActivities", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                //compare calling name
                String name = (String) XposedHelpers.callMethod(param.thisObject, "getNameForUid", Binder.getCallingUid());
                //not include xposed call
                if(!name.contains(callingName))return;
                // android.content.pm.ParceledListSlice
                List<ResolveInfo> list = (List) param.getResult();
                Iterator<ResolveInfo> iterator = list.iterator();
                //remove query package from ResolveInfo
                while (iterator.hasNext()) {
                    String queryName = iterator.next().activityInfo.packageName;
                    if (isQueryThis(queryName)) {
                        xLog("ModuleHide Package(queryIntentActivities): " + queryName + "   Calling by: " + name);
                        iterator.remove();
                    }
                }
                //set result
                param.setResult(list);
            }
        });
        XposedBridge.hookAllMethods(service, "queryIntentActivityOptions", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                //compare calling name
                String name = (String) XposedHelpers.callMethod(param.thisObject, "getNameForUid", Binder.getCallingUid());
                //not include xposed call
                if(!name.contains(callingName))return;
                // android.content.pm.ParceledListSlice
                List<ResolveInfo> list = (List) param.getResult();
                Iterator<ResolveInfo> iterator = list.iterator();
                //remove query package from ResolveInfo
                while (iterator.hasNext()) {
                    String queryName = iterator.next().activityInfo.packageName;
                    if (isQueryThis(queryName)) {
                        xLog("ModuleHide Package(queryIntentActivityOptions): " + queryName + "   Calling by: " + name);
                        iterator.remove();
                    }
                }
                //set result
                param.setResult(list);
            }
        });
    }

    //query name
    private static boolean isQueryThis(String name) {
        return name.contains("qcute") || name.contains("xposed");
    }

    //log control
    private static void xLog(String msg){
        //if(PreferencesUtils.debug())
        //XposedBridge.log(msg);
    }
}
