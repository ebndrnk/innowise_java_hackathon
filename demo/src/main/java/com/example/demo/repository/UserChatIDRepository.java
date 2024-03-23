    package com.example.demo.repository;

    import com.example.demo.models.UserChatID;
    import org.springframework.stereotype.Repository;

    import java.sql.*;
    import java.util.HashSet;
    import java.util.Set;

    @Repository
    public class UserChatIDRepository{
        private static final String URL = "jdbc:postgresql://localhost:5431/postgres";
        private static final String USER = "postgres";
        private static final String PASSWORD = "postgres";


        public void addChatId(Long chatId) {
            String sql = "INSERT INTO user_chat_ids (chat_id) VALUES (?)";
            try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setLong(1, chatId);
                pstmt.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        public Set<Long> getChatIds() {
            Set<Long> chatIds = new HashSet<>();
            String sql = "SELECT chat_id FROM user_chat_ids";
            try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
                 PreparedStatement pstmt = conn.prepareStatement(sql);
                 ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    chatIds.add(rs.getLong("chat_id"));
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return chatIds;
        }


        public UserChatID findByChatId(Long chatId) {
            UserChatID userChatID = null;
            String sql = "SELECT * FROM user_chat_ids WHERE chat_id = ?";
            try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setLong(1, chatId);
                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) {
                    userChatID = new UserChatID();
                    userChatID.setChatId(rs.getLong("chat_id"));
                    userChatID.setChoice(rs.getInt("choice"));
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return userChatID;
        }

        public void save(UserChatID userChatID) {
            String sql = "INSERT INTO user_chat_ids (chat_id, choice) VALUES (?, ?) ON CONFLICT (chat_id) DO UPDATE SET choice = ?";
            try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setLong(1, userChatID.getChatId());
                pstmt.setInt(2, userChatID.getChoice());
                pstmt.setInt(3, userChatID.getChoice());
                pstmt.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }


        public boolean existById(Long chatId) {
            boolean exists = false;
            String sql = "SELECT EXISTS(SELECT 1 FROM user_chat_ids WHERE chat_id = ?)";
            try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setLong(1, chatId);
                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) {
                    exists = rs.getBoolean(1);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return exists;
        }
    }
