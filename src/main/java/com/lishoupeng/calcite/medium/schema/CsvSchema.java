package com.lishoupeng.calcite.medium.schema;

import com.google.common.collect.ImmutableMap;
import com.google.common.io.Resources;
import org.apache.calcite.schema.Table;
import org.apache.calcite.schema.impl.AbstractSchema;
import org.apache.calcite.util.Source;
import org.apache.calcite.util.Sources;

import java.net.URL;
import java.util.Map;

public class CsvSchema extends AbstractSchema {

    // dataFiles 定义在 model.json 一个属性, 我们这里以为这表名.
    private final String dataFiles;

    public CsvSchema(String dataFile) {
        this.dataFiles = dataFile;
    }

    /**
     * 当调用 statement.executeQuery(sql); 时进入此方法
     * @return Map<String, Table>
     */
    @Override
    protected Map<String, Table> getTableMap() {
        final ImmutableMap.Builder<String, Table> builder = ImmutableMap.builder();
        for (String dataFile : dataFiles.split(",")) {
            URL url = Resources.getResource(dataFile);
            Source source = Sources.of(url);
            builder.put(dataFile.split("\\.")[0], new CsvTable(source));
        }
        return builder.build();
    }
}