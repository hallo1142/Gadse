package de.hallo1142.gadse;

import de.hallo1142.gadse.entities.Alliance;
import de.hallo1142.gadse.entities.AllianceChannelWhitelist;
import de.hallo1142.gadse.entities.AllianceMember;
import de.hallo1142.gadse.entities.GuildSettings;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.AvailableSettings;
import org.hibernate.cfg.Configuration;
import org.hibernate.tool.schema.Action;

public class Database {

    private final SessionFactory sessionFactory;

    public Database() {
        sessionFactory = new Configuration()
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
                    .addAnnotatedClass(Alliance.class)
                    .addAnnotatedClass(AllianceChannelWhitelist.class)
                    .addAnnotatedClass(AllianceMember.class)
                    .addAnnotatedClass(GuildSettings.class)
                    .buildSessionFactory();
    }

    public SessionFactory getSessionFactory() {
        return sessionFactory;
    }
}
