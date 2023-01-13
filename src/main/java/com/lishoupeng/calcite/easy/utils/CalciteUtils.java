package com.lishoupeng.calcite.easy.utils;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.calcite.adapter.enumerable.EnumerableRules;
import org.apache.calcite.config.CalciteConnectionConfigImpl;
import org.apache.calcite.jdbc.CalciteSchema;
import org.apache.calcite.plan.ConventionTraitDef;
import org.apache.calcite.plan.RelOptCluster;
import org.apache.calcite.plan.RelOptPlanner;
import org.apache.calcite.plan.RelOptTable;
import org.apache.calcite.plan.hep.HepPlanner;
import org.apache.calcite.plan.hep.HepProgramBuilder;
import org.apache.calcite.plan.volcano.VolcanoPlanner;
import org.apache.calcite.prepare.CalciteCatalogReader;
import org.apache.calcite.rel.RelDistributionTraitDef;
import org.apache.calcite.rel.RelNode;
import org.apache.calcite.rel.RelRoot;
import org.apache.calcite.rel.rules.FilterJoinRule;
import org.apache.calcite.rel.rules.PruneEmptyRules;
import org.apache.calcite.rel.rules.ReduceExpressionsRule;
import org.apache.calcite.rel.type.RelDataType;
import org.apache.calcite.rel.type.RelDataTypeFactory;
import org.apache.calcite.rel.type.RelDataTypeSystem;
import org.apache.calcite.rel.type.RelDataTypeSystemImpl;
import org.apache.calcite.rex.RexBuilder;
import org.apache.calcite.schema.SchemaPlus;
import org.apache.calcite.schema.impl.AbstractTable;
import org.apache.calcite.sql.SqlNode;
import org.apache.calcite.sql.fun.SqlStdOperatorTable;
import org.apache.calcite.sql.parser.SqlParseException;
import org.apache.calcite.sql.parser.SqlParser;
import org.apache.calcite.sql.type.BasicSqlType;
import org.apache.calcite.sql.type.SqlTypeFactoryImpl;
import org.apache.calcite.sql.type.SqlTypeName;
import org.apache.calcite.sql.validate.SqlConformanceEnum;
import org.apache.calcite.sql.validate.SqlValidator;
import org.apache.calcite.sql.validate.SqlValidatorUtil;
import org.apache.calcite.sql2rel.RelDecorrelator;
import org.apache.calcite.sql2rel.SqlToRelConverter;
import org.apache.calcite.tools.FrameworkConfig;
import org.apache.calcite.tools.Frameworks;
import org.apache.calcite.tools.RelBuilder;

import java.net.URL;
import java.net.URLDecoder;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;

public class CalciteUtils {

    private static final SchemaPlus schemaPlus = CalciteUtils.registerRootSchema();
    private static final SqlTypeFactoryImpl factory = new SqlTypeFactoryImpl(RelDataTypeSystem.DEFAULT);
    private static final CalciteCatalogReader calciteCatalogReader = new CalciteCatalogReader(
            CalciteSchema.from(schemaPlus),
            CalciteSchema.from(schemaPlus).path(null),
            factory,
            new CalciteConnectionConfigImpl(new Properties())
    );
    private static final FrameworkConfig frameworkConfig = Frameworks.newConfigBuilder()
            .parserConfig(SqlParser.Config.DEFAULT)
            .defaultSchema(schemaPlus)
            .traitDefs(ConventionTraitDef.INSTANCE, RelDistributionTraitDef.INSTANCE)
            .build();

    public static SchemaPlus registerRootSchema() {
        SchemaPlus rootSchema = Frameworks.createRootSchema(true);
        rootSchema.add("USERS", new AbstractTable() { //note: add a table
            @Override
            public RelDataType getRowType(final RelDataTypeFactory typeFactory) {
                RelDataTypeFactory.Builder builder = typeFactory.builder();

                builder.add("ID", new BasicSqlType(new RelDataTypeSystemImpl() {
                }, SqlTypeName.INTEGER));
                builder.add("NAME", new BasicSqlType(new RelDataTypeSystemImpl() {
                }, SqlTypeName.CHAR));
                builder.add("AGE", new BasicSqlType(new RelDataTypeSystemImpl() {
                }, SqlTypeName.INTEGER));
                return builder.build();
            }
        });

        rootSchema.add("JOBS", new AbstractTable() {
            @Override
            public RelDataType getRowType(final RelDataTypeFactory typeFactory) {
                RelDataTypeFactory.Builder builder = typeFactory.builder();
                builder.add("ID", new BasicSqlType(new RelDataTypeSystemImpl() {}, SqlTypeName.INTEGER));
                builder.add("NAME", new BasicSqlType(new RelDataTypeSystemImpl() {}, SqlTypeName.CHAR));
                builder.add("COMPANY", new BasicSqlType(new RelDataTypeSystemImpl() {}, SqlTypeName.CHAR));
                return builder.build();
            }
        });
        return rootSchema;
    }

    public static class ViewExpanderImpl implements RelOptTable.ViewExpander {
        public ViewExpanderImpl() {
            // TODO document why this constructor is empty
        }

        @Override
        public RelRoot expandView(RelDataType rowType, String queryString, List<String> schemaPath, List<String> viewPath) {
            System.out.println("expandView");
            return null;
        }
    }

