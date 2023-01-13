package com.lishoupeng.calcite.easy;

import com.lishoupeng.calcite.easy.utils.CalciteUtils;
import com.lishoupeng.calcite.easy.visitor.Visitor;
import lombok.Data;
import org.apache.calcite.plan.RelOptUtil;
import org.apache.calcite.plan.hep.HepPlanner;
import org.apache.calcite.rel.RelNode;
import org.apache.calcite.sql.SqlExplainLevel;
import org.apache.calcite.sql.SqlNode;
import org.apache.calcite.sql.parser.SqlParseException;
import org.apache.calcite.sql.validate.SqlValidator;

/**
 * @Description RBOTest.
 * @Author lishoupeng
 * @Date 2022/12/28 16:25
 */
@Data
public class RBOTest {

    public static RelNode rBoRelNodeFindBestExp(RelNode relNode, HepPlanner planner) {
        // 构建图
        planner.setRoot(relNode);
        // 查找最佳解析表达式
        return planner.findBestExp();
    }

    public static void main(String[] args) throws SqlParseException {
        // users 表的字段叫 id, 可以特意写成 ids, 触发下 main.java.demo.calcite.CBOTest.validate() 的报错
        String sql = "select u.id as user_id, u.name as user_name, j.company as user_company, u.age as user_age " +
                "from users u join jobs j on u.id=j.id " +
                "where u.age > 30 and j.id > 10 " +
                "order by user_id";

        SqlNode parsedSqlNode = CalciteUtils.parse(sql);
        Visitor visitor = new Visitor();
        parsedSqlNode.accept(visitor);
        System.out.println("Parser 之后的 SqlNode: " + parsedSqlNode);
        System.out.println("The raw sql: " + sql);
        System.out.println("------------------------");
        System.out.println("orderBy: " + visitor.getOrderByColumnNames());
        System.out.println("select: " + visitor.getSelectColumnNames());
        System.out.println("tables: " + visitor.getSelectTableNames());
        System.out.println("where: " + visitor.getWhereColumnNames());

        SqlValidator sqlValidator = CalciteUtils.createValidator();
        SqlNode validatedSqlNode = CalciteUtils.validate(parsedSqlNode, sqlValidator);
        System.out.println("验证之后的 SqlNode: " + validatedSqlNode.toString());

        HepPlanner hepPlanner = CalciteUtils.createHepPlanner();
        RelNode relNode = CalciteUtils.sQLNode2RelNode(validatedSqlNode, sqlValidator, hepPlanner);
        System.out.println("RelNode:");
        System.out.println(RelOptUtil.toString(relNode, SqlExplainLevel.ALL_ATTRIBUTES));

        RelNode rBoBestExpRelNode = rBoRelNodeFindBestExp(relNode, hepPlanner);
        System.out.println("RBO 优化后:");
        System.out.println(RelOptUtil.toString(rBoBestExpRelNode, SqlExplainLevel.ALL_ATTRIBUTES));

    }
}
