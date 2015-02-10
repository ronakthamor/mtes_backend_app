package com.remitbee.mtes_backend_app.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Thamor on 2014-12-10.
 */
public class User implements Parcelable{

    private int id;
    private String cus_username;
    private String cus_email;
    private int company_id;
    private String cus_registration_type;

    // Used in profile:
    private String cus_firstname;
    private String cus_lastname;
    private String cus_phone1;
    private String cus_phone2;

    private String cus_address1;
    private String cus_address2;
    private String cus_city;
    private String cus_province;
    private String cus_country;
    private String cus_postal;

    private String cus_id_1;
    private String cus_id_2;
    private String cus_id_3;

    private String cus_unique_id;

    public User(String cus_firstname, String cus_lastname, String cus_email, String cus_phone1, String cus_phone2,
                String cus_address1, String cus_address2, String cus_city, String cus_province, String cus_country, String cus_postal,
                String cus_id_1, String cus_id_2, String cus_id_3, String cus_unique_id){
        this.cus_firstname = cus_firstname;
        this.cus_lastname = cus_lastname;
        this.cus_email = cus_email;
        this.cus_phone1 = cus_phone1;
        this.cus_phone2 = cus_phone2;

        this.cus_address1 = cus_address1;
        this.cus_address2 = cus_address2;
        this.cus_city = cus_city;
        this.cus_province = cus_province;
        this.cus_country = cus_country;
        this.cus_postal = cus_postal;

        this.cus_id_1 = cus_id_1;
        this.cus_id_2 = cus_id_2;
        this.cus_id_3 = cus_id_3;

        this.cus_unique_id = cus_unique_id;
    }





    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCus_username() {
        return cus_username;
    }

    public void setCus_username(String cus_username) {
        this.cus_username = cus_username;
    }

    public int getCompany_id() {
        return company_id;
    }

    public void setCompany_id(int company_id) {
        this.company_id = company_id;
    }

    public String getCus_email() {
        return cus_email;
    }

    public void setCus_email(String cus_email) {
        this.cus_email = cus_email;
    }

    public String getCus_registration_type() {
        return cus_registration_type;
    }

    public void setCus_registration_type(String cus_registration_type) {
        this.cus_registration_type = cus_registration_type;
    }



    // Used in profile view:
    public String getCus_firstname() {
        return cus_firstname;
    }

    public void setCus_firstname(String cus_firstname) {
        this.cus_firstname = cus_firstname;
    }

    public String getCus_lastname() {
        return cus_lastname;
    }

    public void setCus_lastname(String cus_lastname) {
        this.cus_lastname = cus_lastname;
    }

    public String getCus_phone1() {
        return cus_phone1;
    }

    public void setCus_phone1(String cus_phone1) {
        this.cus_phone1 = cus_phone1;
    }

    public String getCus_phone2() {
        return cus_phone2;
    }

    public void setCus_phone2(String cus_phone2) {
        this.cus_phone2 = cus_phone2;
    }

    public String getCus_address1() {
        return cus_address1;
    }

    public void setCus_address1(String cus_address1) {
        this.cus_address1 = cus_address1;
    }

    public String getCus_address2() {
        return cus_address2;
    }

    public void setCus_address2(String cus_address2) {
        this.cus_address2 = cus_address2;
    }

    public String getCus_city() {
        return cus_city;
    }

    public void setCus_city(String cus_city) {
        this.cus_city = cus_city;
    }

    public String getCus_province() {
        return cus_province;
    }

    public void setCus_province(String cus_province) {
        this.cus_province = cus_province;
    }

    public String getCus_country() {
        return cus_country;
    }

    public void setCus_country(String cus_country) {
        this.cus_country = cus_country;
    }

    public String getCus_postal() {
        return cus_postal;
    }

    public void setCus_postal(String cus_postal) {
        this.cus_postal = cus_postal;
    }

    // IDs:
    public String getCus_id_1() {
        return cus_id_1;
    }

    public void setCus_id_1(String cus_id_1) {
        this.cus_id_1 = cus_id_1;
    }

    public String getCus_id_3() {
        return cus_id_3;
    }

    public void setCus_id_3(String cus_id_3) {
        this.cus_id_3 = cus_id_3;
    }

    public String getCus_id_2() {
        return cus_id_2;
    }

    public void setCus_id_2(String cus_id_2) {
        this.cus_id_2 = cus_id_2;
    }


    public String getCus_unique_id() {
        return cus_unique_id;
    }

    public void setCus_unique_id(String cus_unique_id) {
        this.cus_unique_id = cus_unique_id;
    }

    // Parcelling part
    public User(Parcel in){
        String[] data = new String[15];

        in.readStringArray(data);
        this.cus_firstname = data[0];
        this.cus_lastname = data[1];
        this.cus_email = data[2];
        this.cus_phone1 = data[3];
        this.cus_phone2 = data[4];

        this.cus_address1 = data[5];
        this.cus_address2 = data[6];
        this.cus_city = data[7];
        this.cus_province = data[8];
        this.cus_country = data[9];
        this.cus_postal = data[10];

        this.cus_id_1 = data[11];
        this.cus_id_2 = data[12];
        this.cus_id_3 = data[13];

        this.cus_unique_id = data[14];
    }

    public int describeContents(){
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeStringArray(new String[] {this.cus_firstname,
                this.cus_lastname,
                this.cus_email,
                this.cus_phone1,
                this.cus_phone2,

                this.cus_address1,
                this.cus_address2,
                this.cus_city,
                this.cus_province,
                this.cus_country,
                this.cus_postal,

                this.cus_id_1,
                this.cus_id_2,
                this.cus_id_3,

                this.cus_unique_id
        });
    }
    public static final Creator CREATOR = new Creator() {
        public User createFromParcel(Parcel in) {
            return new User(in);
        }

        public User[] newArray(int size) {
            return new User[size];
        }
    };

}
