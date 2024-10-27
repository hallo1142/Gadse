package de.hallo1142.gadse;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.AvailableSettings;
import org.hibernate.cfg.Configuration;
import org.hibernate.tool.schema.Action;

public class Database {

    public Database() {
            SessionFactory sessionFactory = new Configuration()
                    .setProperty(AvailableSettings.JAKARTA_JDBC_URL, "jdbc:mariadb://localhost:3306/gadse")
                    .setProperty(AvailableSettings.JAKARTA_JDBC_USER, "gadse")
                    .setProperty(AvailableSettings.JAKARTA_JDBC_PASSWORD, "gadse")
                    // Remove on Live
                    .setProperty(AvailableSettings.JAKARTA_HBM2DDL_DATABASE_ACTION,
                            Action.SPEC_ACTION_DROP_AND_CREATE)
                    .setProperty(AvailableSettings.SHOW_SQL, true)
                    .setProperty(AvailableSettings.FORMAT_SQL, true)
                    .setProperty(AvailableSettings.HIGHLIGHT_SQL, true)
                    .setProperty(AvailableSettings.AGROAL_MAX_SIZE, 5)
                    .setProperty("hibernate.agroal.providerClassName", "org.mariadb.jdbc.Driver")
                    .buildSessionFactory();
    }
}
