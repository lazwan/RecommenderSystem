package cn.edu.ahtcm.kafkastream;

import org.apache.kafka.streams.processor.Processor;
import org.apache.kafka.streams.processor.ProcessorContext;
import org.apache.log4j.Logger;

public class LogProcessor implements Processor<byte[], byte[]> {

    private final Logger logger = Logger.getLogger(this.getClass().getName());

    private ProcessorContext context;

    @Override
    public void init(ProcessorContext processorContext) {
        this.context = processorContext;
    }

    @Override
    public void process(byte[] dummy, byte[] line) {
        // 核心处理流程
        String input = new String(line);
        // 提取数据，以固定前缀过滤日志信息
        if (input.contains("PRODUCT_RATING:")) {
            input = input.split("PRODUCT_RATING:")[1].trim();
            logger.info("==== product rating coming! -> " + input + " ====");
            context.forward("logProcessr".getBytes(), input.getBytes());
        }
    }

    @Override
    public void punctuate(long l) {

    }

    @Override
    public void close() {

    }
}
