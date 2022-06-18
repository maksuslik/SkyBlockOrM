package me.maksuslik;

import me.maksuslik.exception.ReflectionException;
import me.maksuslik.util.File;
import org.apache.commons.dbcp2.BasicDataSource;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.NoSuchFileException;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;

public class SkyBlockOrM {
    private static BasicDataSource ds;

    public static void connectionPool(String driverClass, String url, String user, String password, int minPoolSize, int maxPoolSize) {
        ds = new BasicDataSource();
        ds.setDriverClassName(driverClass);
        ds.setUrl(url);
        if (user != null) {
            ds.setUsername(user);
            ds.setPassword(password);
        }
        ds.setInitialSize(minPoolSize);
        ds.setMaxTotal(maxPoolSize);
    }

    public static void applyDDL(String sqlFolder) throws SQLException, IOException, IllegalAccessException, InstantiationException, ReflectionException {
        Connection con = null;
        int i = 1;
        ResultSet tables = null;
        try {
            con = ds.getConnection();
            DatabaseMetaData dbm = con.getMetaData();
            tables = dbm.getTables(null, null, "DDLHISTORY", null);

            if (tables.next()) {
                ArrayList<Object> ret = findBy("true order by id desc limit 1", DDLHistory.class);
                i = 1 + ((DDLHistory) ret.get(0)).getId();
            } else {
                ResultSet tables2 = null;
                try {
                    tables2 = dbm.getTables(null, null, "DDLHistory", null);
                    if (tables2.next()) {
                        ArrayList<Object> ret = findBy("true order by id desc limit 1", DDLHistory.class);
                        i = 1 + ((DDLHistory) ret.get(0)).getId();
                    } else {
                        try (Statement stmt = con.createStatement()) {
                            String sql = "CREATE TABLE DDLHistory (id INT PRIMARY KEY, applyDate DATE)";
                            stmt.execute(sql);
                            System.out.println(sql);
                        }
                    }

                } catch (me.maksuslik.exception.ReflectionException e) {
                    e.printStackTrace();
                } finally {
                    if (tables2 != null) tables.close();
                }
            }

            tables.close();

        } finally {
            if (con != null) con.close();
            if (tables != null) tables.close();

        }

        try {
            for (; true; i++) {
                Connection conn = ds.getConnection();
                try {
                    conn.setAutoCommit(false);
                    File.readLines(sqlFolder + "/" + i + ".sql").forEach((line) -> {
                        Statement stmt = null;
                        try {
                            stmt = conn.createStatement();
                            System.out.println(line);
                            stmt.execute(line);
                        } catch (SQLException e) {
                            e.printStackTrace();
                        } finally {
                            try {
                                if (stmt != null) stmt.close();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });
                    conn.commit();
                    DDLHistory ddl = new DDLHistory();
                    ddl.setId(i);
                    ddl.setApplyDate(new java.util.Date());
                    insert(ddl);
                } finally {
                    if (conn != null) {
                        conn.setAutoCommit(true);
                        conn.close();
                    }
                }
            }
        } catch (NoSuchFileException | ReflectionException e) {
            //It is last sql file
        }

    }

    public static Transaction beginTransaction() throws SQLException {
        return new Transaction(ds.getConnection());
    }


    public static Object update(Object obj) throws IllegalAccessException, SQLException {

        Object ret;
        Transaction tx = SkyBlockOrM.beginTransaction();
        ret = tx.update(obj);
        tx.commit();
        return ret;

    }


    public static void insert(Object obj) throws IllegalAccessException, SQLException, ReflectionException {
        Transaction tx = beginTransaction();
        tx.insert(obj);
        tx.commit();

    }

    public static ArrayList<Object> findBy(String where, Object classObject, Object... params) throws SQLException, IllegalAccessException, InstantiationException, IOException, ReflectionException {
        ArrayList<Object> ret;
        Transaction tx = beginTransaction();
        ret = tx.findBy(where, classObject.getClass(), params);
        tx.commit();
        return ret;
    }

    public static Object findOne(Object className, Object primaryKey) throws SQLException, InstantiationException, IllegalAccessException, IOException, ReflectionException {
        Object ret;
        Transaction tx = beginTransaction();
        ret = tx.findOne(className.getClass(), primaryKey);
        tx.commit();
        return ret;
    }

    public static int deleteBy(String where, Object classObject, Object... params) throws SQLException {
        int ret;
        Transaction tx = beginTransaction();
        ret = tx.deleteBy(where, classObject.getClass(), params);
        tx.commit();
        return ret;
    }

    public static ArrayList<HashMap<String, Object>> selectQuery(String sqlQuery, Object... params) throws SQLException {
        ArrayList<HashMap<String, Object>> ret;
        Transaction tx = beginTransaction();
        ret = tx.selectQuery(sqlQuery, params);
        tx.commit();
        return ret;
    }


    public static int delete(Object obj) throws IllegalAccessException, SQLException {
        int ret;
        Transaction tx = beginTransaction();
        ret = tx.delete(obj);
        tx.commit();
        return ret;
    }

    public static ArrayList<Object> findAll(Object classObject) throws SQLException, InstantiationException, IllegalAccessException, IOException, ReflectionException {

        ArrayList<Object> ret;
        Transaction tx = beginTransaction();
        ret = tx.findAll(classObject.getClass());
        tx.commit();

        return ret;
    }

    public static void setField(Object object, String fieldName, Object value) throws IllegalAccessException, ReflectionException {
        boolean noField = true;
        for (Field field : object.getClass().getDeclaredFields()) {
            if (field.getName().equals(fieldName)) {
                field.setAccessible(true);
                field.set(object, value);
                noField = false;
            }
        }
        if (noField) {
            throw new ReflectionException("No field");
        }
    }
}
