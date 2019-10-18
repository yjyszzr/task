package com.dl.task.configurer;

import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.jdbc.DataSourceBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Primary;

import com.alibaba.druid.filter.Filter;
import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.wall.WallConfig;
import com.alibaba.druid.wall.WallFilter;

@Configuration
public class DataSourceConfig {
	@Autowired
    WallFilter wallFilter;
	
	@Bean(name = "dataSource1")
	@Primary
	// application.properteis中对应属性的前缀
	@ConfigurationProperties(prefix = "spring.datasource1.druid")
	public DataSource dataSource1() {
		DruidDataSource dateSource = new DruidDataSource();
		 // filter
        List<Filter> filters = new ArrayList<>();
        filters.add(wallFilter);
        dateSource.setProxyFilters(filters);
		return dateSource;
	}

	@Bean(name = "dataSource2")
	// application.properteis中对应属性的前缀
	@ConfigurationProperties(prefix = "spring.datasource2.druid")
	public DataSource dataSource2() {
		return DataSourceBuilder.create().type(DruidDataSource.class).build();
	}

    @Bean(name = "wallConfig")
    WallConfig wallFilterConfig(){
        WallConfig wc = new WallConfig ();
        wc.setMultiStatementAllow(true);
        return wc;
    }

    @Bean(name = "wallFilter")
    @DependsOn("wallConfig")
    WallFilter wallFilter(WallConfig wallConfig){
        WallFilter wfilter = new WallFilter ();
        wfilter.setConfig(wallConfig);
        return wfilter;
    }
	@Value("${mybatis.mapper-locations}")
	private String mybatisMapperLocations;
	@Value("${mybatis.type-aliases-package}")
	private String mybatisTypeAliasesPackage;
	
	public String getMybatisMapperLocations(){
		return mybatisMapperLocations;
	}
	
	public String getMybatisTypeAliasesPackage(){
		return mybatisTypeAliasesPackage;
	}
}
