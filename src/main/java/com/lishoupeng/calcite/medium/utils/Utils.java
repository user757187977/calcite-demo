package com.lishoupeng.calcite.medium.utils;

import com.google.common.collect.Lists;
import com.lishoupeng.calcite.medium.ruleinstances.CSVFilterConverter;
import com.lishoupeng.calcite.medium.ruleinstances.CSVProjectConverter;
import com.lishoupeng.calcite.medium.ruleinstances.CSVTableScanConverter;
import org.apache.calcite.config.Lex;
import org.apache.calcite.jdbc.CalcitePrepare;
import org.apache.calcite.plan.ConventionTraitDef;
import org.apache.calcite.plan.RelOptCluster;
import org.apache.calcite.plan.RelOptRule;
import org.apache.calcite.plan.hep.HepPlanner;
import org.apache.calcite.plan.hep.HepProgramBuilder;
import org.apache.calcite.plan.volcano.VolcanoPlanner;
import org.apache.calcite.rel.RelNode;
import org.apache.calcite.rel.RelRoot;
import org.apache.calcite.rel.metadata.CachingRelMetadataProvider;
import org.apache.calcite.rel.metadata.ChainedRelMetadataProvider;
import org.apache.calcite.rel.metadata.RelMetadataProvider;
import org.apache.calcite.rel.type.RelDataTypeFactory;
import org.apache.calcite.rex.RexBuilder;
import org.apache.calcite.server.CalciteServerStatement;
import org.apache.calcite.sql.SqlNode;
import org.apache.calcite.sql.parser.SqlParser;
import org.apache.calcite.sql.type.SqlTypeFactoryImpl;
import org.apache.calcite.tools.FrameworkConfig;
import org.apache.calcite.tools.Frameworks;
import org.apache.calcite.tools.Planner;

import java.sql.Connection;
import java.sql.Statement;
import java.util.List;

/**
 * @Description Utils
 * @Author lishoupeng
 * @Date 2023/1/13 10:41
 */
public class Utils {

    public static RelRoot sql2RelRoot(Connection connection, String sql) {
        try (Statement statement = connection.createStatement()) {
            CalciteServerStatement calciteServerStatement = statement.unwrap(CalciteServerStatement.class);
            CalcitePrepare.Context context = calciteServerStatement.createPrepareContext();
            final FrameworkConfig frameworkConfig = Frameworks.newConfigBuilder()
                    .parserConfig(SqlParser.configBuilder().setLex(Lex.MYSQL).build())
                    .defaultSchema(context.getRootSchema().plus())
//                .traitDefs(ConventionTraitDef.INSTANCE, RelDistributionTraitDef.INSTANCE)
                    .build();
            Planner planner = Frameworks.getPlanner(frameworkConfig);
            SqlNode parse = planner.parse(sql);
            SqlNode validate = planner.validate(parse);
            return planner.rel(validate);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static RelNode rboOptimization(
            RelNode basePlan,
            RelMetadataProvider relMetadataProvider,
            RelOptRule... relOptRules
    ) {
        HepProgramBuilder hepProgramBuilder = new HepProgramBuilder();
        for (RelOptRule relOptRule : relOptRules) {
            hepProgramBuilder.addRuleInstance(relOptRule);
        }
        HepPlanner hepPlanner = new HepPlanner(
                hepProgramBuilder.build(),
                basePlan.getCluster().getPlanner().getContext()
        );
        List<RelMetadataProvider> relMetadataProviders = Lists.newArrayList();
        relMetadataProviders.add(relMetadataProvider);
        hepPlanner.registerMetadataProviders(relMetadataProviders);
        RelMetadataProvider chainedProvider = ChainedRelMetadataProvider.of(relMetadataProviders);
        basePlan.getCluster().setMetadataProvider(new CachingRelMetadataProvider(chainedProvider, hepPlanner));
        hepPlanner.setRoot(basePlan);
        return hepPlanner.findBestExp();
    }

    public static RelNode cboOptimization(RelNode rel, RelOptRule... rules) {
        VolcanoPlanner planner = (VolcanoPlanner) rel.getCluster().getPlanner();
        //VolcanoPlanner 默认带有很多的优化 rule，其中有一个 ProjectRemoveRule 会消除掉 Project，故先 clear
        planner.clear();
        planner.addRelTraitDef(ConventionTraitDef.INSTANCE);
        for (RelOptRule r : rules) {
            planner.addRule(r);
        }

        // view between
//        planner.setRoot(rel);
//        return planner.findBestExp();

        RelOptCluster cluster = newCluster(planner);
        cluster.getPlanner().setRoot(rel);
        return planner.chooseDelegate().findBestExp();
    }

    static RelOptCluster newCluster(VolcanoPlanner planner) {
        final RelDataTypeFactory typeFactory = new SqlTypeFactoryImpl(org.apache.calcite.rel.type.RelDataTypeSystem.DEFAULT);
        return RelOptCluster.create(planner, new RexBuilder(typeFactory));
    }

    public static RelNode relNodeOptimization(RelNode relNode) {
        System.out.println("begin relNodeOptimization:");
        HepProgramBuilder hepProgramBuilder = new HepProgramBuilder();
        hepProgramBuilder.addRuleInstance(CSVTableScanConverter.INSTANCE);
        hepProgramBuilder.addRuleInstance(CSVFilterConverter.INSTANCE);
        hepProgramBuilder.addRuleInstance(CSVProjectConverter.INSTANCE);
        // 同时也可以仿照 FilterIntoJoinRule 这个类实现自己的优化 rule
        HepPlanner hepPlanner = new HepPlanner(hepProgramBuilder.build());
        hepPlanner.setRoot(relNode);
        relNode = hepPlanner.findBestExp();
        return relNode;
    }
}
