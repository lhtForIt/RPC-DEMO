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
public interface TryMapper {

    @Select("select * from t_try_log where tx_no=#{tid}")
    boolean isExist(@Param("tid") String tid);

    @Insert("insert into t_try_log (tx_no) values (#{tid}) ")
    void addTry(@Param("tid") String tid);

}
