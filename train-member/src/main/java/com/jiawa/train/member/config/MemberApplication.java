package com.jiawa.train.member.config;

import org.mybatis.spring.annotation.MapperScan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.env.Environment;

@SpringBootApplication
@ComponentScan("com.jiawa")
@MapperScan("com.jiawa.train.member.mapper")
public class MemberApplication {

	private static final Logger LOG= LoggerFactory.getLogger(MemberApplication.class);
	public static void main(String[] args) {
		// 1. 启动并获取 context（只启动这一次）
		ConfigurableApplicationContext context = SpringApplication.run(MemberApplication.class, args);

		// 2. 从 context 中获取 environment
		Environment env = context.getEnvironment();

		// 3. 打印信息
		LOG.info("启动成功！！");
		LOG.info("地址: \thttp://127.0.0.1:{}{}/hello",
				env.getProperty("server.port"),
				env.getProperty("server.servlet.context-path"));

		// 打印出当前环境识别到的应用名
		String appName = env.getProperty("spring.application.name");
		System.out.println("当前识别到的应用名是：" + appName);
	}

}
