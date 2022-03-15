package io.kimmking.dubbo.demo.provider;

import io.kimmking.dubbo.demo.api.AccountServer;
import io.kimmking.dubbo.demo.provider.mapper.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboService;
import org.dromara.hmily.annotation.HmilyTCC;
import org.dromara.hmily.common.exception.HmilyRuntimeException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * @author Leo liang
 * @Date 2022/3/16
 */
@DubboService(version = "1.0.0", tag = "red", weight = 100)
@Slf4j
public class AccountServerImpl implements AccountServer {

    @Autowired
    private AccountMapper accountMapper;

    @Autowired
    private FreezeAccountMapper freezeAccountMapper;

    @Autowired
    private TryMapper tryMapper;

    @Autowired
    private ConfirmMapper confirmMapper;

    @Autowired
    private CancelMapper cancelMapper;

    @Override
    public boolean doTransfer(String tId, String customerId, int fromAccountType, int toAccountType, int fromAmount, int toAmount) {

        transferAdd(tId, customerId, fromAccountType, fromAmount);
        //新生成事务id
        transferSub(UUID.randomUUID().toString(), customerId, toAccountType, toAmount);
        return true;
    }



    @HmilyTCC(confirmMethod = "confirm1", cancelMethod = "cancel1")
    private boolean transferSub(String tId, String customerId, int toAccountType, int toAmount) {

        //账户减钱
        //幂等判断 判断t_try_log表中是否有try日志记录，如果有则不再执行
        if (tryMapper.isExist(tId)) {
            log.info("bank 中，tid 为 {} 的try操作已执行。直接退出。。", tId);
            return false;
        }
        //悬挂处理：如果cancel、confirm有一个已经执行了，try不再执行
        if (confirmMapper.isExist(tId) || cancelMapper.isExist(tId)) {
            log.info("bank 中，tid 为 {} 的cancel 或者 confirm 操作已执行。直接退出。。", tId);
            return false;
        }
        //扣减金额 冻结操作
        if (accountMapper.subMoney(customerId, 1, toAmount)) {
            log.info("bank 中，账户 {} fromAmount扣减金额 {} 成功 tid is {} ！！！", customerId, toAmount, tId);
            freezeAccountMapper.initAccount(customerId, toAccountType, toAmount, 1);
            //插入try操作日志
            tryMapper.addTry(tId);
            log.info("bank 中插入from try log ...");
        } else {
            throw new HmilyRuntimeException("账户扣减异常！");
        }

        log.info("bank subAccountBalance try end ... tid is {} customerId is {} amount is {} ！！！", tId, customerId, toAmount);

        return true;

    }

    @HmilyTCC(confirmMethod = "confirm", cancelMethod = "cancel")
    private boolean transferAdd(String tId, String customerId, int fromAccountType, int fromAmount) {

        //账户加钱
        //幂等判断 判断t_try_log表中是否有try日志记录，如果有则不再执行
        if (tryMapper.isExist(tId)) {
            log.info("bank 中，tid 为 {} 的try操作已执行。直接退出。。", tId);
            return false;
        }
        //悬挂处理：如果cancel、confirm有一个已经执行了，try不再执行
        if (confirmMapper.isExist(tId) || cancelMapper.isExist(tId)) {
            log.info("bank 中，tid 为 {} 的cancel 或者 confirm 操作已执行。直接退出。。", tId);
            return false;
        }
        //扣减金额 冻结操作
        if (accountMapper.addMonry(customerId, 1, fromAmount)) {
            log.info("bank 中，账户 {} fromAmount扣减金额 {} 成功 tid is {} ！！！", customerId, fromAmount, tId);
            freezeAccountMapper.initAccount(customerId, fromAccountType, fromAmount, 1);
            //插入try操作日志
            tryMapper.addTry(tId);
            log.info("bank 中插入from try log ...");
        } else {
            throw new HmilyRuntimeException("账户扣减异常！");
        }

        log.info("bank addAccountBalance try end ... tid is {} customerId is {} amount is {} ！！！", tId, customerId, fromAmount);

        return true;

    }

