package tk.qcute.wegrab;


import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;


public class XposedEntry implements IXposedHookLoadPackage, IXposedHookZygoteInit  {
    @Override
    public void initZygote(IXposedHookZygoteInit.StartupParam startupParam) throws Throwable {
        PreferencesUtils.loadPreferencesOnZygote();
    }

    @Override
    public void handleLoadPackage(final XC_LoadPackage.LoadPackageParam Param) throws Throwable {

        if (Param.packageName.equals("com.tencent.mm")) {
            Version.Initialization("com.tencent.mm");
            WeChat.hookPackage(Param);
        }

        if (Param.packageName.equals("com.tencent.mobileqq")) {
            Version.Initialization("com.tencent.mobileqq");
            QQ.hookPackage(Param);
        }

        if (Param.packageName.equals("android")){
            ModuleHide.hookPackage(Param);
        }

        if(Param.packageName.equals("tk.qcute.wegrab")) {
            findAndHookMethod("tk.qcute.wegrab.SettingsActivity",Param.classLoader, "isModuleActive", XC_MethodReplacement.returnConstant(true));
        }
    }
}