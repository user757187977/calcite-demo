# calcite-demo

`An Apache-calcite demo, baseed on V1.18.0.`

1. 对 calcite 还不熟悉的, 可以先看我的[文章](https://github.com/user757187977/WorkMark/blob/master/src/mark/calcite.md)
2. 食用本 demo 请结合 [DOC](https://javadoc.io/doc/org.apache.calcite/calcite-core/1.18.0/overview-summary.html)
3. 此工程包含了两个 [package](./src/main/java/com/lishoupeng/calcite)
    * [easy](./src/main/java/com/lishoupeng/calcite/easy): 入门, 包含了 SQL parser, validate, optimization(RBO/CBO).
    * [medium](./src/main/java/com/lishoupeng/calcite/medium): 中级, 结合自定义 schema, cost, rules 实现完整的过程.
4. 建议从 easy 入门, 了解 calcite 处理一条 SQL 的过程:
    * **SQL**  -_Parser_->  **SqlNode**  -_validate_->  **SqlNode**  -_Rel(Rex)_->  **RelNode**  -_Optimization(
      HepPlanner/VolcanoPlanner)_->  **findBestExp**
5. 再进入到 medium 阶段, 开始接触自定义 schema(表信息), cost(成本), 优化规则(rules)