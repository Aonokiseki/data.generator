THIS_HOME="/home/zhaoyang/dataGenerator"
LIB_HOME="$THIS_HOME/lib"

#check generator.pid
if test -e generator.pid; then
    PID=$(cat generator.pid)
    if kill -0 $PID; then
        echo Process running with PID=$PID
        exit 1
    else
        echo Invalid pid file. Possible process was killed.
        rm -f generator.pid
    fi
fi

#start jvm
nohup java \
-Xms1g \
-Xmx1g \
-classpath "\
$LIB_HOME/accessors-smart-1.2.jar:\
$LIB_HOME/analyzer-1.0.jar:\
$LIB_HOME/animal-sniffer-annotations-1.17.jar:\
$LIB_HOME/ant-1.10.3.jar:\
$LIB_HOME/ant-launcher-1.10.3.jar:\
$LIB_HOME/aopalliance-1.0.jar:\
$LIB_HOME/asm-5.0.4.jar:\
$LIB_HOME/aspectjweaver-1.8.4.jar:\
$LIB_HOME/audience-annotations-0.5.0.jar:\
$LIB_HOME/avro-1.7.7.jar:\
$LIB_HOME/checker-qual-2.5.2.jar:\
$LIB_HOME/commons-beanutils-1.9.3.jar:\
$LIB_HOME/commons-cli-1.2.jar:\
$LIB_HOME/commons-codec-1.11.jar:\
$LIB_HOME/commons-collections-3.2.2.jar:\
$LIB_HOME/commons-compress-1.18.jar:\
$LIB_HOME/commons-configuration2-2.1.1.jar:\
$LIB_HOME/commons-daemon-1.0.13.jar:\
$LIB_HOME/commons-io-2.5.jar:\
$LIB_HOME/commons-lang3-3.7.jar:\
$LIB_HOME/commons-logging-1.1.3.jar:\
$LIB_HOME/commons-math3-3.1.1.jar:\
$LIB_HOME/commons-net-3.6.jar:\
$LIB_HOME/commons-text-1.4.jar:\
$LIB_HOME/curator-client-2.13.0.jar:\
$LIB_HOME/curator-framework-2.13.0.jar:\
$LIB_HOME/curator-recipes-2.13.0.jar:\
$LIB_HOME/dnsjava-2.1.7.jar:\
$LIB_HOME/error_prone_annotations-2.2.0.jar:\
$LIB_HOME/failureaccess-1.0.jar:\
$LIB_HOME/gson-2.8.6.jar:\
$LIB_HOME/guava-27.0-jre.jar:\
$LIB_HOME/guice-4.2.2-no_aop.jar:\
$LIB_HOME/hadoop-annotations-3.2.1.jar:\
$LIB_HOME/hadoop-auth-3.2.1.jar:\
$LIB_HOME/hadoop-client-3.2.1.jar:\
$LIB_HOME/hadoop-common-3.2.1.jar:\
$LIB_HOME/hadoop-hdfs-3.2.1.jar:\
$LIB_HOME/hadoop-hdfs-client-3.2.1.jar:\
$LIB_HOME/hadoop-mapreduce-client-common-3.2.1.jar:\
$LIB_HOME/hadoop-mapreduce-client-core-3.2.1.jar:\
$LIB_HOME/hadoop-mapreduce-client-jobclient-3.2.1.jar:\
$LIB_HOME/hadoop-yarn-api-3.2.1.jar:\
$LIB_HOME/hadoop-yarn-client-3.2.1.jar:\
$LIB_HOME/hadoop-yarn-common-3.2.1.jar:\
$LIB_HOME/hamcrest-core-1.3.jar:\
$LIB_HOME/htrace-core4-4.1.0-incubating.jar:\
$LIB_HOME/httpclient-4.5.6.jar:\
$LIB_HOME/httpcore-4.4.10.jar:\
$LIB_HOME/j2objc-annotations-1.1.jar:\
$LIB_HOME/jackson-annotations-2.9.0.jar:\
$LIB_HOME/jackson-core-2.9.8.jar:\
$LIB_HOME/jackson-core-asl-1.9.2.jar:\
$LIB_HOME/jackson-databind-2.9.8.jar:\
$LIB_HOME/jackson-jaxrs-1.9.2.jar:\
$LIB_HOME/jackson-jaxrs-base-2.9.8.jar:\
$LIB_HOME/jackson-jaxrs-json-provider-2.9.8.jar:\
$LIB_HOME/jackson-mapper-asl-1.9.2.jar:\
$LIB_HOME/jackson-module-jaxb-annotations-2.9.8.jar:\
$LIB_HOME/jackson-xc-1.9.2.jar:\
$LIB_HOME/javax.inject-1.jar:\
$LIB_HOME/javax.servlet-api-3.1.0.jar:\
$LIB_HOME/jaxb-api-2.2.11.jar:\
$LIB_HOME/jaxb-impl-2.2.3-1.jar:\
$LIB_HOME/jcip-annotations-1.0-1.jar:\
$LIB_HOME/jcommander-1.78.jar:\
$LIB_HOME/jersey-client-1.19.jar:\
$LIB_HOME/jersey-core-1.19.jar:\
$LIB_HOME/jersey-json-1.19.jar:\
$LIB_HOME/jersey-server-1.19.jar:\
$LIB_HOME/jersey-servlet-1.19.jar:\
$LIB_HOME/jettison-1.1.jar:\
$LIB_HOME/jetty-http-9.3.24.v20180605.jar:\
$LIB_HOME/jetty-io-9.3.24.v20180605.jar:\
$LIB_HOME/jetty-security-9.3.24.v20180605.jar:\
$LIB_HOME/jetty-server-9.3.24.v20180605.jar:\
$LIB_HOME/jetty-servlet-9.3.24.v20180605.jar:\
$LIB_HOME/jetty-util-9.3.24.v20180605.jar:\
$LIB_HOME/jetty-util-ajax-9.3.24.v20180605.jar:\
$LIB_HOME/jetty-webapp-9.3.24.v20180605.jar:\
$LIB_HOME/jetty-xml-9.3.24.v20180605.jar:\
$LIB_HOME/jline-0.9.94.jar:\
$LIB_HOME/jsch-0.1.54.jar:\
$LIB_HOME/json-smart-2.3.jar:\
$LIB_HOME/jsr305-3.0.0.jar:\
$LIB_HOME/jsr311-api-1.1.1.jar:\
$LIB_HOME/junit-4.12.jar:\
$LIB_HOME/kerb-admin-1.0.1.jar:\
$LIB_HOME/kerb-client-1.0.1.jar:\
$LIB_HOME/kerb-common-1.0.1.jar:\
$LIB_HOME/kerb-core-1.0.1.jar:\
$LIB_HOME/kerb-crypto-1.0.1.jar:\
$LIB_HOME/kerb-identity-1.0.1.jar:\
$LIB_HOME/kerb-server-1.0.1.jar:\
$LIB_HOME/kerb-simplekdc-1.0.1.jar:\
$LIB_HOME/kerb-util-1.0.1.jar:\
$LIB_HOME/kerby-asn1-1.0.1.jar:\
$LIB_HOME/kerby-config-1.0.1.jar:\
$LIB_HOME/kerby-pkix-1.0.1.jar:\
$LIB_HOME/kerby-util-1.0.1.jar:\
$LIB_HOME/kerby-xdr-1.0.1.jar:\
$LIB_HOME/leveldbjni-all-1.8.jar:\
$LIB_HOME/listenablefuture-9999.0-empty-to-avoid-conflict-with-guava.jar:\
$LIB_HOME/log4j-1.2.17.jar:\
$LIB_HOME/log4j-api-2.7.jar:\
$LIB_HOME/log4j-core-2.7.jar:\
$LIB_HOME/lucene-analyzers-common-8.7.0.jar:\
$LIB_HOME/lucene-analyzers-smartcn-8.7.0.jar:\
$LIB_HOME/lucene-core-8.7.0.jar:\
$LIB_HOME/netty-3.10.5.Final.jar:\
$LIB_HOME/netty-all-4.0.52.Final.jar:\
$LIB_HOME/nimbus-jose-jwt-4.41.1.jar:\
$LIB_HOME/okhttp-2.7.5.jar:\
$LIB_HOME/okio-1.6.0.jar:\
$LIB_HOME/paranamer-2.3.jar:\
$LIB_HOME/protobuf-java-2.5.0.jar:\
$LIB_HOME/re2j-1.1.jar:\
$LIB_HOME/slf4j-api-1.7.25.jar:\
$LIB_HOME/slf4j-log4j12-1.7.25.jar:\
$LIB_HOME/snakeyaml-1.21.jar:\
$LIB_HOME/snappy-java-1.0.5.jar:\
$LIB_HOME/spring-aop-4.3.9.RELEASE.jar:\
$LIB_HOME/spring-aspects-4.3.9.RELEASE.jar:\
$LIB_HOME/spring-beans-4.3.9.RELEASE.jar:\
$LIB_HOME/spring-context-4.3.9.RELEASE.jar:\
$LIB_HOME/spring-core-4.3.9.RELEASE.jar:\
$LIB_HOME/spring-expression-4.3.9.RELEASE.jar:\
$LIB_HOME/spring-web-4.3.9.RELEASE.jar:\
$LIB_HOME/stax2-api-3.1.4.jar:\
$LIB_HOME/testng-7.3.0.jar:\
$LIB_HOME/token-provider-1.0.1.jar:\
$LIB_HOME/woodstox-core-5.0.3.jar:\
$LIB_HOME/zookeeper-3.4.13.jar:\
$THIS_HOME/data.generator-0.0.1-SNAPSHOT.jar" \
priv.azure.miracle.data.generator.master.Entrance &
echo $! > ${THIS_HOME}/generator.pid
