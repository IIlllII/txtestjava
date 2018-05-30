package com.bitbreeds;

import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.sql.DataSource;
import javax.transaction.Transactional;
import javax.validation.Valid;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

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

@Service
@Validated
class ServiceOneImpl implements  ServiceOne{
    private final NamedParameterJdbcTemplate tmpl;
    private final ServiceTwo serviceTwo;

    private final Logger logger = LoggerFactory.getLogger(ServiceOneImpl.class);

    public ServiceOneImpl(DataSource ds, ServiceTwo serviceTwo) {
        this.tmpl = new NamedParameterJdbcTemplate(ds);
        this.serviceTwo = serviceTwo;
    }

    String insert = "INSERT INTO TB2 " +
            "(STUFF,NM) " +
            "VALUES " +
            "(:stuff, :nm)";

    @Transactional
    @Override
    public void insertStuff(Runnable runnable) {
        HashMap<String, String> args = new HashMap<>();
        args.put("stuff", "1234");
        args.put("nm", "23456");
        tmpl.update(insert, args);
        try {
            serviceTwo.insertStuff(runnable);
        } catch (Exception e) {
            logger.warn("We rollback, but continue",e);
        }
    }


    @Transactional
    @Override
    public void insertNestedChecked() {
        HashMap<String, String> args = new HashMap<>();
        args.put("stuff", "1234");
        args.put("nm", "23456");
        tmpl.update(insert, args);
        try {
            serviceTwo.insertChecked();
        } catch (Exception e) {
            logger.warn("We rollback, but continue",e);
        }
    }

    @Transactional
    @Override
    public void insertStuffSimple(Runnable runnable) {
        HashMap<String, String> args = new HashMap<>();
        args.put("stuff", "1234");
        args.put("nm", "23456");
        tmpl.update(insert, args);
        runnable.run();
    }

    @Transactional
    @Override
    public ReturnPojo insertAnnotated(String data) {
        HashMap<String, String> args = new HashMap<>();
        args.put("stuff", "1234");
        args.put("nm", data);
        tmpl.update(insert, args);
        return new ReturnPojo(data,"WAT");
    }

    @Transactional
    @Override
    @Valid
    public ReturnPojo insertAnnotatedNullInput( String data) {
        HashMap<String, String> args = new HashMap<>();
        args.put("stuff", "1234");
        args.put("nm", data);
        tmpl.update(insert, args);
        return new ReturnPojo(data,null);
    }

    @Transactional
    @Override
    @Valid
    public ReturnPojo insertPojo(ReturnPojo data) {
        HashMap<String, String> args = new HashMap<>();
        args.put("stuff", "1234");
        args.put("nm", data.getF2());
        tmpl.update(insert, args);
        return new ReturnPojo(data.getFa(),"asdasd");
    }


    @HystrixCommand(commandKey = "test",commandProperties =
            {
            @HystrixProperty(name = "circuitBreaker.forceOpen", value="true")
            })
    @Transactional
    @Override
    @Valid
    public ReturnPojo insertHystrix(ReturnPojo data) {
        HashMap<String, String> args = new HashMap<>();
        args.put("stuff", "1234");
        args.put("nm", data.getF2());
        tmpl.update(insert, args);
        return new ReturnPojo(data.getFa(),"asdasd");
    }

    @Transactional
    public List<String> getAll() {
        return tmpl.query("Select * from TB2", Collections.emptyMap(), (i, r) -> "Weee");
    }

}
