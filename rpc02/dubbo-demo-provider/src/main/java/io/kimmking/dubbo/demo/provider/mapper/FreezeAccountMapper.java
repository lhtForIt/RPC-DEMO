package io.kimmking.dubbo.demo.provider.mapper;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Repository;

/**
 * @author Leo liang
 * @Date 2022/3/15
 */
@Repository
public interface FreezeAccountMapper {

    //当account表处于try状态时像Freeze插入冻结的数据,way代表是加还是减，方便后续反向恢复，1代表+，2代表-
    @Insert("insert into t_bank_freeze (customer_id,account_type,balance,way,create_time) value (#{customerId},#{type},#{amount},#{way},now())")
    boolean initAccount(@Param("customerId") String customerId, @Param("type") int type, @Param("amount") int amount,@Param("way") int way);

    @Delete("delete from t_bank_freeze where customer_id=#{customerId} and account_type=#{type} and balance=#{amount}")
    boolean unfreezeAccount(@Param("customerId") String customerId, @Param("type") int type, @Param("amount") int amount);

    @Update("update t_bank_account set balance=(case when way=1 then balance-#{amount}" +
            "else balance+#{amount} end) where customer_id=#{customerId} and account_type=#{type} and balance=#{amount}")
    boolean revertFreezeAccount(@Param("customerId") String customerId, @Param("type") int type, @Param("amount") int amount, @Param("way") int way);
}
