package org.wso2.carbon.analytics.common.data.provider.rdbms.bean;

import org.wso2.carbon.config.annotation.Configuration;
import org.wso2.carbon.config.annotation.Element;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by sajithd on 11/21/17.
 */
@Configuration(namespace = "wso2.rdbms.data.provider", description = "WSO2 RDBMS Data Provider Configuration object.")
public class RDBMSDataProviderConfBean {
    @Element(description = "Regex for sanitizing user provided sql SELECT query.")
    String sqlSelectQuerySanitizingRegex = null;

    @Element(description = "Regex for sanitizing user provided sql WHERE query.")
    String sqlWhereQuerySanitizingRegex = null;

    @Element(description = "Regex for sanitizing user provided table name.")
    String sqlTableNameSanitizingRegex = null;

    @Element(description = "Map for holding the sql purging queries template.")
    Map<String,String> purgingSQLQueryMap =  new HashMap<String, String>(){{
        put("MySQL_default", "DELETE FROM {{TABLE}} ORDER BY {{INCREMENTAL_COLUMN}} ASC LIMIT {{LIMIT_VALUE}}");
    }};

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
