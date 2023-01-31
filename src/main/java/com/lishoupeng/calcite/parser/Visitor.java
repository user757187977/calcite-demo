package com.lishoupeng.calcite.parser;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.calcite.sql.SqlCall;
import org.apache.calcite.sql.SqlDataTypeSpec;
import org.apache.calcite.sql.SqlDynamicParam;
import org.apache.calcite.sql.SqlIdentifier;
import org.apache.calcite.sql.SqlIntervalQualifier;
import org.apache.calcite.sql.SqlJoin;
import org.apache.calcite.sql.SqlKind;
import org.apache.calcite.sql.SqlLiteral;
import org.apache.calcite.sql.SqlNode;
import org.apache.calcite.sql.SqlNodeList;
import org.apache.calcite.sql.SqlOrderBy;
import org.apache.calcite.sql.SqlSelect;
import org.apache.calcite.sql.SqlWith;
import org.apache.calcite.sql.parser.SqlParserPos;
import org.apache.calcite.sql.util.SqlBasicVisitor;
import org.apache.commons.lang3.tuple.ImmutablePair;

import java.util.ArrayList;
import java.util.List;

/**
 * @Description Visitor.
 * @Author lishoupeng
 * @Date 2022/12/28 15:17
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class Visitor extends SqlBasicVisitor<SqlNode> {

    List<String> selectTableNames = new ArrayList<>();
    List<String> selectColumnNames = new ArrayList<>();
    List<String> whereColumnNames = new ArrayList<>();
    List<String> orderByColumnNames = new ArrayList<>();
    // with 语句结束的位置，left: 行数；right: 列数
    ImmutablePair<Integer, Integer> withFinishPos = new ImmutablePair<>(0, 0);

    @Override
    public SqlNode visit(SqlLiteral literal) {
        return super.visit(literal);
    }

    @Override
    public SqlNode visit(SqlCall call) {
        switch (call.getKind()) {
            case SELECT:
                SqlSelect sqlSelect = (SqlSelect) call;
                for (SqlNode sqlNode : sqlSelect.getSelectList().getList()) {
                    selectColumnNames.add(sqlNode.toString());
                }
                SqlNode fromNode = sqlSelect.getFrom();
                if (SqlKind.JOIN.equals(fromNode.getKind())) {
                    SqlJoin sqlJoin = (SqlJoin) fromNode;
                    selectTableNames.add(sqlJoin.getLeft().toString());
                    selectTableNames.add(sqlJoin.getRight().toString());
                }
                break;
            case WITH:
                SqlParserPos sqlParserPos = ((SqlWith) call).body.getParserPosition();
                withFinishPos = new ImmutablePair<>(sqlParserPos.getLineNum(), sqlParserPos.getColumnNum());
                break;
            case ORDER_BY:
                SqlOrderBy sqlOrderBy = (SqlOrderBy) call;
                for (SqlNode sqlNode : sqlOrderBy.orderList.getList()) {
                    orderByColumnNames.add(sqlNode.toString());
                }
                break;
            default:
                break;
        }
        return super.visit(call);
    }

    @Override
    public SqlNode visit(SqlNodeList nodeList) {
        return super.visit(nodeList);
    }

    @Override
    public SqlNode visit(SqlIdentifier id) {
        return super.visit(id);
    }

    @Override
    public SqlNode visit(SqlDataTypeSpec type) {
        return super.visit(type);
    }

    @Override
    public SqlNode visit(SqlDynamicParam param) {
        return super.visit(param);
    }

    @Override
    public SqlNode visit(SqlIntervalQualifier intervalQualifier) {
        return super.visit(intervalQualifier);
    }

}
