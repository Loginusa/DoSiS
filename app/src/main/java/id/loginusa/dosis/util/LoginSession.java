package id.loginusa.dosis.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import com.google.gson.JsonObject;

import org.json.JSONException;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import id.loginusa.dosis.LoginActivity;
import id.loginusa.dosis.R;
import id.loginusa.dosis.backgroundprocess.intentservices.dataservice.DataServiceIntentService;
import id.loginusa.dosis.backgroundprocess.services.LogoutService;
import id.loginusa.dosis.util.externalconnection.openbravows.OpenbravoConnection;
import id.loginusa.dosis.util.externalconnection.openbravows.OpenbravoLoginService;
import id.loginusa.dosis.util.json.JsonBuilder;
import id.loginusa.dosis.util.json.entity.UserData;

/**
 * Created by mfachmirizal on 5/3/16.
 */



public class LoginSession extends SessionManager{
    // User Info
    //Session static baru
    private static final String USER_IS_LOGIN = "UserIsLoggedIn";
    public static final String USER_DATA = "userData";
    private static final String className="LoginSession";

    public LoginSession(Context context) {
        super(context);
    }


    /**
     * Create Login Session
     *
     */
    //public void createLoginSession(String username,String name,String email, String profpic){
    public JsonObject setLoginSession(JsonObject data){
        try {
            editor.putString(USER_DATA,data.toString());
            // commit changes
            editor.commit();

            editor.putBoolean(USER_IS_LOGIN, isLoggedIn());
            //re commit for set is_login
            editor.commit();
        } catch (Exception e) {
            Logging.toast(_context,"Error : "+e.getMessage(),1);
        }
        return data;
    }

    /**
     * get raw UserData in Session
     * @return JSON User Data
     */
    public String getUserData() {
        UserData user = new UserData();
        return pref.getString(USER_DATA, JsonBuilder.toJson(user,0).toString());
    }

    /**
     * check this user login status, if not login, the redirect to login page
     */
    public void checkLogin(){
        // Check login status
        if(!this.isLoggedIn()){
            // user is not logged in redirect him to Login Activity
            Intent i = new Intent(_context, LoginActivity.class);
            // Closing all the Activities
            //i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

            // Add new Flag to start new Activity
            //i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            // Staring Login Activity
            _context.startActivity(i);
        }

    }

    /**
     * Clear session details
     * */
    public void logoutUser(boolean forceLogout){
        //kirim laporan ke server bahwa user ini logoff TODO:kasih delay task nanti, bahwa bila koneksi off / tidak terkoneksi proses ini ngedelay
        if (!forceLogout) {
            UserData ud = getUserDetails();
            //jalankan service untuk kirim pesan logout ke server
            //SessionIntentService.startLogoutIntentService(_context, ud.getUsername());
            LogoutService.startLogoutService(_context, ud.getUsername());
        }

        // Clearing all data from Shared Preferences, excepts GCM session
        boolean issent = pref.getBoolean(SENT_TOKEN_TO_SERVER, false);
        String token = pref.getString(CURRENT_TOKEN, "");
        String instanceid = pref.getString(CURRENT_INSTANCE_ID, "");
        String pendinglogoutuser = pref.getString(PENDING_LOGOUT_USER, "");

        editor.clear();
        editor.commit();
        //reset session token gcm
        createGCMTokenSentSession(issent,token,instanceid);
        createPendingUserLogout(pendinglogoutuser);

        ((Activity)_context).finish();
        ((Activity)_context).startActivity(((Activity)_context).getIntent());
    }

    /**
     * Quick check for login
     * **/
    // Get Login State
    public boolean isLoggedIn(){
        UserData user = getUserDetails();
        try {
            return user.isIsLogin();
        } catch (Exception e) {
            Logging.toast(_context,"Error : "+e.getMessage(),1);
        }
        return user.isIsLogin();
    }


    /**
     * Get stored session data
     * */
/*    @Deprecated
    public HashMap<String, String> getUserDetails(){
        HashMap<String, String> user = new HashMap<String, String>();
//        user.put(USER_USERNAME, pref.getString(USER_USERNAME, ""));
//        user.put(USER_NAME, pref.getString(USER_NAME, ""));
//        user.put(USER_EMAIL, pref.getString(USER_EMAIL, ""));
//        user.put(USER_PROFPIC, pref.getString(USER_PROFPIC, ""));

        return user;
    }*/

    /**
     * Get stored session data
     * */

    public UserData getUserDetails(){
        UserData user = new UserData();
        try {
            List<UserData> userList = UserData.getList(pref.getString(USER_DATA, JsonBuilder.toJson(user,0).toString()));
            if (userList != null) {
                user = userList.get(0);
            }
        } catch (Exception e) {
            Logging.toast(_context,"Error : "+e.getMessage(),1);
        }
        return user;
    }


    //Method yang berkenaan pemutakhiran UserData
    public void updateAccInfo(JsonObject newJsonUserData) throws Exception{
        setLoginSession(newJsonUserData);
//            UserData prevUserData = getUserDetails();
//            UserData newUserData = UserData.getList(newJsonUserData).get(0);
//            prevUserData = newUserData;

    }

    /**
     * sama seperti @link#updateAccInfo(JsonObject newJsonUserData) hanya ini versi static, yang dapat di panggil langsung
     * @param userData new UserData
     * @param ctx Context application
     */
    static public void updateAccInfo(JsonObject userData,Context ctx) {
        new LoginSession(ctx).setLoginSession(userData);
    }

    public void updateServerAccInfo(JsonObject newJsonUserData) throws Exception{
        setLoginSession(newJsonUserData);
        DataServiceIntentService.startUpdateServerAccInfoIntentService(_context,newJsonUserData);
    }

    public void setNewRevisionDate(Date newDate) {
        UserData user = getUserDetails();
        user.setRevision_date(newDate);
        setLoginSession(JsonBuilder.toJson(user,1));
    }

    public boolean refreshAccountInfo() throws Exception{
        UserData user = getUserDetails();
        boolean userLoggedin = user.isIsLogin();
        if (userLoggedin) {
            DataServiceIntentService.startCheckServerAccConsistency(_context, JsonBuilder.toJson(user, 1), getCurrentToken());
        }
        return userLoggedin;
    }
}
