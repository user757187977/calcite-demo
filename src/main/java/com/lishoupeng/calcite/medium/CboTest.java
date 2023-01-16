package com.lishoupeng.calcite.medium;

import com.lishoupeng.calcite.easy.utils.CalciteUtils;
import com.lishoupeng.calcite.medium.ruleinstances.CSVFilterConverter;
import com.lishoupeng.calcite.medium.ruleinstances.CSVNewProjectConverter;
import com.lishoupeng.calcite.medium.ruleinstances.CSVNewProjectRule;
import com.lishoupeng.calcite.medium.ruleinstances.CSVProjectConverter;
import com.lishoupeng.calcite.medium.ruleinstances.CSVTableScanConverter;
import com.lishoupeng.calcite.medium.utils.Utils;
import org.apache.calcite.plan.RelOptUtil;
import org.apache.calcite.rel.RelNode;
import org.apache.calcite.rel.RelRoot;
import org.apache.calcite.sql.SqlExplainLevel;

import java.sql.Connection;
import java.util.Objects;

public class CboTest {

    private static final String SQL = "select * from demo.job where name='jobName_1'";
    private static final String FILE_PATH = "/model.json";
    private static final Connection connection = CalciteUtils.getConnect(FILE_PATH);

    public static void main(String[] args) {

        RelRoot relRoot = Utils.sql2RelRoot(connection, SQL);
        System.out.println("----------------- before optimizer ------------------");
        System.out.println(RelOptUtil.toString(Objects.requireNonNull(relRoot).rel, SqlExplainLevel.ALL_ATTRIBUTES));

        RelNode relNode = Utils.rboOptimization(
                relRoot.rel,
                CSVTableScanConverter.INSTANCE,
                CSVFilterConverter.INSTANCE,
                CSVProjectConverter.INSTANCE,
                CSVNewProjectConverter.INSTANCE
        );

        System.out.println("----------------- after RBO optimizer ------------------");
        System.out.println(RelOptUtil.toString(relNode, SqlExplainLevel.ALL_ATTRIBUTES));

        //这里的 rule 是替换 CsvProject 为 NewCsvProject，是否替换会根据 cumulative cost 的信息，谁的小就替换谁的
        //我直接在对应的 rel 里面写死了返回的 cost 信息（rows:10,cpu:10,io:0），如果调高一点（高过 CsvProject 的定义），那么是不会替换的
        relNode = Utils.cboOptimization(relNode, CSVNewProjectRule.INSTANCE);
        System.out.println("----------------- after CBO optimizer ------------------");
        System.out.println(RelOptUtil.toString(relNode, SqlExplainLevel.ALL_ATTRIBUTES));
    }


}
