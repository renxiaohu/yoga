package com.woniu.yoga.pay.dao;

import com.woniu.yoga.pay.pojo.Wallet;
import com.woniu.yoga.pay.pojo.WalletRecord;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
@Repository
public interface WalletMapper {
    int deleteByPrimaryKey(Integer walletId);

    int insert(Wallet record);

    int insertSelective(Wallet record);

    Wallet selectByPrimaryKey(Integer walletId);

    int updateByPrimaryKeySelective(Wallet record);

    int updateByPrimaryKey(Wallet record);
    //查找钱包
    @Select("select * from wallet where user_id=#{userId}")
    Wallet findWalletByuserId(@Param("userId") int userId);

    //查询交易记录
    @Select("select * from wallet_record where from_id =#{userId}")
    List<WalletRecord> selectOrderByuserId(@Param("userId") int userId);

    //更新钱余额用于付款
    @Update("update wallet set balance=balance-#{money} where wallet_id=#{walletId}")
    int updateUserMoneyByWalletId(@Param("walletId") Integer walletId,@Param("money") BigDecimal money);

    //更新钱余额用于充值
    @Update("update wallet set balance=balance+#{money} where wallet_id=#{walletId}")
    int saveMoney(@Param("walletId") int walletId, @Param("money") BigDecimal money);

    //添加银行卡
    @Update("update wallet set bankcard=#{bankcard},pay_pwd=#{pwd} where user_id=#{userId}")
    int addBankcardByWalletId(@Param("userId") Integer walletid,@Param("pwd") String pwd, String againPwd,@Param("bankcard") String bankcard);

    @Insert("INSERT INTO wallet (user_id) VALUES (#{userId});")
    void saveWallet(@Param("userId")Integer userId);
}