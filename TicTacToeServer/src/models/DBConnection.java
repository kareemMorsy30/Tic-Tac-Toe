package models;

import java.sql.*;
import java.util.Vector;

public class DBConnection {
    private static Connection ret = null;
    
    public static Connection getConnection()
    {
        try {
            Config config = new Config();
            
            Class.forName("com.mysql.jdbc.Driver");

            /* Create connection url. */
            String mysqlConnUrl = config.getMysqlConnUrl();

            /* db user name. */
            String mysqlUserName = config.getMysqlUserName();

            /* db password. */
            String mysqlPassword = config.getMysqlPassword();

            if(ret == null)
            {
                /* Get the Connection object. */

                ret = DriverManager.getConnection(mysqlConnUrl, mysqlUserName , mysqlPassword);
                System.out.println(ret.getMetaData());
                return ret;
            }
            else {
                return ret;
            }
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
            return null;
        }
    }








    public static void closeConnection()
    {
        try {
            getConnection().close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        ret = null;

    }
}
