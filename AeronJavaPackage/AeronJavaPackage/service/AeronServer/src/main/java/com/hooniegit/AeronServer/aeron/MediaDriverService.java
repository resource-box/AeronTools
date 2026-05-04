package com.hooniegit.AeronServer.aeron;

import com.hooniegit.AeronJavaTools.Server.AeronMediaDriver;
import com.hooniegit.AeronServer.config.MediaDriverConfig;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Aeron 미디어 드라이버 서비스를 시작합니다. 미디어 드라이버 주소는 config.ini 파일의 aeron.server.location 항목을 참조합니다.
 */
@Component
public class MediaDriverService {

    // Media Driver
    private String location;
    private AeronMediaDriver mediaDriver;

    @Autowired
    private MediaDriverConfig config;

    @PostConstruct
    public void init() {
        this.location = config.getLocation();
        this.mediaDriver = new AeronMediaDriver(this.location);
        this.mediaDriver.startDriver();
    }

    @PreDestroy
    public void destroy() {
        this.mediaDriver.stopDriver();
    }

    @Scheduled(fixedDelayString = "60000")
    public void checkHealthAndRestart() {
        this.mediaDriver.checkHealthAndRestart();
    }

}
