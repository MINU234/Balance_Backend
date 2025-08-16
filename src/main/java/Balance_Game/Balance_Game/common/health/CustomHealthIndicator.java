package Balance_Game.Balance_Game.common.health;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuator.health.Health;
import org.springframework.boot.actuator.health.HealthIndicator;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Statement;

@Slf4j
@Component
@RequiredArgsConstructor
public class CustomHealthIndicator implements HealthIndicator {
    
    private final DataSource dataSource;
    
    @Override
    public Health health() {
        try {
            // 데이터베이스 연결 체크
            boolean dbHealthy = checkDatabaseConnection();
            
            // 메모리 사용량 체크
            boolean memoryHealthy = checkMemoryUsage();
            
            // 디스크 공간 체크
            boolean diskHealthy = checkDiskSpace();
            
            if (dbHealthy && memoryHealthy && diskHealthy) {
                return Health.up()
                        .withDetail("database", "Connected")
                        .withDetail("memory", getMemoryInfo())
                        .withDetail("disk", getDiskInfo())
                        .withDetail("timestamp", System.currentTimeMillis())
                        .build();
            } else {
                return Health.down()
                        .withDetail("database", dbHealthy ? "Connected" : "Disconnected")
                        .withDetail("memory", memoryHealthy ? "OK" : "High usage")
                        .withDetail("disk", diskHealthy ? "OK" : "Low space")
                        .withDetail("timestamp", System.currentTimeMillis())
                        .build();
            }
        } catch (Exception e) {
            log.error("Health check failed", e);
            return Health.down()
                    .withDetail("error", e.getMessage())
                    .withDetail("timestamp", System.currentTimeMillis())
                    .build();
        }
    }
    
    private boolean checkDatabaseConnection() {
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            statement.execute("SELECT 1");
            return true;
        } catch (Exception e) {
            log.error("Database health check failed", e);
            return false;
        }
    }
    
    private boolean checkMemoryUsage() {
        Runtime runtime = Runtime.getRuntime();
        long maxMemory = runtime.maxMemory();
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long usedMemory = totalMemory - freeMemory;
        
        double usagePercentage = (double) usedMemory / maxMemory * 100;
        
        // 80% 이상 사용 시 경고
        return usagePercentage < 80.0;
    }
    
    private boolean checkDiskSpace() {
        try {
            long freeSpace = new java.io.File(".").getFreeSpace();
            long totalSpace = new java.io.File(".").getTotalSpace();
            
            double usagePercentage = (double) (totalSpace - freeSpace) / totalSpace * 100;
            
            // 90% 이상 사용 시 경고
            return usagePercentage < 90.0;
        } catch (Exception e) {
            log.warn("Disk space check failed", e);
            return true; // 체크 실패 시 정상으로 간주
        }
    }
    
    private String getMemoryInfo() {
        Runtime runtime = Runtime.getRuntime();
        long maxMemory = runtime.maxMemory();
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long usedMemory = totalMemory - freeMemory;
        
        return String.format("Used: %dMB, Free: %dMB, Max: %dMB", 
                usedMemory / 1024 / 1024, 
                freeMemory / 1024 / 1024, 
                maxMemory / 1024 / 1024);
    }
    
    private String getDiskInfo() {
        try {
            long freeSpace = new java.io.File(".").getFreeSpace();
            long totalSpace = new java.io.File(".").getTotalSpace();
            
            return String.format("Free: %dGB, Total: %dGB", 
                    freeSpace / 1024 / 1024 / 1024, 
                    totalSpace / 1024 / 1024 / 1024);
        } catch (Exception e) {
            return "Unknown";
        }
    }
}