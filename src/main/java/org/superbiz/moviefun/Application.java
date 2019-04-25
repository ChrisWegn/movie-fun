package org.superbiz.moviefun;

import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.Database;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

@SpringBootApplication(exclude = {
        DataSourceAutoConfiguration.class,
        HibernateJpaAutoConfiguration.class
})
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Bean
    public ServletRegistrationBean actionServletRegistration(ActionServlet actionServlet) {
        return new ServletRegistrationBean(actionServlet, "/moviefun/*");
    }

    @Bean
    public DatabaseServiceCredentials databaseServiceCredentials(
            @Value("#{environment.VCAP_SERVICES}") String vcapServicesJson
    ) {
        return new DatabaseServiceCredentials(vcapServicesJson);
    }

    @Bean
    public DataSource albumsDataSource(DatabaseServiceCredentials serviceCredentials) {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(serviceCredentials.jdbcUrl("albums-mysql"));
        return new HikariDataSource(config);
    }

    @Bean
    public DataSource moviesDataSource(DatabaseServiceCredentials serviceCredentials) {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(serviceCredentials.jdbcUrl("movies-mysql"));
        return new HikariDataSource(config);
    }

    @Bean
    public HibernateJpaVendorAdapter hibernateJpaVendorAdapter(){
        HibernateJpaVendorAdapter hibernateJpaVendorAdapter = new HibernateJpaVendorAdapter();
        hibernateJpaVendorAdapter.setDatabase(Database.MYSQL);
        hibernateJpaVendorAdapter.setDatabasePlatform("org.hibernate.dialect.MySQL5Dialect");
        hibernateJpaVendorAdapter.setGenerateDdl(true);

        return hibernateJpaVendorAdapter;
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean moviesFactory(
            @Qualifier("moviesDataSource") DataSource dataSource,
            HibernateJpaVendorAdapter hibernateJpaVendorAdapter
    ) {
        LocalContainerEntityManagerFactoryBean factoryBean = new LocalContainerEntityManagerFactoryBean();
        factoryBean.setDataSource(dataSource);
        factoryBean.setJpaVendorAdapter(hibernateJpaVendorAdapter);
        factoryBean.setPackagesToScan("org.superbiz.moviefun.movies");
        factoryBean.setPersistenceUnitName("moviesPersistenceUnit");

        return factoryBean;
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean albumsFactory(
            @Qualifier("albumsDataSource") DataSource dataSource,
            HibernateJpaVendorAdapter hibernateJpaVendorAdapter
    ) {
        LocalContainerEntityManagerFactoryBean factoryBean = new LocalContainerEntityManagerFactoryBean();
        factoryBean.setDataSource(dataSource);
        factoryBean.setJpaVendorAdapter(hibernateJpaVendorAdapter);
        factoryBean.setPackagesToScan("org.superbiz.moviefun.albums");
        factoryBean.setPersistenceUnitName("albumsPersistenceUnit");

        return factoryBean;
    }

    @Bean
    public PlatformTransactionManager albumsTransactionManager(EntityManagerFactory albumsFactory) {
        return new JpaTransactionManager(albumsFactory);
    }

    @Bean
    public PlatformTransactionManager moviesTransactionManager(EntityManagerFactory moviesFactory) {
        return new JpaTransactionManager(moviesFactory);
    }

}
