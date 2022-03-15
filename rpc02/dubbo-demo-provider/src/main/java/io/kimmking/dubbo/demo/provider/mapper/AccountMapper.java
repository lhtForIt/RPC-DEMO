package io.kimmking.dubbo.demo.provider.mapper;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Repository;

/**
 * @author Leo liang
 * @Date 2022/3/15
 */
@Repository
public interface AccountMapper {

    @Update("update t_bank_account set balance=(balance-#{amount}) where customer_id=#{customer_id} and account_type=#{type}")
    boolean subMoney(@Param("customerId") String customerId, @Param("type") int type, @Param("amount") int amount);
    @Update("update t_bank_account set balance=(balance+#{amount}) where customer_id=#{customer_id} and account_type=#{type}")
    boolean addMonry(@Param("customerId") String customerId, @Param("type")int type, @Param("amount")int amount);

}
