package com.lishoupeng.calcite.rewrite;

import org.apache.calcite.sql.SqlIdentifier;
import org.apache.calcite.sql.SqlLiteral;
import org.apache.calcite.sql.SqlNode;
import org.apache.calcite.sql.SqlNodeList;
import org.apache.calcite.sql.SqlSelect;
import org.apache.calcite.sql.dialect.PrestoSqlDialect;
import org.apache.calcite.sql.fun.SqlStdOperatorTable;
import org.apache.calcite.sql.parser.SqlParseException;
import org.apache.calcite.sql.parser.SqlParser;
import org.apache.calcite.sql.parser.SqlParserPos;

import java.util.ArrayList;
import java.util.List;

public class CalciteExample {

    private static final SqlParserPos ZERO_POS = SqlParserPos.ZERO;

    public static void main(String[] args) throws SqlParseException {
        String originalSql = "SELECT SUM(a), b FROM table_name WHERE 1 = 1";
        SqlNode originalSqlParser = parseSql(originalSql);
        SqlSelect newQuery = modifyQuery(originalSqlParser);
        System.out.println(newQuery.toSqlString(PrestoSqlDialect.DEFAULT).getSql());
    }

    private static SqlNode parseSql(String sql) throws SqlParseException {
        SqlParser sqlParser = SqlParser.create(sql);
        return sqlParser.parseQuery();
    }

    private static SqlSelect modifyQuery(SqlNode originalQuery) {
        SqlSelect originalSelect = (SqlSelect) originalQuery;

        SqlNodeList selectList = originalSelect.getSelectList();
        SqlNode tableRef = originalSelect.getFrom();
        SqlNode originalCondition = originalSelect.getWhere();

        List<SqlNode> newConditions = createConditions(originalCondition);

        /**
         * 循环拼装查询条件
         * 注意📢：sql 中 where 部分，除了有查询条件（a=1，b=2）还有查询条件之间的拼接（and，or）所以在有了查询条件之后，还要将这些查询条件拼接
         * 而拼接，存在的问题就是，查询条件只能两两拼接，即：将 a=1 和 b=2 拼接一个 and
         * 而没有办法将多个查询条件拼接，即：a=1 b=2 c=3，是没有办法拼接的，因为这里需要两个拼接符，calcite 目前没有这样的方法封装
         * 所以，在拼接时候要注意，有些人拼接之后会丢掉结果，就是因为给了多个条件，但是只给了一个拼接
         */
        SqlNode sqlNode = null;
        for (SqlNode condition : newConditions) {
            if (sqlNode == null) {
                sqlNode = condition;
            } else {
                // 这里简化一些我们所有的查询条件的拼接都是通过 and 进行拼接的
                sqlNode = SqlStdOperatorTable.AND.createCall(ZERO_POS, sqlNode, condition);
            }
        }

        return new SqlSelect(
                originalSelect.getParserPosition(),
                null,
                selectList,
                tableRef,
                sqlNode,
                originalSelect.getGroup(),
                originalSelect.getHaving(),
                originalSelect.getWindowList(),
                originalSelect.getQualify(),
                originalSelect.getOrderList(),
                originalSelect.getOffset(),
                originalSelect.getFetch(),
                originalSelect.getHints()
        );
    }

    private static List<SqlNode> createConditions(SqlNode originalCondition) {
        List<SqlNode> conditions = new ArrayList<>();
        conditions.add(originalCondition);
        // 添加查询条件
        conditions.add(createCondition("column1", "value1"));
        conditions.add(createCondition("column2", "value2"));
        conditions.add(createCondition("column3", "value3"));

        List<SqlNode> sqlNodes = new ArrayList<SqlNode>() {{
            add(SqlLiteral.createExactNumeric("1", ZERO_POS));
            add(SqlLiteral.createCharString("2", ZERO_POS));
            add(SqlLiteral.createExactNumeric("3", ZERO_POS));
        }};
        conditions.add(createCondition("column4", new SqlNodeList(sqlNodes, ZERO_POS)));

        return conditions;
    }

    private static SqlNode createCondition(String columnName, String value) {
        return SqlStdOperatorTable.EQUALS.createCall(
                ZERO_POS,
                new SqlIdentifier(columnName, ZERO_POS),
                SqlLiteral.createCharString(value, ZERO_POS)
        );
    }

    private static SqlNode createCondition(String columnName, SqlNodeList sqlNodeList) {
        return SqlStdOperatorTable.NOT_IN.createCall(
                ZERO_POS,
                new SqlIdentifier(columnName, ZERO_POS),
                sqlNodeList
        );
    }

}