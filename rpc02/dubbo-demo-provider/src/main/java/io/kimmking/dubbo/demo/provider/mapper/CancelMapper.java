package io.kimmking.dubbo.demo.provider.mapper;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

/**
 * @author Leo liang
 * @Date 2022/3/16
 */
@Repository
public interface CancelMapper {

    @Select("select * from t_cancel_log where #{tid}")
    boolean isExist(@Param("tid") String tid);

    @Insert("insert into t_cancel_log (tx_no) values (#{tid}) ")
    void addCancel(@Param("tid") String tid);

}
