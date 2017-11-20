package org.wso2.carbon.analytics.common.data.provider.rdbms;

import com.google.gson.Gson;
import org.wso2.carbon.analytics.common.data.provider.rdbms.config.RDBMSProviderConf;

/**
 * Created by sajithd on 11/20/17.
 */
public class Main {
    public static void main(String[] args) {
        String message = "{\n" +
                "\t\"dataSetMetadata\": {\n" +
                "\t\t\"names\": [\"column1\", \"column2\", \"column3\"],\n" +
                "\t\t\"types\": [\"LINEAR\", \"ORDINAL\", \"TIME\"]\n" +
                "\t}\n" +
                "}";
        RDBMSProviderConf conf = new Gson().fromJson(message, RDBMSProviderConf.class);
        System.out.println("myname");
    }
}
