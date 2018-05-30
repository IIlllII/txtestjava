package com.bitbreeds;

import com.arjuna.ats.jta.TransactionManager;
import org.apache.commons.dbcp2.managed.BasicManagedDataSource;
import org.h2.jdbcx.JdbcDataSource;

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
public class DataSources {

    /**
     * Create a datasource which pools XA connections
     */
    public static BasicManagedDataSource createDataSource(
            String url,
            String user,
            String pass) {
        JdbcDataSource ds = new JdbcDataSource();
        ds.setUrl(url);
        ds.setUser(user);
        ds.setPassword(pass);

        BasicManagedDataSource dataSource = new BasicManagedDataSource();
        dataSource.setXaDataSourceInstance(ds);
        dataSource.setMaxTotal(20);
        dataSource.setMinIdle(2);
        dataSource.setTransactionManager(TransactionManager.transactionManager());
        return dataSource;
    }
}
