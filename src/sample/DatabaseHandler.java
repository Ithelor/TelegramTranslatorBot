package sample;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseHandler {


    private static final String URL = "jdbc:mysql://localhost:3307/numerius_db";
    private static final String USER = "root";
    private static final String PASSWORD = "root";

    private static Connection con;
    private static Statement stmt;
    private static ResultSet rs;

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
            System.out.println("Not found. Executing: " + query);
        }
        else
        {
            query = "UPDATE chats " +
                    "SET last_target_language_code = \"" + Bot.getCodeByName(call_data) +
                    "\" WHERE chat_id = " + chatId + ";";
            System.out.println("Found. Executing: " + query);
        }

        executeQuery(query, null, false);
    }

    public static void handleSpecify(String call_data, Long chatId)
    {

        String query = "SELECT * FROM chats WHERE chat_id = " + chatId + ";";

        if (executeQuery(query, "1", true).equals("0"))
        {
            query = "INSERT INTO chats (chat_id, latest_specified_language_code)" +
                    " VALUES (" + chatId + ", \"" + call_data + "\");";
            System.out.println("Not found. Executing: " + query);
        }
        else
        {
            query = "UPDATE chats " +
                    "SET latest_specified_language_code = \"" + call_data +
                    "\" WHERE chat_id = " + chatId + ";";
            System.out.println("Found. Executing: " + query);
        }

        executeQuery(query, null, false);
    }

    public static String getCurrentTargetLanguage(Long chatId)
    {

        String query = "SELECT last_target_language_code FROM chats WHERE chat_id = " + chatId + ";";
        return executeQuery(query, "1", true);
    }

}
