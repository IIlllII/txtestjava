package com.bitbreeds; /**
 * Copyright (c) 27/05/2018, Jonas Waage
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and
 * to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

import org.apache.commons.dbcp2.managed.BasicManagedDataSource;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.client.circuitbreaker.EnableCircuitBreaker;
import org.springframework.cloud.netflix.hystrix.EnableHystrix;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.assertEquals;


@SpringBootConfiguration
@SpringBootTest
@ComponentScan(basePackages = "com.bitbreeds")
@RunWith(SpringRunner.class)
@EnableTransactionManagement
@EnableCircuitBreaker
@EnableAspectJAutoProxy
@EnableHystrix
@EnableAutoConfiguration
public class AnnotationTest {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    DataSource dataSource;

    @Autowired
    NamedParameterJdbcTemplate named;

    String createTB2 = "CREATE TABLE IF NOT EXISTS TB2 (\n" +
            "            STUFF varchar(40) NOT NULL,\n" +
            "            NM varchar(40) NOT NULL)";

    String createTB1 = "CREATE TABLE IF NOT EXISTS TB1 (\n" +
            "            OI varchar(40) NOT NULL,\n" +
            "            NOI varchar(40) NOT NULL)";

    String deleteTB1 = "DELETE FROM TB1";

    String deleteTB2 = "DELETE FROM TB2";

    @Autowired
    ServiceOne serviceOne;

    @Before
    public void setup() {
        logger.info("creating tables");
        named.getJdbcOperations().execute(createTB1);
        named.getJdbcOperations().execute(createTB2);
        logger.info("created tables");
    }

    @After
    public void tearDown() {
        named.getJdbcOperations().execute(deleteTB1);
        named.getJdbcOperations().execute(deleteTB2);
    }



    @Test
    public void testSingle() {
        serviceOne.insertStuff(()->{});
        assertEquals(Arrays.asList("Weee","Weee"),serviceOne.getAll());
    }

    @Test
    public void testSingleRollbackSimple() {
        try {
            serviceOne.insertStuffSimple(() -> {
                throw new RuntimeException("Ya");
            });
        }catch (RuntimeException e) {

        }
        assertEquals(Collections.emptyList(),serviceOne.getAll());
    }

    @Test
    public void testSingleSimple() {
        serviceOne.insertStuffSimple(()->{});
        assertEquals(Arrays.asList("Weee"),serviceOne.getAll());
    }

    @Test
    public void testNested() {
        try {
            serviceOne.insertStuff(() -> {
                throw new RuntimeException("WAT");
            });
        } catch (Exception e) {
            logger.error("war",e);
        }
        assertEquals(Collections.emptyList(), serviceOne.getAll());
    }


    @Test
    public void testNestedChecked() {
        try {
            serviceOne.insertNestedChecked();
        } catch (Exception e) {
            logger.error("war",e);
        }
        assertEquals(Arrays.asList("Weee","Weee"), serviceOne.getAll());
    }




    @Test
    public void testAnnotationOrder() {
        try {
            serviceOne.insertAnnotated("WAT");
        } catch (Exception e) {
            logger.error("Ok, since testing");
        }
        assertEquals(Arrays.asList("Weee"),serviceOne.getAll());
    }


    @Test
    public void testAnnotationOrderExpection() {
        try {
            serviceOne.insertAnnotated(null);
        } catch (Exception e) {
            logger.error("Ok, since testing",e);
        }
        assertEquals(Collections.emptyList(),serviceOne.getAll());
    }


    @Test
    public void testAnnotationOrderValidationOnReturn() {
        try {
            serviceOne.insertAnnotatedNullInput("wee");
        } catch (Exception e) {
            logger.error("Ok, since testing",e);
        }
        assertEquals(Collections.emptyList(),serviceOne.getAll());
    }


    @Test
    public void testAnnotationOrderValidationOnInput() {
        try {
            serviceOne.insertPojo(new ReturnPojo("asd",null));
        } catch (Exception e) {
            logger.error("Ok, since testing",e);
        }
        assertEquals(Collections.emptyList(),serviceOne.getAll());
    }

    @Test
    public void testAnnotationOrderHystrxInput() {
        try {
            serviceOne.insertHystrix(new ReturnPojo("asd","ajgh"));
        } catch (Exception e) {
            logger.error("Ok, since testing",e);
        }
        assertEquals(Collections.emptyList(),serviceOne.getAll());
    }
}
