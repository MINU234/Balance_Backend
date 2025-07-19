# 1. Java 17을 포함한 기본 이미지를 사용합니다.
FROM openjdk:17-jdk-slim

# 2. 빌드된 Jar 파일이 저장될 인자를 선언합니다.
ARG JAR_FILE=build/libs/*.jar

# 3. Jar 파일을 컨테이너의 app.jar로 복사합니다.
COPY ${JAR_FILE} app.jar

# 4. 애플리케이션 실행 시 사용할 포트를 노출합니다.
EXPOSE 8080

# 5. 컨테이너가 시작될 때 app.jar를 실행하는 명령입니다.
ENTRYPOINT ["java","-jar","/app.jar"]
