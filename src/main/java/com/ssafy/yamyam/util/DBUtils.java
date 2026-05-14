package com.ssafy.yamyam.util;

import java.sql.Connection;
import java.sql.SQLException;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

public class DBUtils {
	private final String driverClassName = "com.mysql.cj.jdbc.Driver";
    private static final String URL = "jdbc:mysql://localhost:3306/ssafy_yumyum";    
    private static final String USER = "ssafy";
    private static final String PASSWORD = "ssafy";
    private static HikariDataSource ds;
    private final static DBUtils util = new DBUtils();
    
    private void init() {
        System.out.println("DB util 초기화");
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(URL);
        config.setUsername(USER);
        config.setPassword(PASSWORD);
        config.setDriverClassName(driverClassName);

        config.setMinimumIdle(3);
        config.setMaximumPoolSize(5);
        config.setIdleTimeout(1000 * 60 * 5);
        config.setConnectionTimeout(1000 * 60 * 10);
        config.addDataSourceProperty("profileSQL", "true");
        ds = new HikariDataSource(config);
    }

    public static DBUtils getUtil() {
        return util;
    }

    private DBUtils() {
        init();
    }

    public static Connection getConnection() throws SQLException {
        return ds.getConnection();
    }

    public static void close(AutoCloseable... closeables) {
        for (AutoCloseable c : closeables) {
            if (c != null) {
                try {
                    c.close();
                } catch (Exception ignore) {

                }
            }
        }
    }

}
