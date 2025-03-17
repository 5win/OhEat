package com.etcCodes;

import java.io.BufferedReader;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class OneHopBfs {

    private static final String url = "jdbc:mysql://localhost:3306/{DB_NAME}";
    private static final String user = "root";
    private static final String password = "password";

    private static final int MAX_NODE = 250;
    private static List<Integer> adj[] = new ArrayList[MAX_NODE + 1];

    public static void main(String[] args) throws Exception {

        for (int i = 0; i <= MAX_NODE; i++) {
            adj[i] = new ArrayList<>();
        }

        BufferedReader br = new BufferedReader(new FileReader("sigungu_edges.txt"));

        String line;
        while ((line = br.readLine()) != null) {
            String[] nodes = line.split("\\s+");
            if (nodes.length != 2) {
                continue;
            }

            int src = Integer.parseInt(nodes[0]);
            int dst = Integer.parseInt(nodes[1]);

            adj[src].add(dst);
            adj[dst].add(src);
        }

        byte[][] data = new byte[MAX_NODE + 1][32];
        for (int src = 1; src <= MAX_NODE; src++) {
            int[] dist = bfs(src, 1);       // 1-hop 이내의 시군구 계산
            data[src] = makeBytes(dist);
        }

        updateData(data);
    }

    private static int[] bfs(int src, int maxHop) {
        Queue<Integer> q = new LinkedList<>();
        int[] dist = new int[MAX_NODE + 1];
        Arrays.fill(dist, -1);

        q.offer(src);
        dist[src] = 0;

        while (!q.isEmpty()) {
            int here = q.poll();

            for (int next : adj[here]) {
                if (dist[next] != -1 || dist[here] >= maxHop) {
                    continue;
                }
                dist[next] = dist[here] + 1;
                q.offer(next);
            }
        }

        return dist;
    }

    /**
     * 해당 번호가 인접한 시군구이면 1, 아니면 0을 나타내는 bit-sequence 생성 후 byte로 변환
     */
    private static byte[] makeBytes(int[] dist) {
        byte[] data = new byte[32];

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i <= MAX_NODE; i++) {
            if (i != 0 && i % 8 == 0) {
                data[i / 8 - 1] = (byte) Integer.parseInt(sb.toString(), 2);
                sb.setLength(0);
            }
            sb.append(dist[i] == -1 ? '0' : '1');
        }
        return data;
    }

    /**
     * bit-sequence byte 데이터들을 DB에 저장
     */
    private static void updateData(byte[][] data) {
        String query = "update sigungu set adj = ? where ogr_fid = ?";

        try {
            Connection conn = DriverManager.getConnection(url, user, password);
            PreparedStatement stmt = conn.prepareStatement(query);

            for (int ogrFid = 1; ogrFid <= MAX_NODE; ogrFid++) {
                stmt.setBytes(1, data[ogrFid]);
                stmt.setInt(2, ogrFid);
                stmt.executeUpdate();
                System.out.println(ogrFid + "번 시군구 BINARY 데이터 업데이트 성공.");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
