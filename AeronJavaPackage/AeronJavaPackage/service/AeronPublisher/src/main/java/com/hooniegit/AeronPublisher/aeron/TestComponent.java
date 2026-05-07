package com.hooniegit.AeronPublisher.aeron;

import com.hooniegit.AeronJavaTools.Common.TagData;
import com.hooniegit.AeronJavaTools.Publisher.DataPublisher;
import com.hooniegit.AeronPublisher.config.PublisherConfig;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * 테스트용 컴포넌트입니다.
 */
@Component
public class TestComponent {

    private final List<TagData<Double>> dataList = new ArrayList<>();
    private DataPublisher dataPublisher;

    @Autowired
    private PublisherConfig config;

    @PostConstruct
    public void test() throws InterruptedException {

        String LOCATION = config.getLocation();
        String HOST = config.getHost();
        int PORT = config.getPort();
        int STREAM_ID = 10;

        if (HOST != null && !"NULL".equalsIgnoreCase(HOST)) {
            this.dataPublisher = new DataPublisher(LOCATION, STREAM_ID, HOST, PORT);
        } else {
            this.dataPublisher = new DataPublisher(LOCATION, STREAM_ID);
        }

        this.dataPublisher.connect();
        Thread.sleep(3000);

        while (true) {
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS"));
            generateTagData();
            this.dataPublisher.publishListDataMessage(dataList, timestamp);
            this.dataList.clear();
            Thread.sleep(1);
        }

    }

    public void generateTagData() {
        for (int i = 0; i < 5000; i++) {
            this.dataList.add(new TagData<>(i, 100.0 + i));
        }
    }

}
