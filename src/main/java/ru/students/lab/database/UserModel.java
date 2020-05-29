package ru.students.lab.database;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserModel {
    public static final String DEFAULT_USERNAME = "default";
    public static final String ROOT_USERNAME = "root";
    public static final String WRONG_CREDENTIALS_CODE = "wrong_credentials";
    private final Connection connection;

    public UserModel(Connection connection) {
        this.connection = connection;
    }

    public int checkUserAndGetID(Credentials credentials) throws SQLException, NoSuchAlgorithmException {
        if (credentials == null || credentials.username.equals(DEFAULT_USERNAME))
            return -1;

        PreparedStatement preparedStatement = connection.prepareStatement(SQLQuery.Get.PASS_USING_USERNAME);
        preparedStatement.setString(1, credentials.username);
        ResultSet rs = preparedStatement.executeQuery();
        if (rs.next()) {
            if (hashPassword(credentials.password).equals(rs.getString(1)))
                return rs.getInt(2);
            else
                return -1;
        }

        return -1;
    }


    public int registerUser(Credentials credentials) throws SQLException, NoSuchAlgorithmException {
        //final ReentrantLock mainLock = this.mainLock;
        //mainLock.lock();
        //try {
            final boolean oldAutoCommit = connection.getAutoCommit();
            try {
                connection.setAutoCommit(false);
                PreparedStatement preparedStatement = connection.prepareStatement(SQLQuery.Add.USER);
                int pointer = 0;
                preparedStatement.setString(++pointer, credentials.username);
                preparedStatement.setString(++pointer, hashPassword(credentials.password));
                preparedStatement.executeUpdate();

                preparedStatement = connection.prepareStatement(SQLQuery.Get.ID_USING_USERNAME);
                pointer = 0;
                preparedStatement.setString(++pointer, credentials.username);
                ResultSet rs = preparedStatement.executeQuery();
                if (rs.next())
                    return rs.getInt(1);

                connection.commit();
            } catch (Throwable e) {
                connection.rollback();
                throw e;
            } finally {
                connection.setAutoCommit(oldAutoCommit);
            }
            return -1;

        /*} finally {
            mainLock.unlock();
        }*/
    }


    public String hashPassword(String password) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        return toHexString(md.digest(password.getBytes(StandardCharsets.UTF_8)));
    }
    public String toHexString(byte[] hash) {
        BigInteger number = new BigInteger(1, hash);
        StringBuilder hexString = new StringBuilder(number.toString(16));

        while (hexString.length() < 32)
            hexString.insert(0, '0');

        return hexString.toString();
    }
}