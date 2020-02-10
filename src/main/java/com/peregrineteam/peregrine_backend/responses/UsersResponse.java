package com.peregrineteam.peregrine_backend.responses;

import java.util.ArrayList;
import java.util.UUID;

public class UsersResponse {
    public ArrayList<User> users;

    public static class User {
        public long id;
        public UUID uuid;
        public String name;
        public String email;
        public Boolean email_verified;
        public String phone;
        public String access;
        public String country_id;
        public ArrayList<Location> locations;
    }

    public static class Location {
        public long id;
        public String address;
        public String floor;
        public String extra_info;
        public Long zip_code;
        public String country_id;
        public long user_id;
        public boolean is_default;
    }
}

