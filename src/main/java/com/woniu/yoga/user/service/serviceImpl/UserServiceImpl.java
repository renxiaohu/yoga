package com.woniu.yoga.user.service.serviceImpl;

import com.woniu.yoga.commom.utils.ExceptionUtil;
import com.woniu.yoga.commom.vo.Result;
import com.woniu.yoga.manage.dao.CouponMapper;
import com.woniu.yoga.manage.pojo.Coupon;
import com.woniu.yoga.user.dao.CoachMapper;
import com.woniu.yoga.user.dao.OrderMapper;
import com.woniu.yoga.user.dao.UserMapper;
import com.woniu.yoga.user.dto.InteractionDTO;
import com.woniu.yoga.user.dto.SearchConditionDTO;
import com.woniu.yoga.user.pojo.User;
import com.woniu.yoga.user.repository.UserRepository;
import com.woniu.yoga.user.service.UserService;
import com.woniu.yoga.user.util.*;
import com.woniu.yoga.user.vo.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.TimeUnit;


/**
 * @Author liufeng
 * @ClassName StudentService
 * @Date 2019/4/18 15:30
 * @Version 1.0
 * @Description 处理用户交互
 **/
@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private CoachMapper coachMapper;

    //查询用户所有订单
    @Override
    public Result listOrder(Integer userId, String orderStatus) {
        User user = userMapper.selectByPrimaryKey(userId);
        String status = OrderUtil.getOrderStatus(orderStatus);//利用工具将状态转为字符串（数组）
        List<OrderVO> data = null;
        try {
            if (user.getRoleId() == 1) {
                data = ConvertVOToDTOUtil.convertList(orderMapper.findStudentOrder(userId, status));
            }
            if (user.getRoleId() == 2) {
                data = ConvertVOToDTOUtil.convertList(orderMapper.findCoachOrder(userId, status));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw ExceptionUtil.getDatabaseException();
        }
        return ResultUtil.actionSuccess("查询成功", data);
    }

    //查询用户所有有效的优惠券
    @Override
    public Result listCouponsByUserId(Integer userId) throws RuntimeException {
        try {
            List<Coupon> coupons = userMapper.findCouponByUserId(userId);
            return ResultUtil.actionSuccess("查询成功", coupons);
        } catch (SQLException e) {
            e.printStackTrace();
            throw ExceptionUtil.getDatabaseException();
        }
    }

    //查找学员周边的瑜伽师
    @Override
    public Result listAroundCoachs(SearchConditionVO searchConditionVO) {
        //判断用户是否开发定位给app，null则没有开启定位，需要提示用户开启定位
        if (searchConditionVO.getLongitude() == null || searchConditionVO.getLatitude() == null) {
            return ResultUtil.errorOperation("请开启定位！");
        }
        //double[4] 西侧经度，东侧经度，南侧纬度，北侧纬度
        double bounds[] = GetBmapDistanceUtil.getRange(searchConditionVO.getLongitude(), searchConditionVO.getLatitude(), searchConditionVO.getRound());
        SearchConditionDTO searchConditionDTO = ConvertVOToDTOUtil.searchConditionConvert(bounds, searchConditionVO);
        List<CoachVO> data = null;
        try {
            data = ConvertVOToDTOUtil.convertCoachDTOtoVO(userMapper.listAroundCoach(searchConditionDTO));
        } catch (Exception e) {
            e.printStackTrace();
            throw ExceptionUtil.getDatabaseException();
        }
        return ResultUtil.actionSuccess("查询成功", data);
    }

    @Override
    public Result listAroundVenues(SearchConditionVO searchConditionVO) throws RuntimeException {
        //判断用户是否开发定位给app，null则没有开启定位，需要提示用户开启定位
        if (searchConditionVO.getLongitude() == null || searchConditionVO.getLatitude() == null) {
            return ResultUtil.errorOperation("请开启定位！");
        }
        //double[4] 西侧经度，东侧经度，南侧纬度，北侧纬度
        double bounds[] = GetBmapDistanceUtil.getRange(searchConditionVO.getLongitude(), searchConditionVO.getLatitude(), searchConditionVO.getRound());
        SearchConditionDTO searchConditionDTO = ConvertVOToDTOUtil.searchConditionConvert(bounds, searchConditionVO);
        List<VenueVOR> data = null;
        try {
            data = userMapper.listAroundVenue(searchConditionDTO);
        } catch (Exception e) {
            e.printStackTrace();
            throw ExceptionUtil.getDatabaseException();
        }
        return ResultUtil.actionSuccess("查询成功", data);
    }

    @Override
    public List<Coupon> fandCouponByUserId(int userid) throws RuntimeException {
        List<Coupon> coupons = null;
        try {
            coupons = userMapper.findCouponByUserId(userid);
        } catch (SQLException e) {
            e.printStackTrace();
            throw ExceptionUtil.getDatabaseException();
        }
        return coupons;
    }

    //如果是教练，查找头像、姓名、简介、流派、认证方式（单击查看场馆）、课程（单击查看课程），交易次数，好评数
    //好友或者公开才能查看qq、微信、电话等信息
    @Override
    public Result getDetailInfoByUserId(Integer userId, Integer coachId) {
        if (coachId == null) {
            return ResultUtil.errorOperation("请选择想了解的瑜伽师!");
        }
        CoachDetailInfoVO coachDetailInfoVO = null;
        try {
            coachDetailInfoVO = userMapper.getDetailInfoByUserId(coachId);
            User venue = userMapper.selectVenueByCoachId(coachDetailInfoVO.getCoachId());
            if (venue != null) {
                coachDetailInfoVO.setVenueId(venue.getUserId());
                coachDetailInfoVO.setVenueName(venue.getRealName());
            }
            //如果是场馆认证，设置venueName：场馆名
            if (coachDetailInfoVO.getAuthentication() == 1) {
                coachDetailInfoVO.setVenueName(coachMapper.getVenueByCoachId(coachId));
            }

            //如果学员和瑜伽师不是好友，隐藏个人信息；或者瑜伽师设置保密
            if ((coachDetailInfoVO.getPrivacy() == 0) || (coachDetailInfoVO.getPrivacy() == 1 && !(isFriend(userId, coachId)))) {
                coachDetailInfoVO.setQq(null);
                coachDetailInfoVO.setWechat(null);
                coachDetailInfoVO.setPhone(null);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw ExceptionUtil.getDatabaseException();
        }
        //如果是官方认证，设置venueName：平台认证
        if (coachDetailInfoVO.getAuthentication() == 2) {
            coachDetailInfoVO.setVenueName("平台认证");
        }
        InteractionDTO interactionDTO = this.getInteractionByUserId(coachId);
        coachDetailInfoVO.setFans(interactionDTO.getFans());
        coachDetailInfoVO.setFocus(interactionDTO.getFocus());
        coachDetailInfoVO.setComments(interactionDTO.getComments());
        coachDetailInfoVO.setInfo(interactionDTO.getInfo());
        //System.out.println("coach detail " + coachDetailInfoVO);
        return ResultUtil.actionSuccess("查询成功", coachDetailInfoVO);
    }

    @Transactional
    @Override
    public void saveUser(User user) {
        userRepository.save(user);
    }


    /**
     * 方法实现说明  发送注册邮箱的验证码
     * 1、生成验证码,保存验证码和邮箱，插入到redis中，键为邮箱，值为验证码
     * 2、在redis中，查找邮箱是否存在，存在代表插入成功，为true，此时不能更改激活状态，激活码改变在注册成功改变
     * 3、成功，则通过线程的方式给用户发送一封邮件，并且发送的时候在Redis设置验证码过期时间
     * <p>
     * 2、
     *
     * @return boolean true-->注册成功 false-->注册失败
     * @throws
     * @author lxy
     * @Param: userName, email, password, code,
     * @date 2019/4/17 17:03
     */
    @Override
    public boolean sendRegEmailCode(User user) {
        String userVerifyCode = CodeUtil.userNumber();
        user.setUserVerifyCode(userVerifyCode);
        String content = "<html><head></head><body><h1>这是一封绝密邮件,不要随便将内容透露给别人。" +
                "</h1><br><h3>您本次注册的所需验证码为：" + user.getUserVerifyCode() + "。请尽快注册，验证码有效时间为3分钟，超出时间范围内，需重新获取。</h3></body></html>";
        stringRedisTemplate.opsForValue().set(user.getUserEmail(), user.getUserVerifyCode());
        if (stringRedisTemplate.hasKey(user.getUserEmail())) {
            new Thread(new MailUtil(user.getUserEmail(), userVerifyCode, content)).start();
            stringRedisTemplate.expire(user.getUserEmail(), 100, TimeUnit.SECONDS);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public User queryUserByEmail(String userEmail) {
        return userRepository.queryUserByEmail(userEmail);
    }

    @Override
    public User queryUserByPhone(String userPhone) {
        return userRepository.queryUserByPhone(userPhone);
    }

    /**
     * 方法实现说明  登录和注册手机时发送密码（验证码）
     * 1、生成验证码,保存密码（验证码）和手机，插入到redis中，键为邮箱，值为密码（验证码）
     * 2、在redis中，查找邮箱是否存在，存在代表插入成功，为true，此时不能更改激活状态，激活码改变在注册成功改变
     * 3、成功，则通过线程的方式给用户发送短信，并且发送的时候在Redis设置密码（验证码）过期时间
     *
     * @return boolean
     * @throws
     * @author lxy
     * @Param:
     * @date 2019/4/21 1:53
     */
    @Override
    public boolean sendPhoneMessage(User user, Integer templateId) {
        String userPwd = CodeUtil.userNumber();
        user.setUserPwd(userPwd);
        stringRedisTemplate.opsForValue().set(user.getUserPhone(), userPwd);
        if (!stringRedisTemplate.hasKey(user.getUserPhone())) {
            return false;
        }
        new PhoneUtil(templateId).sendPhoneMessage(user.getUserPhone(), userPwd);
        stringRedisTemplate.expire(user.getUserPhone(), 180, TimeUnit.SECONDS);
        return true;

    }

    @Override
    public Result getVenueDetailInfoByUserId(Integer userId) {
        try {
            VenueDetailInfoVO venueDetailInfoVO = userMapper.getVenueDetailInfoByUserId(userId);
            InteractionDTO interactionDTO = this.getInteractionByUserId(userId);
            venueDetailInfoVO.setFans(interactionDTO.getFans());
            venueDetailInfoVO.setFocus(interactionDTO.getFocus());
            venueDetailInfoVO.setInfo(interactionDTO.getInfo());
            return ResultUtil.actionSuccess("查询成功", venueDetailInfoVO);
        } catch (SQLException e) {
            e.printStackTrace();
            throw ExceptionUtil.getDatabaseException();
        }
    }

    @Override
    public Result getAllMyInfos(Integer userId) {
        List infos = null;
        try {
            infos = userMapper.selectAllMyInfos(userId);
            return ResultUtil.actionSuccess("查询成功", infos);
        } catch (SQLException e) {
            e.printStackTrace();
            throw ExceptionUtil.getDatabaseException();
        }
    }

    @Override
    public Result getAllMyFans(Integer userId) {
        List fans = null;
        try {
            fans = userMapper.selectAllMyFans(userId);
            return ResultUtil.actionSuccess("查询成功", fans);
        } catch (SQLException e) {
            e.printStackTrace();
            throw ExceptionUtil.getDatabaseException();
        }
    }

    @Override
    public Result getAllMyFocus(Integer userId) {
        List focus = null;
        try {
            focus = userMapper.selectAllMyFocus(userId);
            return ResultUtil.actionSuccess("查询成功", focus);
        } catch (SQLException e) {
            e.printStackTrace();
            throw ExceptionUtil.getDatabaseException();
        }
    }

    @Override
    public Result getAllMyComments(Integer userId) {
        List comments = null;
        try {
            comments = userMapper.selectAllMyComments(userId);
            return ResultUtil.actionSuccess("查询成功", comments);
        } catch (SQLException e) {
            e.printStackTrace();
            throw ExceptionUtil.getDatabaseException();
        }
    }

    @Override
    public List<Coupon> selectCouponByUserId(int userId) {
        return userMapper.selectCouponByUserId(userId);
    }

    @Override
    public Result getStudentInfo(Integer otherId) {
        try {
            UserDetailVO userDetailVO = userMapper.getStudentInfo(otherId);
            InteractionDTO interactionDTO = this.getInteractionByUserId(otherId);
            userDetailVO.setFocus(interactionDTO.getFocus());
            userDetailVO.setFans(interactionDTO.getFans());
            userDetailVO.setInfo(interactionDTO.getInfo());
            return ResultUtil.actionSuccess("查询成功", userDetailVO);
        } catch (SQLException e) {
            e.printStackTrace();
            throw ExceptionUtil.getDatabaseException();
        }
    }

    @Override
    public User selectByPrimaryKey(Integer userId) {
        User user = userMapper.selectByPrimaryKey(userId);
        return user;
    }

    @Override
    public Integer updateUser(User user) {
        int row = userMapper.updateByPrimaryKeySelective(user);
        return row;
    }

    InteractionDTO getInteractionByUserId(int userId) throws RuntimeException {
        InteractionDTO interactionDTO = new InteractionDTO();
        try {
            interactionDTO.setFocus(userMapper.selectFocusByUserId(userId));
            interactionDTO.setFans(userMapper.selectFansByUserId(userId));
            interactionDTO.setInfo(userMapper.selectInfoByUserId(userId));
            interactionDTO.setComments((userMapper.selectCommentsByUserId(userId)));
            return interactionDTO;
        } catch (SQLException e) {
            e.printStackTrace();
            throw ExceptionUtil.getDatabaseException();
        }
    }

    boolean isFriend(Integer userId, Integer friendId) throws SQLException {
        Integer followId = userMapper.selectOneFocus(userId, friendId);
        Integer followId2 = userMapper.selectOneFocus(friendId, userId);
        if (followId != null && followId2 != null) {
            return true;
        }
        return false;
    }

}
