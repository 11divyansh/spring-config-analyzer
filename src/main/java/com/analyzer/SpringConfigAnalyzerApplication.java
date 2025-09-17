package com.analyzer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages="com.analyzer")
public class SpringConfigAnalyzerApplication {

	public static void main(String[] args) {
		SpringApplication.run(SpringConfigAnalyzerApplication.class, args);
	}

}
