package com.lishoupeng.calcite.medium;

import com.lishoupeng.calcite.easy.utils.CalciteUtils;
import com.lishoupeng.calcite.medium.cost.DefaultRelMetadataProvider;
import com.lishoupeng.calcite.medium.ruleInstances.CSVFilterConverter;
import com.lishoupeng.calcite.medium.ruleInstances.CSVNewProjectConverter;
import com.lishoupeng.calcite.medium.ruleInstances.CSVNewProjectRule;
import com.lishoupeng.calcite.medium.ruleInstances.CSVProjectConverter;
import com.lishoupeng.calcite.medium.ruleInstances.CSVTableScanConverter;
import com.lishoupeng.calcite.medium.utils.Utils;
import org.apache.calcite.plan.RelOptUtil;
import org.apache.calcite.rel.RelNode;
import org.apache.calcite.rel.RelRoot;
import org.apache.calcite.sql.SqlExplainLevel;

import java.sql.Connection;
import java.util.Objects;

public class CboTest {

    private static final String SQL = "select * from DEMO.TEST01 where TEST01.NAME1='hello'";
    private static final String FILE_PATH = "/model.json";
    private static final Connection connection = CalciteUtils.getConnect(FILE_PATH);

    public static void main(String[] args) {

        RelRoot root = Utils.sql2RelRoot(connection, SQL);
        System.out.println("----------------- before optimizer ------------------");
        System.out.println(RelOptUtil.toString(Objects.requireNonNull(root).rel, SqlExplainLevel.ALL_ATTRIBUTES));

        RelNode rel = Utils.rboOptimization(
                root.rel,
                DefaultRelMetadataProvider.getMetadataProvider(),
                CSVTableScanConverter.INSTANCE,
                CSVFilterConverter.INSTANCE,
                CSVProjectConverter.INSTANCE,
                CSVNewProjectConverter.INSTANCE
        );

        System.out.println("----------------- after RBO optimizer ------------------");
        System.out.println(RelOptUtil.toString(rel, SqlExplainLevel.ALL_ATTRIBUTES));

        //这里的 rule 是替换 CsvProject 为 NewCsvProject，是否替换会根据 cumulative cost 的信息，谁的小就替换谁的
        //我直接在对应的 rel 里面写死了返回的 cost 信息（rows:10,cpu:10,io:0），如果调高一点（高过 CsvProject 的定义），那么是不会替换的
        rel = Utils.cboOptimization(rel, CSVNewProjectRule.INSTANCE);
        System.out.println("----------------- after CBO optimizer ------------------");
        System.out.println(RelOptUtil.toString(rel, SqlExplainLevel.ALL_ATTRIBUTES));
    }


}
