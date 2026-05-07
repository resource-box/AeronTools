package com.hooniegit.AeronSubscriber;

import com.hooniegit.SpringInitializer.IniConfigApplicationContextInitializer;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Aeron 미디어 드라이버 서버 어플리케이션입니다.
 *
 * @Run_Command
 * $ java -Dconfig.path=C:/WAT/interface/config/subscriber/config.ini
 * --add-exports java.base/jdk.internal.misc=ALL-UNNAMED
 * --add-opens java.base/jdk.internal.misc=ALL-UNNAMED
 * --add-opens java.base/java.nio=ALL-UNNAMED
 * --add-opens java.base/sun.nio.ch=ALL-UNNAMED
 * -jar C:/WAT/interface/jar/subscriber/v0.1.0.jar
 */
@EnableScheduling
@SpringBootApplication
public class AeronSubscriberApplication {

    public static void main(String[] args) {
        new SpringApplicationBuilder(AeronSubscriberApplication.class)
                .initializers(new IniConfigApplicationContextInitializer())
                .run(args);
    }

}