    public static Connection getConnect(String filePath) {
        Connection connection = null;
        try {
            URL url = CalciteUtils.class.getResource(filePath);
            String str = URLDecoder.decode(Objects.requireNonNull(url).toString(), "UTF-8");
            Properties info = new Properties();
            info.put("model", str.replace("file:", ""));
            connection = DriverManager.getConnection("jdbc:calcite:", info);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return connection;
    }

    public static List<Map<String, Object>> getData(ResultSet resultSet) {
        List<Map<String, Object>> list = Lists.newArrayList();
        ResultSetMetaData metaData = null;
        int columnSize = 0;
        try {
            metaData = resultSet.getMetaData();
            columnSize = metaData.getColumnCount();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        while (true) {
            try {
                if (!resultSet.next()) break;
            } catch (SQLException e) {
                e.printStackTrace();
            }
            Map<String, Object> map = Maps.newLinkedHashMap();
            for (int i = 1; i < columnSize + 1; i++) {
                try{
                    map.put(metaData.getColumnLabel(i), resultSet.getObject(i));
                }catch (Exception e ){
                    System.out.println(i);
                }

            }
            list.add(map);
        }
        return list;
    }

    public static SqlValidator createValidator() {
        /*
          https://javadoc.io/doc/org.apache.calcite/calcite-core/1.18.0/org/apache/calcite/sql/validate/SqlValidatorUtil.html

          SqlOperatorTable defines a directory interface for enumerating and looking up SQL operators and functions. --查找 SQL 运算符和函数
          presenting the repository information of interest to the validator. --向验证者展示感兴趣的存储库信息
          RelDataTypeFactory is a factory for datatype descriptors. --数据类型描述符的工厂
          Enumeration of valid SQL compatibility modes. --枚举有效的 SQL 兼容模式
         */
        return SqlValidatorUtil.newValidator(
                SqlStdOperatorTable.instance(),
                calciteCatalogReader,
                new SqlTypeFactoryImpl(RelDataTypeSystem.DEFAULT),
                SqlConformanceEnum.DEFAULT
        );
    }

    public static HepPlanner createHepPlanner() {
        HepProgramBuilder hepProgramBuilder = new HepProgramBuilder();
        hepProgramBuilder.addRuleInstance(FilterJoinRule.FILTER_ON_JOIN);
        hepProgramBuilder.addRuleInstance(ReduceExpressionsRule.PROJECT_INSTANCE);
        hepProgramBuilder.addRuleInstance(PruneEmptyRules.PROJECT_INSTANCE);
        return new HepPlanner(hepProgramBuilder.build());
    }

    public static VolcanoPlanner createVolcanoPlanner() {
        // use VolcanoPlanner
        VolcanoPlanner planner = new VolcanoPlanner();
        planner.addRelTraitDef(ConventionTraitDef.INSTANCE);
        planner.addRelTraitDef(RelDistributionTraitDef.INSTANCE);
        // add rules
        planner.addRule(FilterJoinRule.FILTER_ON_JOIN);
        planner.addRule(ReduceExpressionsRule.PROJECT_INSTANCE);
        planner.addRule(PruneEmptyRules.PROJECT_INSTANCE);
        // add ConverterRule
        planner.addRule(EnumerableRules.ENUMERABLE_MERGE_JOIN_RULE);
        planner.addRule(EnumerableRules.ENUMERABLE_SORT_RULE);
        planner.addRule(EnumerableRules.ENUMERABLE_VALUES_RULE);
        planner.addRule(EnumerableRules.ENUMERABLE_PROJECT_RULE);
        planner.addRule(EnumerableRules.ENUMERABLE_FILTER_RULE);
        return planner;
    }

    public static SqlNode parse(String sql) throws SqlParseException {
        SqlParser parser = SqlParser.create(sql, SqlParser.Config.DEFAULT);
        return parser.parseStmt();
    }

    public static SqlNode validate(SqlNode sqlNode, SqlValidator validator) {
        return validator.validate(sqlNode);
    }

    public static RelNode sQLNode2RelNode(SqlNode sqlNode, SqlValidator sqlValidator, RelOptPlanner planner) {
        // 初始化 RexBuilder
        final RexBuilder rexBuilder = new RexBuilder(factory);
        // 初始化 RelOptCluster
        final RelOptCluster cluster = RelOptCluster.create(planner, rexBuilder);

        // init SqlToRelConverter config
        final SqlToRelConverter.Config config = SqlToRelConverter.configBuilder()
                .withConfig(frameworkConfig.getSqlToRelConverterConfig())
                .withTrimUnusedFields(false)
                .withConvertTableAccess(false)
                .build();
        // 初始化 SqlToRelConverter
        final SqlToRelConverter sqlToRelConverter = new SqlToRelConverter(
                new ViewExpanderImpl(),
                sqlValidator,
                calciteCatalogReader,
                cluster,
                frameworkConfig.getConvertletTable(),
                config
        );

        // ============重要============
        // SqlNode -> RelNode
        // ============结束============
        RelRoot relRoot = sqlToRelConverter.convertQuery(sqlNode, false, true);
        // 进行一次扁平化处理
        relRoot = relRoot.withRel(sqlToRelConverter.flattenTypes(relRoot.rel, true));
        final RelBuilder relBuilder = config.getRelBuilderFactory().create(cluster, null);
        relRoot = relRoot.withRel(RelDecorrelator.decorrelateQuery(relRoot.rel, relBuilder));
        return relRoot.rel;
    }

}