    @Transactional(rollbackFor = Exception.class)
    public boolean confirm(String tId, String customerId, int accountType, int amount) {

        log.info("bank1 confirm begin ... tid is {} customerId is {} amount is {} ！！！", tId, customerId, amount);
        //幂等校验 如果confirm操作未执行，才能去除冻结金额，否则什么也不做。。
        if (!confirmMapper.isExist(tId)) {
            //只有try操作完成之后，且cancel操作未执行的情况下，才允许执行confirm
            if (tryMapper.isExist(tId) && !cancelMapper.isExist(tId)) {
                //解除冻结金额
                log.info("bank1 confirm 操作中，解除冻结金额成功!");
                freezeAccountMapper.unfreezeAccount(customerId, 1, amount);
                //写入confirm日志
                log.info("bank1 confirm 操作中，增加confirm日志!");
                confirmMapper.addConfirm(tId);
            }
        }
        log.info("bank confirm end ... tid is {} customerId is {} amount is {} ！！！", tId, customerId, amount);
        return true;
    }

    @Transactional(rollbackFor = Exception.class)
    public boolean cancel(String tId, String customerId, int accountType, int amount) {
        log.info("bank1 cancel begin ... tid is {} customerId is {} amount is {} ！！！", tId, customerId, amount);
        //幂等校验 只有当cancel操作未执行的情况下，才执行cancel，否则什么也不做。
        if (!cancelMapper.isExist(tId)) {
            //空回滚操作，如果try操作未执行，那么cancel什么也不做，当且仅当try执行之后，才能执行cancel
            if (tryMapper.isExist(tId)) {
                //cancel操作，需要判断confirm是否执行了
                //如果此时confirm还未执行，那么需要将冻结金额清除
                if (!confirmMapper.isExist(tId)) {
                    log.info("bank1 cancel 操作中，解除冻结金额成功!");
                    freezeAccountMapper.revertFreezeAccount(customerId, 1, amount, 2);
                }
                log.info("bank1  cancel 操作中，增加账户余额成功!");
                //增加cancel log
                cancelMapper.addCancel(tId);
            }
        }
        log.info("bank1 cancel end ... tid is {} customerId is {} amount is {} ！！！", tId, customerId, amount);
        return true;
    }

    @Transactional(rollbackFor = Exception.class)
    public boolean confirm1(String tId, String customerId, int accountType, int amount) {

        log.info("bank1 confirm begin ... tid is {} customerId is {} amount is {} ！！！", tId, customerId, amount);
        //幂等校验 如果confirm操作未执行，才能去除冻结金额，否则什么也不做。。
        if (!confirmMapper.isExist(tId)) {
            //只有try操作完成之后，且cancel操作未执行的情况下，才允许执行confirm
            if (tryMapper.isExist(tId) && !cancelMapper.isExist(tId)) {
                //解除冻结金额
                log.info("bank1 confirm 操作中，解除冻结金额成功!");
                freezeAccountMapper.unfreezeAccount(customerId, 1, amount);
                //写入confirm日志
                log.info("bank1 confirm 操作中，增加confirm日志!");
                confirmMapper.addConfirm(tId);
            }
        }
        log.info("bank confirm end ... tid is {} customerId is {} amount is {} ！！！", tId, customerId, amount);
        return true;
    }

    @Transactional(rollbackFor = Exception.class)
    public boolean cancel1(String tId, String customerId, int accountType, int amount) {
        log.info("bank1 cancel begin ... tid is {} customerId is {} amount is {} ！！！", tId, customerId, amount);
        //幂等校验 只有当cancel操作未执行的情况下，才执行cancel，否则什么也不做。
        if (!cancelMapper.isExist(tId)) {
            //空回滚操作，如果try操作未执行，那么cancel什么也不做，当且仅当try执行之后，才能执行cancel
            if (tryMapper.isExist(tId)) {
                //cancel操作，需要判断confirm是否执行了
                //如果此时confirm还未执行，那么需要将冻结金额清除
                if (!confirmMapper.isExist(tId)) {
                    log.info("bank1 cancel 操作中，解除冻结金额成功!");
                    freezeAccountMapper.revertFreezeAccount(customerId, 1, amount, 1);
                }
                log.info("bank1  cancel 操作中，增加账户余额成功!");
                //增加cancel log
                cancelMapper.addCancel(tId);
            }
        }
        log.info("bank1 cancel end ... tid is {} customerId is {} amount is {} ！！！", tId, customerId, amount);
        return true;
    }
}