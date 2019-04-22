package com.woniu.yoga.user.service.serviceImpl;

import com.woniu.yoga.commom.utils.OrderIdUtil;
import com.woniu.yoga.communicate.dao.CommentMapper;
import com.woniu.yoga.communicate.pojo.Comment;
import com.woniu.yoga.manage.dao.CouponMapper;
import com.woniu.yoga.manage.pojo.Coupon;
import com.woniu.yoga.pay.dao.WalletMapper;
import com.woniu.yoga.pay.pojo.Wallet;
import com.woniu.yoga.user.dao.*;
import com.woniu.yoga.user.dto.SearchConditionDTO;
import com.woniu.yoga.user.pojo.*;
import com.woniu.yoga.user.service.StudentService;
import com.woniu.yoga.user.util.ConvertVOToDTOUtil;
import com.woniu.yoga.user.util.GetBmapDistanceUtil;
import com.woniu.yoga.user.util.OrderUtil;
import com.woniu.yoga.user.util.ResultUtil;
import com.woniu.yoga.user.vo.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * @Author liufeng
 * @ClassName StudentService
 * @Date 2019/4/18 15:30
 * @Version 1.0
 * @Description 处理学员和后台的交互
 **/
@Service
public class StudentServiceImpl implements StudentService {
//    @Autowired
//    private UserRepository userRepository;

    @Autowired
    private UserMapper userMapper;
    @Autowired
    private CoachMapper coachMapper;
    @Autowired
    private WalletMapper walletMapper;
    @Autowired
    private CourseMapper courseMapper;
    @Autowired
    private CouponMapper couponMapper;
    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private CommentMapper commentMapper;
//    private CourseRepository courseRepository;
//    @Autowired
//    private CouponRepository couponRepository;
//
//    @Autowired
//    private OrderRepository orderRepository;
//    @Autowired
//    private CommentRepository commentRepository;

    //查找学员周边的教练、场馆
    @Override
    public Result listAroundUserByIdOrAddress(SearchConditionVO searchConditionVO) {
        //判断用户是否开发定位给app，null则没有开启定位，需要提示用户开启定位
        if (searchConditionVO.getLongitude() == null || searchConditionVO.getLongitude() == null) {
            return null;
        }
        //判断角色id是否合法，2：教练；3：场馆；
        if (searchConditionVO.getRoleId() != 2 && searchConditionVO.getRoleId() != 3) {
            return null;
        }
        //double[4] 西侧经度，东侧经度，南侧纬度，北侧纬度
        double bounds[] = GetBmapDistanceUtil.getRange(searchConditionVO.getLongitude(), searchConditionVO.getLatitude(), searchConditionVO.getRound());
        SearchConditionDTO searchConditionDTO = ConvertVOToDTOUtil.searchConditionConvert(bounds, searchConditionVO);
        List<UserVO> data = userMapper.listAroundUser(searchConditionDTO);
        return ResultUtil.actionSuccess("查询成功",data);
    }

    //如果是教练，查找头像、姓名、简介、流派、认证方式（单击查看场馆）、课程（单击查看课程），交易次数，好评率
    //好友或者公开才能查看qq、微信、电话等信息
    //如果是场馆，查找头像，realName，简介，教练
    @Override
    public Result getDetailInfoByUserId(Integer userId, Integer coachId) {
        CoachDetailInfoVO coachDetailInfoVO = userMapper.getDetailInfoByUserId(coachId);
        //如果学员和瑜伽师不是好友，隐藏个人信息
        if (!("学员和瑜伽师是好友" == "")) {
            coachDetailInfoVO.setQq("secret");
            coachDetailInfoVO.setWechat("secret");
            coachDetailInfoVO.setPhone("secret");
        }
        //如果是场馆认证，设置venueName：场馆名
        if (coachDetailInfoVO.getAuthentication() == 1) {
            coachDetailInfoVO.setVenueName(coachMapper.getVenueByCoachId(coachId));
        }
        //如果是官方认证，设置venueName：平台认证
        if (coachDetailInfoVO.getAuthentication() == 2) {
            coachDetailInfoVO.setVenueName("平台认证");
        }
        List data = new ArrayList();
        data.add(coachDetailInfoVO);
        return ResultUtil.actionSuccess("查询成功",data);
    }

