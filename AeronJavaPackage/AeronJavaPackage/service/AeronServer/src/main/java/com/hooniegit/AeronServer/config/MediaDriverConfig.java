package com.hooniegit.AeronServer.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * config.ini 파일의 미디어 드라이버 관련 설정을 참조합니다.
 */
@Component
@Getter @Setter
@ConfigurationProperties(prefix = "aeron.server")
public class MediaDriverConfig {

    private String location;

}
