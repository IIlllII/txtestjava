package com.bitbreeds;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import javax.sql.DataSource;
import javax.transaction.Transactional;
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
class ServiceTwoImpl implements ServiceTwo {
    private final NamedParameterJdbcTemplate tmpl;
    private final TransactionTemplate template;

    private final Logger logger = LoggerFactory.getLogger(ServiceTwoImpl.class);

    public ServiceTwoImpl(DataSource ds, PlatformTransactionManager platformTransactionManager) {
        this.template = new TransactionTemplate(platformTransactionManager);
        this.tmpl = new NamedParameterJdbcTemplate(ds);
    }

    String insert = "INSERT INTO TB2 " +
            "(STUFF,NM) " +
            "VALUES " +
            "(:stuff, :nm)";

    @Transactional
    public void insertStuff(Runnable runnable) {
        HashMap<String, String> args = new HashMap<>();
        args.put("stuff", "1234");
        args.put("nm", "23456");
        tmpl.update(insert, args);
        runnable.run();
    }

    @Transactional
    public void insertChecked() throws Exception {
        HashMap<String, String> args = new HashMap<>();
        args.put("stuff", "1234");
        args.put("nm", "23456");
        tmpl.update(insert, args);
        throw new Exception("WAT");
    }

    @Transactional
    public List<String> getAll() {
        return tmpl.query("Select * from TB2", Collections.emptyMap(), (i, r) -> "Weee");
    }
}
