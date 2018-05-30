package com.bitbreeds;

import org.springframework.context.annotation.*;

import javax.sql.DataSource;

@Configuration
public class DataSourceInit {

    @Bean
    public DataSource xaDataSource()  {
        return DataSources.createDataSource("jdbc:h2:mem:h2/urldb","user","pass");
    }

}