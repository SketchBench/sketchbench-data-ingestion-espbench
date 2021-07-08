FROM guenter/sbt_scala_java:latest AS builder
ENV BT_OPTS="-Xms1024M -Xmx2048M -Xss64M -XX:MaxMetaspaceSize=2048M"
COPY . /tmp/sketchbench-data-ingestion-espbench
WORKDIR /tmp/sketchbench-data-ingestion-espbench
RUN sbt assembly

FROM bitnami/java:1.8
WORKDIR /sketchbench-data-ingestion-espbench
COPY --from=builder /tmp/sketchbench-data-ingestion-espbench/tools/datasender/target/scala-2.11/DataSender-assembly-0.1.0-SNAPSHOT.jar .
CMD ["java", "-Xms1g", "-jar", "DataSender-assembly-0.1.0-SNAPSHOT.jar"]
