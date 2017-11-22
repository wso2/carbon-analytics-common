/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.analytics.common.data.provider.rdbms.bean;

import org.wso2.carbon.config.annotation.Configuration;
import org.wso2.carbon.config.annotation.Element;

import java.util.HashMap;
import java.util.Map;

/**
 * RDBMS data provider configuration bean.
 */
@Configuration(namespace = "wso2.rdbms.data.provider", description = "WSO2 RDBMS Data Provider Configuration object.")
public class RDBMSDataProviderConfBean {
    @Element(description = "Regex for sanitizing user provided sql SELECT query.")
    String sqlSelectQuerySanitizingRegex = null;

    @Element(description = "Regex for sanitizing user provided sql WHERE query.")
    String sqlWhereQuerySanitizingRegex = null;

    @Element(description = "Regex for sanitizing user provided table name.")
    String sqlTableNameSanitizingRegex = null;

    @Element(description = "Map for the sql purging queries template.")
    Map<String, String> purgingSQLQueryMap = new HashMap<String, String>() { {
        put("MySQL_default", "DELETE FROM {{TABLE_NAME}} ORDER BY {{INCREMENTAL_COLUMN}} ASC LIMIT {{LIMIT_VALUE}}");
        put("H2_default", "DELETE FROM {{TABLE_NAME}} ORDER BY {{INCREMENTAL_COLUMN}} ASC LIMIT {{LIMIT_VALUE}}");
    } };

    @Element(description = "Map for the total record count sql queries template.")
    Map<String, String> totalRecordCountSQLQueryMap = new HashMap<String, String>() { {
        put("MySQL_default", "SELECT COUNT(*) FROM {{TABLE_NAME}}");
        put("H2_default", "SELECT COUNT(*) FROM {{TABLE_NAME}}");
    } };

    @Element(description = "Map for order by and limit sql queries template.")
    Map<String, String> recordLimitSQLQueryMap = new HashMap<String, String>() { {
        put("MySQL_default", " ORDER BY {{INCREMENTAL_COLUMN}} ASC LIMIT {{LIMIT_VALUE}}");
        put("H2_default", " ORDER BY {{INCREMENTAL_COLUMN}} ASC LIMIT {{LIMIT_VALUE}}");
    } };

    @Element(description = "Map for greater than where clause sql queries template.")
    Map<String, String> greaterThanWhereSQLQueryMap = new HashMap<String, String>() { {
        put("MySQL_default", " WHERE {{INCREMENTAL_COLUMN}} > {{LAST_RECORD_VALUE}}");
        put("H2_default", " WHERE {{INCREMENTAL_COLUMN}} > {{LAST_RECORD_VALUE}}");
    } };

    @Element(description = "Array of linear column types in the database.")
    String[] linearTypes = new String[]{"INTEGER", "INT", "SMALLINT", "TINYINT", "MEDIUMINT", "BIGINT",
            "DECIMAL", "NUMERIC", "FLOAT", "DOUBLE", "INT4", "SIGNED", "INT2", "YEAR", "BIGINT", "INT8",
            "IDENTITY", "NUMBER", "DEC", "PRECISION", "FLOAT8", "REAL", "FLOAT4", "NUMBER", "BINARY_FLOAT",
            "BINARY_DOUBLE"};

    @Element(description = "Array of ordinal column types in the database.")
    String[] ordinalTypes = new String[]{"CHAR", "VARCHAR", "BINARY", "VARBINARY", "BLOB", "TEXT", "ENUM", "SET",
            "LONGVARCHAR", "VARCHAR2", "NVARCHAR", "NVARCHAR2",
            "VARCHAR_CASESENSITIVE", "VARCHAR_IGNORECASE", "NCHAR", "CLOB", "TINYTEXT", "MEDIUMTEXT", "LONGTEXT",
            "NTEXT", "NCLOB"};

    @Element(description = "Array of time column types in the database.")
    String[] timeTypes = null;

    public String getSqlSelectQuerySanitizingRegex() {
        return sqlSelectQuerySanitizingRegex;
    }

    public String getSqlTableNameSanitizingRegex() {
        return sqlTableNameSanitizingRegex;
    }

    public String getSqlWhereQuerySanitizingRegex() {
        return sqlWhereQuerySanitizingRegex;
    }

    public Map<String, String> getPurgingSQLQueryMap() {
        return purgingSQLQueryMap;
    }

    public Map<String, String> getTotalRecordCountSQLQueryMap() {
        return totalRecordCountSQLQueryMap;
    }

    public Map<String, String> getRecordLimitSQLQueryMap() {
        return recordLimitSQLQueryMap;
    }

    public Map<String, String> getGreaterThanWhereSQLQueryMap() {
        return greaterThanWhereSQLQueryMap;
    }

    public String[] getLinearTypes() {
        return linearTypes;
    }

    public String[] getOrdinalTypes() {
        return ordinalTypes;
    }

    public String[] getTimeTypes() {
        return timeTypes;
    }
}
