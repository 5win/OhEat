package com.etcCodes;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SelectAdjSigungu {

    private static final String url = "jdbc:mysql://localhost:3306/{DB_NAME}";
    private static final String user = "root";
    private static final String password = "password";

    public static void main(String[] args) {

        String query = "select adj from sigungu where ogr_fid = ?";

        try {
            Connection conn = DriverManager.getConnection(url, user, password);
            PreparedStatement stmt = conn.prepareStatement(query);

            stmt.setInt(1, 1);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                byte[] adj = rs.getBytes("adj");
                System.out.println(bytesToBinaryString(adj));
                System.out.println(adj[0] & (1 << 7));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static String bytesToBinaryString(byte[] data) {
        StringBuilder binaryString = new StringBuilder();
        for (byte i : data) {
            binaryString.append(String.format("%8s", Integer.toBinaryString(i & 0xFF)).replace(' ', '0')).append(" ");
        }
        return binaryString.toString().trim();
    }
}
