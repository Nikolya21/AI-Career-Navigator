package com.aicareer.core.config;

import javax.sql.DataSource;
import java.sql.*;
import java.io.PrintWriter;
import java.util.logging.Logger;
import java.util.concurrent.atomic.AtomicInteger; // ← Добавили

public class SimpleDataSource implements DataSource {
    private final String url;
    private final String username;
    private final String password;
    private final AtomicInteger connectionCount = new AtomicInteger(0); // ← Добавили

    public SimpleDataSource(String url, String username, String password) {
        this.url = url;
        this.username = username;
        this.password = password;
    }

    @Override
    public Connection getConnection() throws SQLException {
        connectionCount.incrementAndGet(); // ← Считаем соединения
        return DriverManager.getConnection(url, username, password);
    }

    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        connectionCount.incrementAndGet(); // ← Считаем соединения
        return DriverManager.getConnection(url, username, password);
    }

    // ← ДОБАВИЛИ МЕТОД ДЛЯ МОНИТОРИНГА
    public int getActiveConnections() {
        return connectionCount.get();
    }

    // Остальные методы без изменений...
    @Override public PrintWriter getLogWriter() throws SQLException { return null; }
    @Override public void setLogWriter(PrintWriter out) throws SQLException { }
    @Override public void setLoginTimeout(int seconds) throws SQLException { }
    @Override public int getLoginTimeout() throws SQLException { return 0; }
    @Override public Logger getParentLogger() { return null; }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return iface != null && iface.isAssignableFrom(getClass());
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T unwrap(Class<T> iface) throws SQLException {
        if (iface == null) throw new SQLException("Interface argument cannot be null");
        if (!iface.isAssignableFrom(getClass())) {
            throw new SQLException("Cannot unwrap to " + iface.getName());
        }
        return (T) this;
    }
}