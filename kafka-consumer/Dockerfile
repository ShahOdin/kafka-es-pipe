# Use the official SBT image
FROM mozilla/sbt

WORKDIR /root/build

COPY build.sbt /root/build/
COPY project/ /root/build/project/

COPY src/ /root/build/src/

RUN sbt compile

CMD ["sbt", "run"]