package com.kt_giga_fms.dtg.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Data
@Component
@ConfigurationProperties(prefix = "csv.data")
public class CsvDataConfig {
    
    private boolean enabled = true;
    private List<String> files;
    
    /**
     * 설정된 CSV 파일 목록을 반환
     */
    public List<String> getCsvFiles() {
        return files;
    }
    
    /**
     * CSV 데이터 사용 여부 확인
     */
    public boolean isEnabled() {
        return enabled;
    }
}
