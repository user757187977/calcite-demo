package com.lishoupeng.calcite.easy;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.lishoupeng.calcite.easy.utils.CalciteUtils;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * @Description CsvTest.
 * @Author lishoupeng
 * @Date 2023/1/10 10:55
 */
public class CsvTest {

    private static final String FILE_PATH = "/model.json";
    private static final Connection connection = CalciteUtils.getConnect(FILE_PATH);

    public static void main(String[] args) {
        try (Statement statement = connection.createStatement()) {
            String[] strArray = {
                    "select * from DEMO.JOB"
                    , "select * from DEMO.INSTANCE"
                    , "select * from DEMO.INSTANCE i left join DEMO.JOB j on i.JOBID = j.ID"
            };
            for (String sql : strArray) {
                ResultSet resultSet = statement.executeQuery(sql);
                System.out.println("-------------------------  " + "start sql" + "  -------------------------  ");
                System.out.println(JSON.toJSONString(CalciteUtils.getData(resultSet), SerializerFeature.PrettyFormat));
                System.out.println("-------------------------  " + "end sql" + "  -------------------------  ");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
