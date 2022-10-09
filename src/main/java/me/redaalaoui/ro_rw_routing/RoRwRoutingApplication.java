package me.redaalaoui.ro_rw_routing;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Map;
import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import me.redaalaoui.ro_rw_routing.data_source.DataSourceTypes;
import me.redaalaoui.ro_rw_routing.data_source.RoutingDataSource;
import org.h2.jdbcx.JdbcConnectionPool;
import org.hibernate.cache.ehcache.internal.EhcacheRegionFactory;
import org.hibernate.cfg.AvailableSettings;
import org.hibernate.jpa.HibernatePersistenceProvider;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.Database;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

@SpringBootApplication
public class RoRwRoutingApplication {

  public static void main(String[] args) {
    SpringApplication.run(RoRwRoutingApplication.class, args);
  }

  @Bean(DataSourceTypes.MAIN)
  public DataSource mainDatasource() {
    return setupDatabase(
        JdbcConnectionPool.create(
            "jdbc:h2:mem:main;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE", "sa", "sa"));
  }

  @Bean(DataSourceTypes.REPLICA)
  public DataSource replicaDatasource() {
    return setupDatabase(
        JdbcConnectionPool.create(
            "jdbc:h2:mem:replica;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE", "sa", "sa"));
  }

  @Bean
  public DataSource routingDataSource() {
    RoutingDataSource routingDataSource = new RoutingDataSource();
    routingDataSource.setTargetDataSources(
        Map.ofEntries(
            Map.entry(DataSourceTypes.MAIN, mainDatasource()),
            Map.entry(DataSourceTypes.REPLICA, replicaDatasource())));
    return routingDataSource;
  }

  @Bean
  public LocalContainerEntityManagerFactoryBean entityManagerFactoryBean() {
    LocalContainerEntityManagerFactoryBean bean = new LocalContainerEntityManagerFactoryBean();
    bean.setPersistenceUnitName(getClass().getSimpleName());
    bean.setPersistenceProvider(new HibernatePersistenceProvider());

    bean.setDataSource(routingDataSource());
    bean.setPackagesToScan("me.redaalaoui.ro_rw_routing");

    bean.setJpaPropertyMap(
        Map.ofEntries(
            Map.entry(AvailableSettings.USE_SECOND_LEVEL_CACHE, true),
            Map.entry(AvailableSettings.USE_QUERY_CACHE, true),
            Map.entry(AvailableSettings.CACHE_REGION_FACTORY, new EhcacheRegionFactory())));

    HibernateJpaVendorAdapter jpaVendorAdapter = new HibernateJpaVendorAdapter();
    jpaVendorAdapter.setShowSql(false);
    jpaVendorAdapter.setDatabase(Database.H2);
    jpaVendorAdapter.setGenerateDdl(false);
    jpaVendorAdapter.getJpaDialect().setPrepareConnection(false);
    bean.setJpaVendorAdapter(jpaVendorAdapter);
    return bean;
  }

  @Bean("transactionManager")
  public PlatformTransactionManager transactionManager(EntityManagerFactory entityManagerFactory) {
    JpaTransactionManager transactionManager = new JpaTransactionManager();
    transactionManager.setEntityManagerFactory(entityManagerFactory);
    return transactionManager;
  }

  @Bean
  public TransactionTemplate transactionTemplate(PlatformTransactionManager transactionManager) {
    return new TransactionTemplate(transactionManager);
  }

  private DataSource setupDatabase(DataSource dataSource) {
    try (Connection connection = dataSource.getConnection();
        PreparedStatement preparedStatement =
            connection.prepareStatement(
                """
				create table post(
				id IDENTITY PRIMARY KEY
				);
				create table postcomment(
				id IDENTITY PRIMARY KEY,
				post_id bigint references post(id),
				review varchar
				);
				""")) {
      preparedStatement.executeUpdate();
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }

    return dataSource;
  }
}
