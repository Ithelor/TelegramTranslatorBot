package sample;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

// TODO: total mess - clean up
public class DatabaseHandler {

    private static final String URL = "jdbc:mysql://localhost:3307/numerius_db";
    private static final String USER = "root";
    private static final String PASSWORD = "root";

    private static Connection con;
    private static Statement stmt;
    private static ResultSet rs;

    public static String getValueByColumn(String query, String columnIndex, Long chatId)
    {

        String id = null;

        try {

            con = DriverManager.getConnection(URL, USER, PASSWORD);
            stmt = con.createStatement();
            rs = stmt.executeQuery(query);
            while (rs.next())
                id = rs.getString(Integer.parseInt(columnIndex));

        } catch (SQLException sqlEx) {
            sqlEx.printStackTrace();
        } finally {
            try { con.close(); } catch (SQLException throwable) { throwable.printStackTrace(); }
            try { stmt.close(); } catch (SQLException throwable) { throwable.printStackTrace();}
            try { rs.close(); } catch (SQLException throwable) { throwable.printStackTrace(); }
        }

        return id;
    }

    public static String executeQuery(String query, String columnIndex, Boolean isRs)
    {
        String id = "0";

        try {

            con = DriverManager.getConnection(URL, USER, PASSWORD);
            stmt = con.createStatement();

            if (!isRs) {
                stmt.execute(query);
            }
            else {
                rs = stmt.executeQuery(query);
                if (!String.valueOf(columnIndex).isEmpty())
                    while (rs.next())
                        id = rs.getString(Integer.parseInt(columnIndex));
            }

        } catch (SQLException sqlEx) {
            sqlEx.printStackTrace();
        } finally {
            try { con.close(); } catch (SQLException throwable) { throwable.printStackTrace(); }
            try { stmt.close(); } catch (SQLException throwable) { throwable.printStackTrace();}
            if (isRs)
                try { rs.close(); } catch (SQLException throwable) { throwable.printStackTrace(); }
        }

        return id;
    }

    public static void handleSTL(String call_data, Long chatId)
    {

        String query = "SELECT * FROM chats WHERE chat_id = " + chatId + ";";

        if (executeQuery(query, "1", true).equals("0"))
        {
            query = "INSERT INTO chats (chat_id, last_target_language_code)" +
                    " VALUES (" + chatId + ", \"" + Bot.getCodeByName(call_data) + "\");";
            System.out.println("handleSTL: not found, executing: " + query);
        }
        else
        {
            query = "UPDATE chats " +
                    "SET last_target_language_code = \"" + Bot.getCodeByName(call_data) +
                    "\" WHERE chat_id = " + chatId + ";";
            System.out.println("handleSTL: found, executing: " + query);
        }

        executeQuery(query, null, false);
    }

    public static void handleSpecify(String translation_mode, String latest_specified_language_code, Long chatId)
    {

        String query = "SELECT * FROM chats WHERE chat_id = " + chatId + ";";

        if (latest_specified_language_code == null) {

            handleMode(translation_mode, chatId);
        }
        else {

            if (executeQuery(query, "1", true).equals("0")) {
                query = "INSERT INTO chats (chat_id, latest_specified_language_code)" +
                        " VALUES (" + chatId + ", \"" + latest_specified_language_code + "\");";
                handleMode(translation_mode, chatId);
                System.out.println("handleSpecify: not found, executing: " + query);
            } else {
                query = "UPDATE chats " +
                        "SET latest_specified_language_code = \"" + latest_specified_language_code +
                        "\" WHERE chat_id = " + chatId + ";";
                handleMode(translation_mode, chatId);
                System.out.println("handleSpecify: found, executing: " + query);
            }
        }

        executeQuery(query, null, false);
    }

    public static void handleMode(String translation_mode, Long chatId)
    {

        String query = "SELECT * FROM chats WHERE chat_id = " + chatId + ";";

        if (executeQuery(query, "1", true).equals("0"))
        {
            query = "INSERT INTO chats (chat_id, translation_mode)" +
                    " VALUES (" + chatId + ", \"" + translation_mode + "\");";
            System.out.println("handleMode: not found, executing: " + query);
        }
        else
        {
            query = "UPDATE chats " +
                    "SET translation_mode = \"" + translation_mode +
                    "\" WHERE chat_id = " + chatId + ";";
            System.out.println("handleMode: found, executing: " + query);
        }

        executeQuery(query, null, false);
    }

    public static String getCurrentTargetLanguage(Long chatId)
    {

        String query = "SELECT last_target_language_code FROM chats WHERE chat_id = " + chatId + ";";
        return executeQuery(query, "1", true);
    }

}
