package com.lishoupeng.calcite;

import com.lishoupeng.calcite.parser.Visitor;
import com.lishoupeng.calcite.parser.hive.HiveSqlParserUtil;

/**
 * @Description
 * @Author lishoupeng
 * @Date 2023/1/31 10:21
 */
public class Parser {
    public static void main(String[] args) {
        String sql = "with\n" +
                "  withA as (\n" +
                "    select * from tableA\n" +
                "    where whereC = '条件 3' and whereA != '条件 1' and whereB != '条件 2'\n" +
                "  ),\n" +
                "  \n" +
                "\n" +
                "\n" +
                "  withB as (\n" +
                "    select * from tableB\n" +
                "    where whereC = '条件 3' and whereA != '条件 1' and whereB != '条件 2'\n" +
                "\n" +
                "\n" +
                "\n" +
                "  )       select withA.*, withB.columnE, withB.columnF from withA\n" +
                "inner join withB\n" +
                "on withA.columnA = withB.columnA\n" +
                "and withA.columnB = withB.columnB\n" +
                "and withA.columnC = withB.columnC\n" +
                "and withA.columnD = withB.columnD";

        Visitor visitor = HiveSqlParserUtil.parser(sql);
        System.out.printf("with 结束位置，行: %s 列: %s %n", visitor.getWithFinishPos().left, visitor.getWithFinishPos().right);
    }
}
