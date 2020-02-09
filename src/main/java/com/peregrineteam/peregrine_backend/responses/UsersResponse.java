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
    }
}

