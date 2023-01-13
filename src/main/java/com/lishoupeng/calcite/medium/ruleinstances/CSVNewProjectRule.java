package com.lishoupeng.calcite.medium.ruleinstances;

import com.lishoupeng.calcite.medium.reloperators.CSVProject;
import com.lishoupeng.calcite.medium.reloperators.CSVRel;
import com.lishoupeng.calcite.medium.reloperators.NewCsvProject;
import org.apache.calcite.plan.RelOptRule;
import org.apache.calcite.plan.RelOptRuleCall;
import org.apache.calcite.plan.RelTraitSet;
import org.apache.calcite.rel.RelDistributionTraitDef;
import org.apache.calcite.rel.core.Project;
import org.apache.calcite.rel.core.RelFactories;
import org.apache.calcite.rex.RexUtil;
import org.apache.calcite.tools.RelBuilderFactory;

public class CSVNewProjectRule extends RelOptRule {

    public static final CSVNewProjectRule INSTANCE = new CSVNewProjectRule(RelFactories.LOGICAL_BUILDER);

    /**
     * Creates a ProjectRemoveRule.
     *
     * @param relBuilderFactory Builder for relational expressions
     */
    public CSVNewProjectRule(RelBuilderFactory relBuilderFactory) {
        super(
                operandJ(Project.class, null, CSVNewProjectRule::isTrivial, any()),
                relBuilderFactory,
                null
        );
    }

    public void onMatch(RelOptRuleCall call) {
        Project project = call.rel(0);
        assert isTrivial(project);
        if (project instanceof CSVProject) {
            CSVProject csvProject = (CSVProject) project;
            NewCsvProject newCsvProject = new NewCsvProject(
                    csvProject.getCluster(),
                    RelTraitSet.createEmpty().plus(CSVRel.CONVENTION).plus(RelDistributionTraitDef.INSTANCE.getDefault()),
                    csvProject.getInput(),
                    csvProject.getProjects(),
                    csvProject.getRowType()
            );
            call.transformTo(newCsvProject);
        }
    }

    public static boolean isTrivial(Project project) {
        return RexUtil.isIdentity(project.getProjects(), project.getInput().getRowType());
    }
}
