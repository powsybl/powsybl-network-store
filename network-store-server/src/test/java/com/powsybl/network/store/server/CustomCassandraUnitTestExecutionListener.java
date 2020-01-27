package com.powsybl.network.store.server;

import org.cassandraunit.CQLDataLoader;
import org.cassandraunit.dataset.cql.ClassPathCQLDataSet;
import org.cassandraunit.spring.CassandraDataSet;
import org.cassandraunit.spring.EmbeddedCassandra;
import org.cassandraunit.utils.EmbeddedCassandraServerHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.support.AbstractTestExecutionListener;
import org.springframework.util.ClassUtils;

import java.util.*;

public class CustomCassandraUnitTestExecutionListener extends AbstractTestExecutionListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(CustomCassandraUnitTestExecutionListener.class);
    private static boolean initialized = false;

    public CustomCassandraUnitTestExecutionListener() {
    }

    @Override
    public void beforeTestClass(TestContext testContext) throws Exception {
        this.startServer(testContext);
    }

    protected void startServer(TestContext testContext) throws Exception {
        EmbeddedCassandra embeddedCassandra = (EmbeddedCassandra) Objects.requireNonNull(AnnotationUtils.findAnnotation(testContext.getTestClass(), EmbeddedCassandra.class), "CassandraUnitTestExecutionListener must be used with @EmbeddedCassandra on " + testContext.getTestClass());
        String keyspace;
        if (!initialized) {
            String yamlFile = (String) Optional.ofNullable(embeddedCassandra.configuration()).get();
            keyspace = embeddedCassandra.tmpDir();
            long timeout = embeddedCassandra.timeout();
            EmbeddedCassandraServerHelper.startEmbeddedCassandra(yamlFile, keyspace, timeout);
            initialized = true;
        }

        CassandraDataSet cassandraDataSet = (CassandraDataSet) AnnotationUtils.findAnnotation(testContext.getTestClass(), CassandraDataSet.class);
        if (cassandraDataSet != null) {
            keyspace = cassandraDataSet.keyspace();
            List<String> dataset = this.dataSetLocations(testContext, cassandraDataSet);
            ListIterator<String> datasetIterator = dataset.listIterator();
            CQLDataLoader cqlDataLoader = new CQLDataLoader(EmbeddedCassandraServerHelper.getSession());

            while (datasetIterator.hasNext()) {
                String next = (String) datasetIterator.next();
                boolean dropAndCreateKeyspace = datasetIterator.previousIndex() == 0;
                cqlDataLoader.load(new ClassPathCQLDataSet(next, dropAndCreateKeyspace, dropAndCreateKeyspace, keyspace));
            }
        }
    }

    private List<String> dataSetLocations(TestContext testContext, CassandraDataSet cassandraDataSet) {
        String[] dataset = cassandraDataSet.value();
        if (dataset.length == 0) {
            String alternativePath = this.alternativePath(testContext.getTestClass(), true, cassandraDataSet.type().name());
            if (testContext.getApplicationContext().getResource(alternativePath).exists()) {
                dataset = new String[]{alternativePath.replace("classpath:/", "")};
            } else {
                alternativePath = this.alternativePath(testContext.getTestClass(), false, cassandraDataSet.type().name());
                if (testContext.getApplicationContext().getResource(alternativePath).exists()) {
                    dataset = new String[]{alternativePath.replace("classpath:/", "")};
                } else {
                    LOGGER.info("No dataset will be loaded");
                }
            }
        }

        return Arrays.asList(dataset);
    }

    public void afterTestMethod(TestContext testContext) throws Exception {
        LOGGER.debug("Cleaning server for test context [{}]", testContext);
        this.cleanData(testContext);
    }

    protected void cleanData(TestContext testContext) {
        CassandraDataSet cassandraDataSet = (CassandraDataSet) AnnotationUtils.findAnnotation(testContext.getTestClass(), CassandraDataSet.class);
        if (cassandraDataSet != null) {
            String keyspace = cassandraDataSet.keyspace();
            EmbeddedCassandraServerHelper.cleanDataEmbeddedCassandra(keyspace);
        }
    }

    @Override
    public void afterTestClass(TestContext testContext) throws Exception {
        this.stopServer();
    }

    protected void stopServer() {
        EmbeddedCassandraServerHelper.cleanEmbeddedCassandra();
    }

    protected String alternativePath(Class<?> clazz, boolean includedPackageName, String extension) {
        return includedPackageName ? "classpath:/" + ClassUtils.convertClassNameToResourcePath(clazz.getName()) + "-dataset." + extension : "classpath:/" + clazz.getSimpleName() + "-dataset." + extension;
    }

    public int getOrder() {
        return 0;
    }
}
