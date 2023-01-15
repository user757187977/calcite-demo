package com.lishoupeng.calcite.medium.cost;

import org.apache.calcite.rel.RelNode;
import org.apache.calcite.rel.core.Filter;
import org.apache.calcite.rel.core.Project;
import org.apache.calcite.rel.metadata.BuiltInMetadata;
import org.apache.calcite.rel.metadata.MetadataDef;
import org.apache.calcite.rel.metadata.ReflectiveRelMetadataProvider;
import org.apache.calcite.rel.metadata.RelMdRowCount;
import org.apache.calcite.rel.metadata.RelMetadataProvider;
import org.apache.calcite.rel.metadata.RelMetadataQuery;
import org.apache.calcite.util.BuiltInMethod;

public class CSVRelMdRowCount extends RelMdRowCount {

    @Override
    public MetadataDef<BuiltInMetadata.RowCount> getDef() {
        return BuiltInMetadata.RowCount.DEF;
    }

    public static final RelMetadataProvider SOURCE = ReflectiveRelMetadataProvider.reflectiveSource(
            BuiltInMethod.ROW_COUNT.method,
            new CSVRelMdRowCount()
    );

    @Override
    public Double getRowCount(RelNode rel, RelMetadataQuery mq) {
        System.out.println("getRowCount RelNode");
        return 1.0;
    }

    @Override
    public Double getRowCount(Project rel, RelMetadataQuery mq) {
        System.out.println("getRowCount Project");
        return 2.0;
    }

    @Override
    public Double getRowCount(Filter rel, RelMetadataQuery mq) {
        System.out.println("getRowCount Filter");
        return mq.getRowCount(rel.getInput()) / 5;
    }

}
