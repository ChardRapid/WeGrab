package tk.qcute.wegrab;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.InputStreamReader;


public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction().replace(R.id.settings_container, new SettingsFragment()).commit();
        }
    }
    /**
     * A placeholder fragment containing a settings view.
     */
    public static class SettingsFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            getPreferenceManager().setSharedPreferencesMode(MODE_WORLD_READABLE);
            addPreferencesFromResource(R.xml.pref_settings);


            if(!isModuleActive())
                new AlertDialog.Builder(getActivity()).setTitle(R.string.attention).setMessage(R.string.message)
                        .setCancelable(false)
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                reBoot();
                            }
                        })
                        .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                getActivity().finish();
                            }
                        }).show();

            //Set WeChat Version Message
            try {
                String version=getActivity().getPackageManager().getPackageInfo("com.tencent.mm", 0).versionName;
                if(Version.isSupportWeChat(version)){
                    findPreference("wechat_version").setSummary(String.format(getResources().getString(R.string.supported), version));
                }
                else{
                    findPreference("wechat_version").setSummary(String.format(getResources().getString(R.string.unsupported), version));
                }
            }catch(Exception e){e.printStackTrace();}

            //Set QQ Version Message
            try {
                String version=getActivity().getPackageManager().getPackageInfo("com.tencent.mobileqq", 0).versionName;
                if(Version.isSupportQQ(version)){
                    findPreference("qq_version").setSummary(String.format(getResources().getString(R.string.supported), version));
                }
                else{
                    findPreference("qq_version").setSummary(String.format(getResources().getString(R.string.unsupported), version));
                }
            }catch(Exception e){e.printStackTrace();}

            //Set Preference Click Listener
            Preference tinker = findPreference("wechat_tinker");
            tinker.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    if(!checkSuperUser()){
                        Toast.makeText(getActivity(),R.string.root_message,Toast.LENGTH_LONG).show();
                        return true;
                    }
                    if(execute("rm -rf /data/data/com.tencent.mm/tinker/*"))
                        if(execute("chmod 000 /data/data/com.tencent.mm/tinker"))
                            Toast.makeText(getActivity(),R.string.tinker_message,Toast.LENGTH_LONG).show();
                    return true;
                }
            });
        }
    }

    //is active check
    private static boolean isModuleActive(){return false;}

    //execute command
    private static boolean reBoot(){
        try {
            ProcessBuilder processBuilder = new ProcessBuilder("reboot");
            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line = bufferedReader.readLine();
            if(line!=null){
                if(line.compareTo("Permission denied")==0){
                    bufferedReader.close();
                    return  false;
                }
            }
            process.waitFor();
        } catch (Exception e) {e.printStackTrace();}
        return  true;
    }


    //execute command
    private static boolean execute(String cmd){
        try {
            ProcessBuilder processBuilder = new ProcessBuilder("su","-c",cmd);
            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line = bufferedReader.readLine();
            if(line!=null){
                if(line.compareTo("Permission denied")==0){
                    bufferedReader.close();
                    return  false;
                }
            }
            process.waitFor();
        } catch (Exception e) {e.printStackTrace();}
        return  true;
    }

    //check root
    private static boolean checkSuperUser(){
        try {
            ProcessBuilder processBuilder = new ProcessBuilder("su","-c","ls  /data");
            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line = bufferedReader.readLine();
            if(line!=null){
                if(line.compareTo("Permission denied")==0){
                    bufferedReader.close();
                    return  false;
                }
            }
            else if(line==null)return false;
            process.waitFor();
        } catch (Exception e) {e.printStackTrace();}
        return  true;
    }
}
