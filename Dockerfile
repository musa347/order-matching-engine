FROM eclipse-temurin:11-jdk-alpine AS builder

WORKDIR /app

COPY src/ src/

RUN mkdir -p target/classes && \
    cd src && \
    find . -name "*.java" | xargs javac -d ../target/classes && \
    cd .. && \
    jar cfe target/matching-engine-1.0.0.jar orderbook.WebServer -C target/classes .

FROM eclipse-temurin:11-jre-alpine

WORKDIR /app

COPY --from=builder /app/target/matching-engine-1.0.0.jar app.jar

EXPOSE $PORT

CMD java -XX:+UnlockExperimentalVMOptions -Xmx256m -jar app.jar