package com.ady4k.todoistapi.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.util.logging.Logger;

@Configuration
public class PersistenceConfig {

    @Value("${CONNECTION_STRING:${CONNECTION_DEFAULT}}")
    private String dbUrl;

    @Value("${DB_USER}")
    private String dbUsername;

    @Value("${DB_PASSWORD}")
    private String dbPassword;

    private static final Logger logger = Logger.getLogger(PersistenceConfig.class.getName());

    private static Boolean isPostgresAvailable = null;

    @Bean
    public DataSource dataSource() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        isPostgresAvailable = isPostgresAvailable();

        if (isPostgresAvailable) {
            dataSource.setDriverClassName("org.postgresql.Driver");
            dataSource.setUrl(dbUrl);
            dataSource.setUsername(dbUsername);
            dataSource.setPassword(dbPassword);
        } else {
            dataSource.setDriverClassName("org.sqlite.JDBC");
            dataSource.setUrl("jdbc:sqlite:local.db");
        }

        return dataSource;
    }

    @Bean
    public JpaVendorAdapter jpaVendorAdapter() {
        HibernateJpaVendorAdapter jpaVendorAdapter = new HibernateJpaVendorAdapter();
        jpaVendorAdapter.setDatabasePlatform(getDialect());
        return jpaVendorAdapter;
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory() throws SQLException {
        LocalContainerEntityManagerFactoryBean factoryBean = new LocalContainerEntityManagerFactoryBean();

        factoryBean.setDataSource(dataSource());
        factoryBean.setJpaVendorAdapter(jpaVendorAdapter());
        factoryBean.setPackagesToScan("com.ady4k.todoistapi.model");

        Properties jpaProperties = new Properties();
        jpaProperties.put("hibernate.dialect", getDialect());
        factoryBean.setJpaProperties(jpaProperties);

        return factoryBean;
    }

    private boolean isPostgresAvailable() {
        try (Connection connection = DriverManager.getConnection(dbUrl, dbUsername, dbPassword)) {
            return connection.isValid(2);
        } catch (SQLException e) {
            logger.warning("PostgreSQL database is not available. Falling back to SQLite DB");
            logger.warning("SQLite DB is used, changing dialect to SQLiteDialect");
            return false;
        }
    }

    private String getDialect() {
        return isPostgresAvailable ?
            "org.hibernate.dialect.PostgreSQLDialect" :
            "org.hibernate.community.dialect.SQLiteDialect";
    }
}