    //学员下单
    @Override
    public Result saveOrder(Order order) {
        Wallet wallet = walletMapper.selectByPrimaryKey(order.getPayerId());
        Course course = courseMapper.selectByPrimaryKey(order.getCourseId());
        BigDecimal orderMoney = course.getCoursePrice().multiply(new BigDecimal(order.getCourseCount()));
        if (orderMoney.compareTo(wallet.getBalance()) < 0) {
            return new Result(1, "余额不足，请充值");
        }
        order.setOrderMoney(orderMoney);
        order.setOrderStatus(OrderUtil.NEWORDER);
        order.setOrderId(OrderIdUtil.getOrderId());
        //插入订单
        int row = orderMapper.insertSelective(order);
        if (row == 1) {
            List data = new ArrayList();
            data.add(order);
            return ResultUtil.actionSuccess("已下单，等待处理中...",data);
        }
        return ResultUtil.connectDatabaseFail();
    }

    //学员付款前的订单金额更新，返回订单确认信息
    @Override
    public Result updateOrderWithCoupon(String orderId, Integer couponId) {
        int faceValue = 0;
        if (couponId != null) {
            Coupon coupon = couponMapper.selectByPrimaryKey(couponId);
            if (coupon == null) {
                return ResultUtil.errorOperation( "优惠券状态错误，请联系管理员");
            }
            faceValue = coupon.getFaceValue();
        }
        Order order = orderMapper.selectByPrimaryKey(orderId);
        if (order.getOrderStatus() != OrderUtil.WAITTOPAY) {
            return ResultUtil.errorOperation( "订单状态错误，请联系管理员");
        }
        User user = userMapper.selectByPrimaryKey(order.getPayerId());
        //查询会员等级，打折，计算应付款
        BigDecimal VIPDiscount = userMapper.selectDiscountByLevel(user.getUserLevel());
        BigDecimal discount = order.getOrderMoney().multiply(VIPDiscount).subtract(new BigDecimal(String.valueOf(faceValue)));
        order.setDiscount(discount);
        int row = orderMapper.insertSelective(order);
        if (row > 1) {
            List data = new ArrayList();
            data.add(order);
            return ResultUtil.actionSuccess( "订单信息更新，请确认...", data);
        } else {
            return ResultUtil.connectDatabaseFail();
        }
    }

    @Override
    public Result updateOrderForPay(String orderId) {
        //学员钱包余额减少，添加钱包记录，更改订单状态、优惠券状态、
        Order order = orderMapper.selectByPrimaryKey(orderId);
        if (order.getOrderStatus() != OrderUtil.WAITTOPAY) {
            return ResultUtil.errorOperation( "订单状态错误，请联系管理员");
        }
        User user = userMapper.selectByPrimaryKey(order.getPayerId());
        Wallet wallet = walletMapper.selectByUserId(user.getUserId());
        //更新钱包余额
        wallet.setBalance(wallet.getBalance().subtract(order.getDiscount()));
        //保存钱包余额

        //添加钱包记录

        //更新订单状态
        order.setOrderStatus(OrderUtil.PAIED);
        //更新优惠券状态

        return null;
    }

    @Override
    public Result saveComment(Comment comment) {
        int row = commentMapper.insertSelective(comment);
        //学员、瑜伽师 会员等级，积分变化，交易记录，并返回评论详细信息
        //根据金额转换积分，修改积分
        //根据积分转换等级
        return null;
    }

    @Override
    public Result listAllCourseAppoint() {
        List<CourseAppoint> data = new ArrayList<>();
        data.add(CourseAppoint.ONLINE);
        data.add(CourseAppoint.OFFLINE);
        return ResultUtil.actionSuccess("查询成功",data);
    }

    @Override
    public Result updateOrderForRefund(String orderId) {
        Order order = orderMapper.selectByPrimaryKey(orderId);
        if (order.getOrderStatus() != OrderUtil.PAIED) {
            return ResultUtil.errorOperation( "订单状态出错，请与管理员联系");
        }
        order.setOrderStatus(OrderUtil.APPLICATIONFORDRAWBACK);
        int row = orderMapper.updateByPrimaryKeySelective(order);
        if (row > 0) {
            List data = new ArrayList();
            data.add(order);
            return ResultUtil.actionSuccess( "已提交申请，等待客服处理中...",data);
        }
        return ResultUtil.connectDatabaseFail();
    }
}