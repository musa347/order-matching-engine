FROM eclipse-temurin:11-jdk-alpine AS builder

WORKDIR /app

COPY src/ src/
COPY build.sh .

RUN chmod +x build.sh && ./build.sh

FROM eclipse-temurin:11-jre-alpine

WORKDIR /app

COPY --from=builder /app/target/matching-engine-1.0.0.jar app.jar

EXPOSE $PORT

CMD java -XX:+UnlockExperimentalVMOptions -Xmx256m -jar app.jar