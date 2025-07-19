//package Balance_Game.Balance_Game.common.config;
//
//import org.flywaydb.core.Flyway;
//import org.springframework.boot.autoconfigure.flyway.FlywayMigrationStrategy;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//
//@Configuration
//public class FlywayConfig {
//
//    @Bean
//    public FlywayMigrationStrategy flywayMigrationStrategy() {
//        return new FlywayMigrationStrategy() {
//            @Override
//            public void migrate(Flyway flyway) {
//                // repair를 먼저 실행하여 체크섬을 재설정하고, 그 다음 migrate를 실행합니다.
//                flyway.repair();
//                flyway.migrate();
//            }
//        };
//    }
//}
