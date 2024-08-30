package org.example.debeziumapp2.util;

import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class SqlGenerator {

    public String generateSelectSql(String tableName, Map<String, String> fieldAndValue) {
        String baseSelectSql = "SELECT * FROM " + tableName + " WHERE ";
        int size = fieldAndValue.size();
        int counter = 0;
        for (Map.Entry<String, String> entry : fieldAndValue.entrySet()) {
            if (entry.getValue().startsWith("\"") && entry.getValue().endsWith("\"")) {
                entry.setValue(entry.getValue().replace('\"', '\''));
            }
            baseSelectSql += entry.getKey() + " = " + entry.getValue();
            if (counter < size - 1) {
                baseSelectSql += " AND ";
            }
            counter++;
        }
        baseSelectSql += ";";

        return baseSelectSql;
    }
}
