package com.lishoupeng.calcite.medium.schema;

import org.apache.calcite.linq4j.Enumerator;
import org.apache.calcite.util.Source;

import java.io.BufferedReader;
import java.io.IOException;

public class CsvEnumerator<E> implements Enumerator<E> {

    // current 包含了我们的行数据
    private E current;

    private BufferedReader br;

    public CsvEnumerator(Source source) {
        try {
            this.br = new BufferedReader(source.reader());
            // 跳过第一行, 因为我们第一行定义的是字段名字与字段类型, 所以手动触发一下;
            // 因为仅是 demo 实现得不规范, 给大家做个 badcase
            // TODO: use OpenCSV; VIA: https://www.baeldung.com/java-csv-file-array
            this.br.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * getCurrent.
     * @return
     */
    @Override
    public E current() {
        return current;
    }

    @Override
    public boolean moveNext() {
        try {
            String line = br.readLine();
            if (line == null) {
                return false;
            }
            current = (E) line.split(",");
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    @Override
    public void reset() {
        System.out.println("reset...");
    }

    @Override
    public void close() {
        System.out.println("close...");
    }
}
