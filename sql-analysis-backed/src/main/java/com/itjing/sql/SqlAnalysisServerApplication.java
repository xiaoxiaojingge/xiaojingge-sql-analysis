package com.itjing.sql;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

@SpringBootApplication(exclude = DataSourceAutoConfiguration.class)
public class SqlAnalysisServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(SqlAnalysisServerApplication.class, args);
	}

}
