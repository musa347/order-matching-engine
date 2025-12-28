FROM openjdk:11-jre-slim

WORKDIR /app

COPY target/matching-engine-1.0.0.jar app.jar

EXPOSE $PORT

CMD java -XX:+UnlockExperimentalVMOptions -Xmx256m -jar app.jar