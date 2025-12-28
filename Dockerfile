FROM eclipse-temurin:11-jre-alpine

WORKDIR /app

COPY target/matching-engine-1.0.0.jar app.jar

EXPOSE $PORT

CMD java -XX:+UnlockExperimentalVMOptions -Xmx256m -jar app.jar