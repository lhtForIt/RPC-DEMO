package io.kimmking.dubbo.demo.api;


import org.dromara.hmily.annotation.Hmily;

/**
 * @author Leo liang
 * @Date 2022/3/14
 */
public interface AccountServer {

    //type:1 人民币账户，2 美元账户
    @Hmily
    boolean doTransfer(String tId, String customerId, int fromAccountType, int toAccountType, int fromAmount, int toAmount);

}
