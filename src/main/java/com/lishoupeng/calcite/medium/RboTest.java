package com.lishoupeng.calcite.medium;

import com.lishoupeng.calcite.easy.utils.CalciteUtils;
import com.lishoupeng.calcite.medium.cost.DefaultRelMetadataProvider;
import com.lishoupeng.calcite.medium.ruleInstances.CSVTableScanConverter;
import com.lishoupeng.calcite.medium.utils.Utils;
import org.apache.calcite.plan.RelOptUtil;
import org.apache.calcite.rel.RelNode;
import org.apache.calcite.rel.RelRoot;
import org.apache.calcite.rel.core.Filter;
import org.apache.calcite.rel.metadata.RelMetadataQuery;
import org.apache.calcite.sql.SqlExplainLevel;

import java.sql.Connection;

/**
 * 通过 MetadataProvider 的方式，并实现相关的 MetadataHandler，最终实现自己计算 cost 的逻辑
 * 不过需要将 RelNode 转换成自己实现的 RelNode，才能实现注入（比如CSVTableScan）
 */
public class RboTest {

    private static final String SQL = "select * from demo.job where name='hello'";
    private static final String FILE_PATH = "/model.json";
    private static final Connection connection = CalciteUtils.getConnect(FILE_PATH);

    public static void main(String[] args) {

        RelRoot relRoot = Utils.sql2RelRoot(connection, SQL);
        assert relRoot != null;

        System.out.println("----------------- before optimizer ------------------");
        System.out.println(RelOptUtil.toString(relRoot.rel, SqlExplainLevel.ALL_ATTRIBUTES));

        RelNode relNode = Utils.rboOptimization(
                relRoot.rel,
                DefaultRelMetadataProvider.getMetadataProvider(),
                CSVTableScanConverter.INSTANCE
        );

        System.out.println("----------------- after optimizer ------------------");
        /**
         * 这里修改了 TableScan 到 Filter 的 rowcount 的计算逻辑，
         * {@link com.lishoupeng.calcite.medium.cost.CSVRelMdRowCount#getRowCount(Filter rel, RelMetadataQuery mq) }
         */
        System.out.println(RelOptUtil.toString(relNode, SqlExplainLevel.ALL_ATTRIBUTES));
    }

}
