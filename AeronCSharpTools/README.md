# Aeron CSharp Tools
Aeron 패키지 기반의 단순한 데이터 통신 라이브러리입니다.

### Target
- Simple Binary Encoding(SBE) 기반의 인코딩/디코딩으로 직렬화 단계를 축약하여 GC 생성을 방지합니다.
- IP 및 Port 입력 여부에 따라 UDP 또는 IPC 통신을 선택 지원합니다.
- Publisher: Resource Container 를 Thread Local 방식으로 관리하여 객체를 재사용하고 GC 부담을 최소화합니다.
- Subscrber: Unsafe Shared Memory 방식으로 공유 메모리를 직접 관리하여 GC 생성을 방지합니다.

### 라이브러리 사용법
1. Server (Aeron Media Driver)
```cs

```
2. Client (Publisher)
```cs
using Com.Hooniegit.AeronCSharpTools.Tools.DataPublisher;
using Com.Hooniegit.AeronCSharpTools.Tools.TagData;

var location = "aeron-sbe-ipc"; // %temp 폴더의 Media Driver 폴더 경로 (임의 지정 가능)
var streamId = 10; // Stream ID (임의 지정 가능)


DataPublisher publisher = new DataPublisher(aeronDir:location, streamId:streamId);

publisher.PublishSingleDataMessage(id: 1, value: "Hello, Aeron!", timestamp: DateTime.UtcNow.ToString("o"));

publisher.PublishListDataMessage(new List<(int id, double value)> { (1, 3.14), (2, 2.718) }, timestamp: DateTime.UtcNow.ToString("o"));

```
3. client (Subscriber)
```cs
using Com.Hooniegit.AeronCSharpTools.Tools.DataSubscriber;

byte[] sharedBuffer = new byte[1024 * 1024]; // 어플리케이션이 사용할 공유 메모리
int delay = 3000; // Connect() 재시도 delay
bool keepRunning = true; // false 설정 시 반복문 종료

string location = "aeron-sbe-ipc"; // %temp 폴더의 Media Driver 폴더 경로 (임의 지정 가능)
int streamId = 10; // Stream ID (Publisher와 동일하게 설정)

while (keepRunning)
{
    try
    {
        using (var subscriber = new DataSubscriber(streamId: 10, sharedBuffer: sharedBuffer))
        {
            // 데이터 수신 콜백 등록
            subscriber.OnListDataReceived = (data) =>
            {
                var entries = data.Entries;
                int entryCount = 0;

                while (entries.HasNext)
                {
                    entries.Next();
                    entryCount++;
                    
                    int id = entries.Id;
                    double value = entries.Value;
                    
                    // 이하에서 데이터 처리
                }
                string timestamp = data.GetTimestamp();
                
                // 이하에서 데이터 처리
            };

            // Media Driver 연결 해제 시 동작
            subscriber.OnDisconnected = () =>
            {
                subscriber.Stop(); // _isRunning = false
            };

            // 연결 및 데이터 수신
            subscriber.Connect();
            subscriber.Start();
        }
    }
    // Connect() 실패 시 동작
    catch (Exception ex)
    {
        // 예외 로그 출력
    }

    // Time Delay
    Thread.Sleep(delay);
}
```
### Version
- v0.1.0: Initial Release / Publisher 및 Subscriber 클래스 구현