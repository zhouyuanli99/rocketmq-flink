package test;

import com.alibaba.fastjson.JSONObject;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.ProcessFunction;
import org.apache.flink.util.Collector;
import org.apache.rocketmq.flink.MQSource;
import org.apache.rocketmq.flink.RocketMQConfig;
import org.apache.rocketmq.flink.RocketMQSink;
import org.apache.rocketmq.flink.RocketMQSource;
import org.apache.rocketmq.flink.common.selector.DefaultTopicSelector;
import org.apache.rocketmq.flink.common.serialization.SimpleKeyValueDeserializationSchema;
import org.apache.rocketmq.flink.common.serialization.SimpleKeyValueSerializationSchema;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class test1 {
    public static void main(String[] args) {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        // enable checkpoint

        Properties consumerProps = new Properties();
        consumerProps.setProperty(RocketMQConfig.NAME_SERVER_ADDR, "172.16.245.37:9876");
        consumerProps.setProperty(RocketMQConfig.CONSUMER_GROUP, "c002");
        consumerProps.setProperty(RocketMQConfig.CONSUMER_TOPIC, "2");
/*
        consumerProps.setProperty(RocketMQConfig.CONSUMER_OFFSET_RESET_TO, RocketMQConfig.CONSUMER_OFFSET_SITE);
        consumerProps.setProperty(RocketMQConfig.CONSUMER_OFFSET_SITE_STARTING_OFFSETS, "{\"2\":{\"iZbp1f9edjszup3fshxxheZ\":{\"0\":2,\"1\":3,\"2\":4,\"3\":3,\"4\":4,\"5\":3,\"6\":4,\"7\":5}}}");
*/
        consumerProps.setProperty(RocketMQConfig.CONSUMER_OFFSET_RESET_TO, RocketMQConfig.CONSUMER_OFFSET_EARLIEST);


        Properties producerProps = new Properties();
        producerProps.setProperty(RocketMQConfig.NAME_SERVER_ADDR, "172.16.245.37:9876");

        env.addSource(new MQSource(new SimpleKeyValueDeserializationSchema(), consumerProps))
                .name("rocketmq-source")
                .setParallelism(8)
                .process(new ProcessFunction<JSONObject, Map>() {
                    @Override
                    public void processElement(JSONObject in, Context ctx, Collector<Map> out) throws Exception {
                        HashMap result = new HashMap();
                        HashMap result2 = new HashMap();
                        in.remove("value");
                        System.err.println(in);
                        result.putAll(result2);
                        out.collect(result);
                    }
                })
                .name("upper-processor")
                .setParallelism(8)
                .addSink(new RocketMQSink(new SimpleKeyValueSerializationSchema("id", "province"),
                        new DefaultTopicSelector("flink-sink2"), producerProps).withBatchFlushOnCheckpoint(true))
                .name("rocketmq-sink")
                .setParallelism(8);

        try {
            env.execute("rocketmq-flink-example");
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
