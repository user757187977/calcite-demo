package com.lishoupeng.calcite.medium.reloperators;

import org.apache.calcite.plan.RelOptCluster;
import org.apache.calcite.plan.RelOptCost;
import org.apache.calcite.plan.RelOptPlanner;
import org.apache.calcite.plan.RelOptTable;
import org.apache.calcite.plan.RelTraitSet;
import org.apache.calcite.rel.core.TableScan;
import org.apache.calcite.rel.metadata.RelMetadataQuery;

public class CSVTableScan extends TableScan implements CSVRel {

    private RelOptCost cost;

    /**
     * RelOptCluster：planner 运行时的环境，保存上下文信息
     * RelTrait：用来定义逻辑表的物理相关属性（physical property），三种主要的 trait 类型是：Convention、RelCollation、RelDistribution；
     * RelOpt：代表关系表
     */
    public CSVTableScan(RelOptCluster cluster, RelTraitSet traitSet, RelOptTable table) {
        super(cluster, traitSet, table);
    }

    @Override public double estimateRowCount(RelMetadataQuery mq) {
        return 50;
    }

    @Override
    public RelOptCost computeSelfCost(RelOptPlanner planner, RelMetadataQuery mq) {
        //return super.computeSelfCo(planner, mq);

        if (cost != null) {
            return cost;
        }
        //通过工厂生成 RelOptCost，注入自定义 cost 值并返回
        cost = planner.getCostFactory().makeCost(1, 1, 0);
        return cost;
    }

}
