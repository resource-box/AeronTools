package com.hooniegit.AeronSubscriber.aeron;

import com.hooniegit.AeronJavaTools.Subscriber.DataMessageListener;
import com.hooniegit.AeronJavaTools.Subscriber.DataSubscriber;
import com.hooniegit.AeronSubscriber.config.SubscriberConfig;
import com.hooniegit.sbe.ListDataMessageDecoder;
import com.hooniegit.sbe.SingleDataMessageDecoder;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 테스트용 컴포넌트입니다.
 */
@Component
public class TestComponent implements DataMessageListener {

    // 앱의 생명주기를 관리하는 플래그
    private final AtomicBoolean keepAppRunning = new AtomicBoolean(true);
    private Thread reconnectThread;

    @Autowired
    private SubscriberConfig config;

    @Override
    public void onSingleDataReceived(SingleDataMessageDecoder decoder) {
        // [중요] SBE에서는 가변 길이 데이터(String 등)를 스키마에 정의된 순서대로 읽어야 합니다.
        // SingleDataMessage 스키마: 1. value -> 2. timestamp
        String value = decoder.value();
        String timestamp = decoder.timestamp();

        // 이하에서 데이터 처리
//        System.out.println(timestamp);
    }

    @Override
    public void onListDataReceived(ListDataMessageDecoder decoder) {

        // [중요] SBE에서는 그룹(entries) 순회가 모두 끝난 후에 다음 데이터를 읽어야 합니다.
        for (ListDataMessageDecoder.EntriesDecoder entry : decoder.entries()) {
            int id = entry.id();
            double value = entry.value();
        }

        // 그룹 순회 후 스키마에 정의된 다음 가변 길이 데이터(timestamp)를 읽습니다.
        String timestamp = decoder.timestamp();

        // 이하에서 데이터 처리
        System.out.println(timestamp);
    }


    private void task() {
        // 외부 무한 재연결 루프
        while (keepAppRunning.get()) {

            DataSubscriber subscriber;
            try {
                System.out.println("\n[System] 미디어 드라이버 연결을 시도합니다..."); // TEST

                String LOCATION = config.getLocation();
                String HOST = config.getHost();
                int PORT = config.getPort();
                int STREAM_ID = 10;

                if (HOST != "NULL") {
                    subscriber = new DataSubscriber(LOCATION, STREAM_ID, HOST, PORT);
                } else {
                    subscriber = new DataSubscriber(LOCATION, STREAM_ID);
                }

                // [핵심] 연결 끊김 감지 시 내부 수신 루프 종료 지시
                subscriber.setOnDisconnected(() -> {
                    System.out.println("[System] 연결 끊김 감지. 수신을 중지하고 재연결을 준비합니다."); // TEST
                    subscriber.stopReceiving(); // while 루프를 빠져나오게 함
                    subscriber.close();
                });

                // 1. 미디어 드라이버 연결
                subscriber.connect();

                // 2. 수신 루프 시작 (이곳에서 스레드가 블로킹되어 데이터를 계속 받습니다)
                // 현재 클래스(TestComponent)가 DataMessageListener를 구현하고 있으므로 this 전달
                subscriber.startReceiving(this);

            } catch (Exception e) {
                // 미디어 드라이버가 꺼져있어 connect() 실패 시 이쪽으로 빠짐
                System.err.println("[Error] Aeron 연결/수신 중 예외 발생: " + e.getMessage());
            }

            // 루프를 빠져나옴 = 에러가 났거나 연결이 끊어졌음을 의미
            if (keepAppRunning.get()) {
                System.out.println("[System] 3초 후 재연결을 시도합니다...");
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    @PostConstruct
    private void start() {
        // Spring Boot 초기화를 막지 않기 위해 별도 스레드에서 무한 루프 실행
        reconnectThread = new Thread(this::task);
        reconnectThread.start();
    }

    // 앱 종료 시 안전하게 스레드를 정리합니다.
    @PreDestroy
    private void stop() {
        keepAppRunning.set(false);
        if (reconnectThread != null) {
            reconnectThread.interrupt();
        }
    }

}
