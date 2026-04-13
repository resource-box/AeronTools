package com.hooniegit.AeronServer;

import com.hooniegit.AeronJavaTools.Server.AeronMediaDriver;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class MediaDriverService {

    // Media Driver
    @Value("${aeron.server.location:aeron-sbe-ipc}")
    private String location;
    private AeronMediaDriver mediaDriver;

    @PostConstruct
    public void init() {
        this.mediaDriver = new AeronMediaDriver(this.location);
        this.mediaDriver.startDriver();
    }

    @PreDestroy
    public void destroy() {
        this.mediaDriver.stopDriver();
    }

    @Scheduled(fixedDelayString = "${aeron.server.monitoring.interval:10000}")
    public void checkHealthAndRestart() {
        this.mediaDriver.checkHealthAndRestart();
    }

}
