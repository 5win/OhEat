package com.etcCodes;

import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class SigunguGraphDataGenerator {

    private static final String URL = "jdbc:mysql://localhost:3306/{DB_NAME}";
    private static final String USER = "root";
    private static final String PASSWORD = "password";

    public static void main(String[] args) {
        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD)) {
            System.out.println("MySQL 연결 성공!");
            Statement stmt = connection.createStatement();
            for (int ogr_fid = 1; ogr_fid <= 250; ogr_fid++) {
                calcAdjSigungu(ogr_fid, stmt);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * 특정 시군구의 인접 시군구를 계산하여 파일에 저장하는 함수
     */
    private static void calcAdjSigungu(int ogr_fid, Statement stmt) {
        System.out.println("시군구 " + ogr_fid + "의 인접 시군구 계산 중...");

        String query = "SELECT a.ogr_fid AS source, b.ogr_fid AS target " +
            "FROM sigungu a " +
            "JOIN sigungu b ON ST_Intersects(a.geometry, b.geometry) " +
            "WHERE a.ogr_fid = " + ogr_fid + " AND a.ogr_fid != b.ogr_fid";

        try (ResultSet rs = stmt.executeQuery(query);
            FileWriter writer = new FileWriter("sigungu_edges.txt", true)) {
            while (rs.next()) {
                int source = rs.getInt("source");
                int target = rs.getInt("target");
                writer.write(source + " " + target + "\n");
            }
            System.out.println(ogr_fid + " 시군구의 엣지 데이터 저장 완료\n");
        } catch (SQLException | IOException e) {
            e.printStackTrace();
        }
    }
}
