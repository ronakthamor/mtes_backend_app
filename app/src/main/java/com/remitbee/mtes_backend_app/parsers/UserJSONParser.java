package com.remitbee.mtes_backend_app.parsers;

import com.remitbee.mtes_backend_app.model.User;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Thamor on 2015-02-10.
 */
public class UserJSONParser {
    public static User parseUserProfileFeed(String content) {
        User user = new User("","","","","","","","","","","","","","","");
        try {
            JSONObject obj = new JSONObject(content); // This will give you response.
            JSONObject obj2 = new JSONObject(obj.getString("data"));
            JSONObject obj3 = new JSONObject(obj2.getString("cus_detail"));
            JSONObject obj4 = new JSONObject(obj2.getString("cus_ids"));

            user = new User(obj3.getString("cus_firstname"), obj3.getString("cus_lastname"), obj3.getString("cus_email"), obj3.getString("cus_phone1"), obj3.getString("cus_phone2"),
                    obj3.getString("cus_address1"), obj3.getString("cus_address2"), obj3.getString("cus_city"), obj3.getString("cus_province"), obj3.getString("cus_country"), obj3.getString("cus_postal"),
                    obj4.getString("id_1"), obj4.getString("id_2"), obj4.getString("id_3"), obj3.getString("cus_unique_id"));
            return user;
        }
        catch(JSONException e){
            e.printStackTrace();
            return user;
        }
    }
}
