package com.yixinintl;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ImportResource;
import com.yixinintl.mapper.*;
@EnableAutoConfiguration
@EnableConfigurationProperties
//@ImportResource({"classpath:applicationContext.xml"})
@ComponentScan(basePackages={"com.cpts",  "com.cpts.util","com.cpts.domain","com.cpts.mapper"})  

@SpringBootApplication
@MapperScan(basePackages={"com.cpts.mapper"})
public class CptsApplication {

	public static void main(String[] args) {
		SpringApplication.run(CptsApplication.class, args);
	}
}
