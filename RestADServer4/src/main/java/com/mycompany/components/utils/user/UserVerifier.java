package com.mycompany.components.utils.user;

import com.mycompany.components.sql.DbAgent;

import java.sql.SQLException;

public class UserVerifier {

    private final DbAgent agent = new DbAgent();
    private final PasswordHasher hasher = new PasswordHasher();

    public boolean isAuthorized(String username, String password) throws SQLException {
        String hashedPassword = hasher.generatePasswordHash(password);
        return agent.getUser(username, hashedPassword) != null;
    }

}
