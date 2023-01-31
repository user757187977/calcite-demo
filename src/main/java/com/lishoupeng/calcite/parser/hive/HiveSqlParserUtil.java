package com.lishoupeng.calcite.parser.hive;

import com.lishoupeng.calcite.parser.Visitor;
import org.apache.calcite.config.Lex;
import org.apache.calcite.sql.SqlNode;
import org.apache.calcite.sql.parser.SqlParseException;
import org.apache.calcite.sql.parser.SqlParser;
import org.apache.calcite.sql.parser.impl.SqlParserImpl;
import org.apache.calcite.sql.validate.SqlConformanceEnum;
import org.apache.calcite.tools.FrameworkConfig;
import org.apache.calcite.tools.Frameworks;

/**
 * @Description 专门解析 HiveSQL，注意词法以及一致性。
 * @Author lishoupeng
 * @Date 2023/1/31 11:12
 */
public class HiveSqlParserUtil {

    public static Visitor parser(String sql) {
        Visitor visitor = new Visitor();
        FrameworkConfig frameworkConfig = Frameworks.newConfigBuilder().parserConfig(
                SqlParser.configBuilder()
                        .setLex(Lex.JAVA)
                        .setParserFactory(SqlParserImpl.FACTORY)
                        .setConformance(SqlConformanceEnum.LENIENT)
                        .build()
        ).build();
        SqlParser parser = SqlParser.create(sql, frameworkConfig.getParserConfig());
        try {
            SqlNode parsedSqlNode = parser.parseStmt();
            parsedSqlNode.accept(visitor);
        } catch (SqlParseException e) {
            e.printStackTrace();
        }
        return visitor;
    }
}
