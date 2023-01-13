package com.lishoupeng.calcite.medium.cost;

import com.google.common.collect.ImmutableList;
import org.apache.calcite.rel.metadata.ChainedRelMetadataProvider;
import org.apache.calcite.rel.metadata.RelMetadataProvider;

public class DefaultRelMetadataProvider {

    public static RelMetadataProvider getMetadataProvider() {
        return ChainedRelMetadataProvider.of(
                ImmutableList.of(
                        CSVRelMdRowCount.SOURCE,
                        CSVRelMdDistinctRowCount.SOURCE,
                        org.apache.calcite.rel.metadata.DefaultRelMetadataProvider.INSTANCE
                )
        );
    }
}
