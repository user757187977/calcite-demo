package com.lishoupeng.calcite.medium.ruleinstances;

import com.lishoupeng.calcite.medium.reloperators.CSVRel;
import com.lishoupeng.calcite.medium.reloperators.CSVTableScan;
import org.apache.calcite.plan.Convention;
import org.apache.calcite.plan.RelOptRuleCall;
import org.apache.calcite.plan.RelTrait;
import org.apache.calcite.plan.RelTraitSet;
import org.apache.calcite.rel.RelDistributionTraitDef;
import org.apache.calcite.rel.RelNode;
import org.apache.calcite.rel.convert.ConverterRule;
import org.apache.calcite.rel.logical.LogicalTableScan;

public class CSVTableScanConverter extends ConverterRule {

    public static final CSVTableScanConverter INSTANCE = new CSVTableScanConverter(
            LogicalTableScan.class,
            Convention.NONE,
            CSVRel.CONVENTION,
            "CSVTableScan"
    );

    public CSVTableScanConverter(Class<? extends RelNode> clazz, RelTrait in, RelTrait out, String description) {
        super(clazz, in, out, description);
    }

    @Override
    public boolean matches(RelOptRuleCall call) {
        return super.matches(call);
    }

    @Override
    public RelNode convert(RelNode rel) {
        LogicalTableScan tableScan = (LogicalTableScan) rel;
        return new CSVTableScan(tableScan.getCluster(),
                RelTraitSet.createEmpty().plus(CSVRel.CONVENTION).plus(RelDistributionTraitDef.INSTANCE.getDefault()),
                tableScan.getTable());
    }
}
