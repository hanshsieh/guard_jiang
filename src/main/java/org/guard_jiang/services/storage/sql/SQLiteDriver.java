package org.guard_jiang.services.storage.sql;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverPropertyInfo;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.Enumeration;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * Created by someone on 4/2/2017.
 */
public class SQLiteDriver implements Driver {

    public SQLiteDriver() {
        System.out.println("=======");
    }

    //@Override
    public Connection connect(String url, Properties info) throws SQLException {
        Properties newProp = new Properties(info);
        Enumeration<?> props = info.propertyNames();
        while (props.hasMoreElements()) {
            Object propName = props.nextElement();
            System.out.println("Prop name: " + propName);
        }
        newProp.put("foreign_keys", true);
        //return super.connect(url, newProp);
        return null;
    }

    @Override
    public boolean acceptsURL(String url) throws SQLException {
        return true;
    }

    @Override
    public DriverPropertyInfo[] getPropertyInfo(String url, Properties info) throws SQLException {
        return new DriverPropertyInfo[0];
    }

    @Override
    public int getMajorVersion() {
        return 0;
    }

    @Override
    public int getMinorVersion() {
        return 0;
    }

    @Override
    public boolean jdbcCompliant() {
        return true;
    }

    @Override
    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        return null;
    }
}
