package cn.zoeci.sosobackend;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.scheduling.annotation.EnableScheduling;

// todo 如需开启 Redis，须移除 exclude 中的内容
@SpringBootApplication(exclude = {RedisAutoConfiguration.class})
@MapperScan("cn.zoeci.sosobackend.mapper")
@EnableScheduling
//@EnableAspectJAutoProxy(proxyTargetClass = true, exposeProxy = true)
public class SosoBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(SosoBackendApplication.class, args);
    }

}
