# Aeron Java Tools
Aeron 패키지 기반의 단순한 데이터 통신 라이브러리입니다.

### Target
- Simple Binary Encoding(SBE) 기반의 인코딩/디코딩으로 직렬화 단계를 축약하여 GC 생성을 방지합니다.
- IP 및 Port 입력 여부에 따라 UDP 또는 IPC 통신을 선택 지원합니다.
- Resource Container를 Thread Local 방식으로 관리하여 객체를 재사용하고 GC 부담을 최소화합니다.

### 라이브러리 사용법
1. Server (Aeron Media Driver / Spring Boot Application)
```java
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import com.hooniegit.AeronJavaTools.Server.AeronMediaDriver;

@Component
public class MediaDriverService {
    
    @Value("${aeron.server.location:aeron-sbe-ipc}")
    private String location; // %temp 폴더의 Media Driver 폴더 경로 (임의 지정 가능)
    private AeronMediaDriver mediaDriver;

    /**
     * Media Driver 초기화 및 시작
     */
    @PostConstruct
    public void init() {
        this.mediaDriver = new AeronMediaDriver(this.location);
        this.mediaDriver.startDriver();
    }

    /**
     * Media Driver 종료
     */
    @PreDestroy
    public void destroy() {
        this.mediaDriver.stopDriver();
    }

    /**
     * Media Driver 상태 점검 (Periodical)
     */
    @Scheduled(fixedDelayString = "${aeron.server.monitoring.interval:10000}")
    public void checkHealthAndRestart() {
        this.mediaDriver.checkHealthAndRestart();
    }

}

```
2. Client (Publisher)
```java
import com.hooniegit.AeronJavaTools.Common.TagData;
import com.hooniegit.AeronJavaTools.Publisher.DataPublisher;

String location = "aeron-sbe-ipc"; // // %temp 폴더의 Media Driver 폴더 경로
int streamId = 1; // Stream ID (임의 지정 가능)

// Data Publisher (Localhost IPC 통신)
DataPublisher publisher = new DataPublisher(location, streamId);

// Media Driver 연결
publisher.connect();

// Single Data 전송
int id = 123;
double value = 45.67;
string timestamp = "2024-06-01T12:00:00Z";
publisher.publishSingleData(id, value, timestamp);

// List Data 전송
List<TagData> dataList = ... ;
string timestamp = "2024-06-01T12:00:00Z";
publisher.publishListData(timestamp, dataList);
```
3. client (Subscriber / Spring Boot Application)
```java
import com.hooniegit.AeronJavaTools.Subscriber.DataMessageListener;
import com.hooniegit.AeronJavaTools.Subscriber.DataSubscriber;
import com.hooniegit.sbe.ListDataMessageDecoder;
import com.hooniegit.sbe.SingleDataMessageDecoder;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.stereotype.Component;
import java.util.concurrent.atomic.AtomicBoolean;

@Component
public class SubscriberService {
    
    private final AtomicBoolean keepAppRunning = new AtomicBoolean(true);
    private Thread reconnectThread;
    private String location = "aeron-sbe-ipc"; // %temp 폴더의 Media Driver 폴더 경로 (임의 지정 가능)
    private int streamId = 10; // Stream ID (Publisher와 동일하게 설정)

    /**
     * Media Driver 연결 -> 데이터 수신 및 처리
     */
    @PostConstruct
    private void start() {
        // 무한 루프
        reconnectThread = new Thread(() -> {

            // 외부 무한 재연결 루프
            while (keepAppRunning.get()) {
                try {
                    // IP 및 Port 입력 : UDP / 미입력 : IPC
                    try (DataSubscriber subscriber = new DataSubscriber(aeronDir, location)) {

                        // Media Driver 연결 해제 시 동작
                        subscriber.setOnDisconnected(() -> {
                            subscriber.stopReceiving(); // while 루프 종료
                        });

                        // Media Driver 연결
                        subscriber.connect();

                        // 데이터 수신 콜백 등록
                        subscriber.startReceiving(new DataMessageListener() {
                            @Override
                            public void onSingleDataReceived(SingleDataMessageDecoder decoder) {
                                int id = decoder.id();
                                String value = decoder.value();
                                String timestamp = decoder.timestamp();

                                // 이하에서 데이터 처리
                            }

                            @Override
                            public void onListDataReceived(ListDataMessageDecoder decoder) {
                                for (ListDataMessageDecoder.EntriesDecoder entry : decoder.entries()) {
                                    int id = entry.id();
                                    double value = entry.value();

                                    // 이하에서 데이터 처리
                                }
                                String timestamp = decoder.timestamp();

                                // 이하에서 데이터 처리
                            }
                        });
                    }
                    
                // connect() 실패 시 동작    
                } catch (Exception e) {
                    // 예외 로그 출력
                }

                // While 루프 종료 시 동작 (Media Driver 연결 해제 또는 기타 예외 발생 시)
                if (keepAppRunning.get()) {
                    try {
                        Thread.sleep(3000);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        });
        
        // 반복
        reconnectThread.start();
    }

    /**
     * Media Driver 연결 해제 및 재연결 스레드 종료
     */
    @PreDestroy
    private void stop() {
        keepAppRunning.set(false);
        if (reconnectThread != null) {
            reconnectThread.interrupt();
        }
    }

}
```
### Version
- v0.1.0: Initial Release / Media Driver Wrapper 클래스 구현 / Publisher 및 Subscriber 클래스 구현