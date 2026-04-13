# AeronTools
Aeron 패키지 기반의 단순한 데이터 통신 라이브러리입니다.

### About Aeron
<b>Aeron</b>은 고성능, 저지연, 신뢰성 있는 메시징 시스템을 구축하기 위한 라이브러리입니다. 금융 거래 시스템과 같이 초저지연이 요구되는 환경에서 널리 사용됩니다.
- <b>IPC(Inter-Process Communication) 통신</b>: 동일한 머신 내의 프로세스 간 통신을 위해 설계된 IPC 채널을 활용하여, 네트워크 스택을 우회하고 OS 개입 없이 데이터를 교환합니다.
- <b>UDP 통신</b>: 네트워크를 통한 통신에서는 UDP 프로토콜을 사용하여, TCP의 연결 지향적 특성 대신 빠른 데이터 전송과 낮은 오버헤드를 제공합니다.
```
# IPC 통신 (Localhost)
[ Client: Producer ] --> [ Server: Media Driver] --> [ Client: Consumer ]

# UDP 통신
[ Client: Producer ] --> [ Server: Media Driver] ==(UDP)==> [ Server: Media Driver] --> [ Client: Consumer ]
```
- <b>메모리 맵 파일(Memory-Mapped File) 사용</b>: 송신자와 수신자가 OS 개입 없이 동일한 물리적 메모리 주소를 공유합니다. 커널을 경유하지 않아 Zero-Copy 전송이 가능합니다(컨텍스트 스위칭 비용 0).
- <b>CPU 하드웨어 기능 활용</b>: 원자적 연산(Atomic Operations), CAS(Compare-And-Swap), 그리고 메모리 배리어(Memory Barriers) 등을 활용하여 스레드간의 블로킹이 없는 Lock-Free 알고리즘을 구현합니다.
- <b>CPU 캐시 라인 Padding</b>: 패딩을 적용하여 'False Sharing'에 의한 병목을 방지하고 멀티 코어 CPU 캐시 히트율을 극대화합니다.

### Information
- 해당 패키지는 이하의 <b>'고정된 규격'</b>을 가진 데이터를 통신하기 위한 라이브러리입니다.
``` text
# 단일 Tag 데이터
Single Data: { "timestamp": string, "id": int, "value": string }

# 리스트 Tag 데이터 (공통 Timestamp 정보 공유)
List Data: { "timestamp": string, data": [ { "id": int, "value": double }, {...}, ... ] }
```
- <b>.NET Framework(v4.8)</b> 및 <b>Java(JDK17)</b> 기반으로 개발되었습니다.
