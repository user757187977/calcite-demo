# calcite-demo

* 2023-10-10 之前：`An Apache-calcite demo, baseed on V1.18.0.`
* 2023-10-10 更新：更新 calcite 版本从 1.18.0 -> 1.35.0

1. 对 calcite 还不熟悉的, 可以先移步我的另外一篇介绍[文章](https://github.com/user757187977/WorkMark/blob/master/src/mark/calcite/calcite.md)
2. 食用本 demo 请结合 [DOC](https://javadoc.io/doc/org.apache.calcite/calcite-core/1.18.0/overview-summary.html)
3. 此工程包含了三个 [package](./src/main/java/com/lishoupeng/calcite)
    * [easy](./src/main/java/com/lishoupeng/calcite/easy) 包含了：[select from csv](./src/main/java/com/lishoupeng/calcite/easy/CsvTest.java)、SQL parser、validate、optimization([RBO](./src/main/java/com/lishoupeng/calcite/easy/RBOTest.java)/[CBO](./src/main/java/com/lishoupeng/calcite/easy/CBOTest.java))。
    * [medium](./src/main/java/com/lishoupeng/calcite/medium)：中级，结合自定义 schema，cost，rules 实现完整的过程。
    * [rewrite](./src/main/java/com/lishoupeng/calcite/rewrite)：重写 sql 部分，翻了其他的仓库，没找到比较好的基于 calcite 的 rewrite 实现，可以参考下这个 demo。
4. 建议从 easy 入门，了解 calcite 处理一条 SQL 的过程：
    * **SQL**  -_Parser_->  **SqlNode**  -_validate_->  **SqlNode**  -_Rel(Rex)_->  **RelNode**  -_Optimization(
      HepPlanner/VolcanoPlanner)_->  **findBestExp**
5. 再进入到 medium 阶段，开始接触自定义 schema(表信息)，cost(成本)，优化规则(rules)