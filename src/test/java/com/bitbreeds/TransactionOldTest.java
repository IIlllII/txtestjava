package com.bitbreeds;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.client.circuitbreaker.EnableCircuitBreaker;
import org.springframework.cloud.netflix.hystrix.EnableHystrix;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import javax.sql.DataSource;

import static org.junit.Assert.assertTrue;

/**
 * Copyright (c) 22/05/2018, Jonas Waage
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


@SpringBootTest(classes = {
        ServiceOne.class,
        ServiceTwo.class,
        ServiceOneImpl.class,
        ServiceTwoImpl.class,
        DataSourceInit.class,
        TestMethodA.class,
        TestMethodB.class})
@RunWith(SpringRunner.class)
@EnableTransactionManagement
@EnableCircuitBreaker
@EnableAspectJAutoProxy
@EnableHystrix
@EnableAutoConfiguration
public class TransactionOldTest {


    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    DataSource dataSource;

    @Autowired
    NamedParameterJdbcTemplate named;

    @Autowired
    TransactionTemplate tx;

    @Autowired
    TestMethodA tmA;

    @Autowired
    TestMethodB tmB;

    String createTB2 = "CREATE TABLE IF NOT EXISTS TB2 (\n" +
            "            STUFF varchar(40) NOT NULL,\n" +
            "            NM varchar(40) NOT NULL)";

    String createTB1 = "CREATE TABLE IF NOT EXISTS TB1 (\n" +
            "            OI varchar(40) NOT NULL,\n" +
            "            NOI varchar(40) NOT NULL)";

    String deleteTB1 = "DELETE FROM TB1";

    String deleteTB2 = "DELETE FROM TB2";



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

        tx.execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus transactionStatus) {
                tmA.insertStuff(() -> { });
                tmB.insertStuff(() -> { });
            }
        });

        assertTrue(!tmA.getAll().isEmpty());
        assertTrue(!tmB.getAll().isEmpty());
    }


    @Test
    public void testRollback() {

        try {
            tx.execute(new TransactionCallbackWithoutResult() {
                @Override
                protected void doInTransactionWithoutResult(TransactionStatus transactionStatus) {
                    tmA.insertStuff(() -> {
                    });
                    tmB.insertStuff(() -> {
                    });
                    throw new RuntimeException("Wat");
                }
            });
        } catch (RuntimeException e) {
            logger.info("Fine we are testing");
        }

        assertTrue(tmA.getAll().isEmpty());
        assertTrue(tmB.getAll().isEmpty());
    }


    @Test
    public void testNested() {

        try {
            tx.execute(new TransactionCallbackWithoutResult() {
                @Override
                protected void doInTransactionWithoutResult(TransactionStatus transactionStatus) {
                    tmB.insertStuff(() -> { });

                    try {

                        tmA.insertStuff(() -> {
                            try {
                                throw new Exception("Wat");
                            } catch (Exception e) {
                                throw new RuntimeException("Wrap",e);
                            }
                        });

                    }catch (RuntimeException e) {
                        logger.info("It is ok!",e);
                    }
                }
            });
        } catch (RuntimeException e) {
            logger.info("Fine we are testing",e);
        }

        assertTrue(tmA.getAll().isEmpty());
        assertTrue(tmB.getAll().isEmpty());
    }
}
