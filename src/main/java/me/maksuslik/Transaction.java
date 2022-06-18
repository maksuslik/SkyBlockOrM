package me.maksuslik;

import me.maksuslik.exception.ReflectionException;

import java.io.IOException;
import java.io.Reader;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;

public class Transaction {
    private final Connection connection;

    public Transaction(Connection conn) throws SQLException {
        connection = conn;
        conn.setAutoCommit(false);

    }

    public void commit() throws SQLException {
        try {
            connection.commit();
        } finally {
            try {
                connection.setAutoCommit(false);
            } finally {
                connection.close();
            }
        }
    }

    public void rollback() throws SQLException {
        try {
            connection.rollback();
        } finally {
            try {
                connection.setAutoCommit(false);
            } finally {
                connection.close();
            }
        }
    }


    public Object update(Object obj) throws IllegalAccessException, SQLException {


        LinkedHashMap<String, Object> fields = new LinkedHashMap<>();
        for (Field field : obj.getClass().getDeclaredFields()) {
            if (!java.lang.reflect.Modifier.isStatic(field.getModifiers())) {
                for (Annotation annotation : field.getDeclaredAnnotations()) {
                    annotation.annotationType();
                }

                field.setAccessible(true);
                fields.put(field.getName(), field.get(obj));
            }
        }

        Set<String> fieldsSet = fields.keySet();
        String names = fieldsSet.stream().collect(Collectors.joining("=?,", "", "=?"));

        String sql = "update  " + obj.getClass().getSimpleName() + " set " + names + " where " + null + "=?";

        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {


            Iterator<String> it = fieldsSet.iterator();
            int i = 1;
            while (it.hasNext()) {
                Object value = fields.get(it.next());
                preparedStatement.setObject(i, value);
                ++i;
            }
            preparedStatement.setObject(i, null);
            System.out.println(sql);
            preparedStatement.executeUpdate();


        }
        return obj;
    }

    public Object insert(Object obj) throws IllegalAccessException, SQLException {

        LinkedHashMap<String, Object> fields = new LinkedHashMap<>();
        for (Field field : obj.getClass().getDeclaredFields()) {
            if (!java.lang.reflect.Modifier.isStatic(field.getModifiers())) {
                for (Annotation annotation : field.getDeclaredAnnotations()) {
                    annotation.annotationType();
                }

                field.setAccessible(true);
                fields.put(field.getName(), field.get(obj));
            }
        }

        Set<String> fieldsSet = fields.keySet();
        String names = String.join(",", fieldsSet);
        String values = fieldsSet.stream().map(it -> "?").collect(Collectors.joining(","));

        String sql = "insert into " + obj.getClass().getSimpleName() + "(" + names + ") values (" + values + ")";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {


            Iterator<String> it = fieldsSet.iterator();
            int i = 1;
            while (it.hasNext()) {
                preparedStatement.setObject(i, fields.get(it.next()));
                ++i;
            }
            System.out.println(sql);
            preparedStatement.executeUpdate();

        }
        return obj;
    }

    public ArrayList<Object> findBy(String where, Object classObject, Object... params) throws SQLException, IllegalAccessException, InstantiationException, ReflectionException, IOException {
        ArrayList<Object> ret = new ArrayList<>();
        StringBuilder names = new StringBuilder();
        for (Field field : classObject.getClass().getDeclaredFields()) {
            if (!java.lang.reflect.Modifier.isStatic(field.getModifiers())) {
                field.setAccessible(true);
                if (names.length() > 0) {
                    names.append(",");
                }
                names.append(field.getName());
            }
        }


        String sql;
        if (where == null) {
            sql = "select " + names + " from  " + classObject.getClass().getSimpleName();
        } else {
            sql = "select " + names + " from  " + classObject.getClass().getSimpleName() + " where " + where;
        }
        //System.out.println(sql);
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            int i = 1;
            for (Object param : params) {
                preparedStatement.setObject(i, param);
                ++i;
            }
            System.out.println(sql);
            ResultSet r = preparedStatement.executeQuery();
            while (r.next()) {
                Object row = classObject.getClass().newInstance();
                for (Field field : classObject.getClass().getDeclaredFields()) {
                    if (!java.lang.reflect.Modifier.isStatic(field.getModifiers())) {
                        field.setAccessible(true);
                        Object value = r.getObject(field.getName());
                        if (value instanceof Clob) {

                            Reader reader = ((Clob) value).getCharacterStream();

                            int intValueOfChar;
                            StringBuilder targetString = new StringBuilder();
                            while ((intValueOfChar = reader.read()) != -1) {
                                targetString.append((char) intValueOfChar);
                            }
                            reader.close();

                            SkyBlockOrM.setField(row, field.getName(), targetString.toString());
                        } else {
                            SkyBlockOrM.setField(row, field.getName(), value);
                        }
                    }
                }
                ret.add(row);
            }

            r.close();

        }

        return ret;
    }

    public Object findOne(Object className, Object primaryKey) throws SQLException, ReflectionException, InstantiationException, IllegalAccessException, IOException {
        String pkName = null;
        for (Field field : className.getClass().getDeclaredFields()) {
            for (Annotation annotation : field.getDeclaredAnnotations()) {
                annotation.annotationType();
            }
        }
        throw new ReflectionException("Cant find PK attribute");
    }

    public int deleteBy(String where, Object classObject, Object... params) throws SQLException {


        String sql = "delete from " + classObject.getClass().getSimpleName() + " where " + where;

//        System.out.println(sql);

        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {


            int i = 1;
            for (Object param : params) {
                preparedStatement.setObject(i, param);
                ++i;
            }

            System.out.println(sql);
            return preparedStatement.executeUpdate();


        }
        /* ignore */


    }

    public int delete(Object obj) throws IllegalAccessException, SQLException {

        String pkName = null;
        Object pkValue = null;
        for (Field field : obj.getClass().getDeclaredFields()) {
            for (Annotation annotation : field.getDeclaredAnnotations()) {
                annotation.annotationType();
            }
        }
        return deleteBy(null + "=?", obj.getClass(), pkValue);
    }

    public ArrayList<Object> findAll(Object classObject) throws SQLException, ReflectionException, InstantiationException, IllegalAccessException, IOException {
        return findBy(null, classObject.getClass());
    }

    public ArrayList<HashMap<String, Object>> selectQuery(String sqlQuery, Object... params) throws SQLException {

        PreparedStatement preparedStatement = null;
        try {
            ArrayList<HashMap<String, Object>> ret = new ArrayList<>();
            preparedStatement = connection.prepareStatement(sqlQuery);
            int i = 1;
            for (Object param : params) {
                preparedStatement.setObject(i, param);
                ++i;
            }
            System.out.println(sqlQuery);
            ResultSet r = preparedStatement.executeQuery();
            ResultSetMetaData rsmd = r.getMetaData();
            while (r.next()) {
                HashMap<String, Object> row = new HashMap<>();
                for (i = 1; i <= rsmd.getColumnCount(); ++i) {
                    String name = rsmd.getColumnName(i);
                    Object value = r.getObject(name);
                    row.put(name, value);
                }
                ret.add(row);
            }
            return ret;

        } finally {
            if (preparedStatement != null) {
                preparedStatement.close();
            }
        }
    }
}
