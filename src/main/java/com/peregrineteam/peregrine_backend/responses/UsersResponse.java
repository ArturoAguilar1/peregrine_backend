package com.peregrineteam.peregrine_backend.responses;

import java.util.ArrayList;

public class UsersResponse {
    public ArrayList<User> users;

    public static class User {
        public String id;
        public String uuid;
        public String name;
        public String email;
        public Boolean email_verified;
        public String phone;
        public String access;
        public String country_id;
        public ArrayList<Address> addresses;
        public ArrayList<Picture> pictures;
    }

    public static class Address {
        public String id;
        public String address;
        public String floor;
        public String extra_info;
        public String zip_code;
        public String country_id;
        public boolean is_default;
    }

    public static class Picture {
        public String id;
        public String src;
    }
}

