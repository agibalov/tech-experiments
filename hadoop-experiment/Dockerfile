FROM sequenceiq/hadoop-docker:2.7.1
ADD ./build/libs/hadoop-experiment-1.0-SNAPSHOT.jar /test/app.jar

CMD /etc/bootstrap.sh && \
    $HADOOP_PREFIX/bin/hdfs dfsadmin -safemode leave && \
    $HADOOP_PREFIX/bin/hadoop fs -mkdir /user/test && \
    echo 'Hello World Bye World' > file00 && \
    $HADOOP_PREFIX/bin/hadoop fs -put file00 /user/test/file00 && \
    $HADOOP_PREFIX/bin/hadoop jar /test/app.jar me.loki2302.App /user/test /user/out && \
    $HADOOP_PREFIX/bin/hadoop fs -get /user/out/part-r-00000 out.txt && \
    cat out.txt && \
    echo 'DONE!'
