package com.woniu.yoga.user.dao;

import com.woniu.yoga.communicate.pojo.Comment;
import com.woniu.yoga.home.pojo.Homepage;
import com.woniu.yoga.manage.pojo.Coupon;
import com.woniu.yoga.pay.pojo.Wallet;
import com.woniu.yoga.user.dto.CoachDTO;
import com.woniu.yoga.user.dto.SearchConditionDTO;
import com.woniu.yoga.user.pojo.User;
import com.woniu.yoga.user.util.UserMapperProviderUtil;
import com.woniu.yoga.user.vo.*;
import org.apache.ibatis.annotations.*;
import org.apache.ibatis.jdbc.SQL;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

@Repository
public interface UserMapper {
    int deleteByPrimaryKey(Integer userId);

    int insert(User record);

    int insertSelective(User record);

    User selectByPrimaryKey(Integer userId);

    int updateByPrimaryKeySelective(User record);

    int updateByPrimaryKey(User record);

    /*
     * @Author liufeng
     * @Description //根据地址查询附近的教练
     **/
    @SelectProvider(type = UserMapperProviderUtil.class, method = "findAroundCoachs")
    List<CoachDTO> listAroundCoach(SearchConditionDTO searchConditionDTO) throws SQLException;

    @SelectProvider(type = UserMapperProviderUtil.class, method = "listAroundVenues")
    List<VenueVOR> listAroundVenue(SearchConditionDTO searchConditionDTO) throws SQLException;

    /*
     * @Author liufeng
     * @Description //查询瑜伽师的详细信息
     **/
    @Select("select user.user_id userId,real_name,user_headimg,user_phone,user_qq,user_wechat,coach_detail,coach_id,coach_style,authentication,deal_account,good_comment,user_privacy,user_level level from user,coach where user.user_id = coach.user_id and user.user_id = #{userId} and user_flag = 0")
    @Results(value = {
            @Result(column = "real_name", property = "realName"),
            @Result(column = "user_headimg", property = "headImg"),
            @Result(column = "user_phone", property = "phone"),
            @Result(column = "user_qq", property = "qq"),
            @Result(column = "user_wechat", property = "wechat"),
            @Result(column = "coach_detail", property = "detail"),
            @Result(column = "coach_style", property = "style"),
            @Result(column = "authentication", property = "authentication"),
            @Result(column = "deal_account", property = "numberOfTrade"),
            @Result(column = "good_comment", property = "goodCommentCount"),
            @Result(column = "user_privacy", property = "privacy"),
            @Result(column = "user_level", property = "level"),
            @Result(column = "coach_id",property = "coachId"),
//            @Result(column = "coach_id", property = "courses", one = @One(
//                    select = "com.woniu.yoga.user.dao.CoachMapper.findCourseByCoachId"
//            )),
    })
    CoachDetailInfoVO getDetailInfoByUserId(Integer userId) throws SQLException;

    /*
     * @Author liufeng
     * @Description //根据用户等级查询所享有的优惠
     **/
    @Select("select discount from level_discount where level_id = #{userLevel}")
    BigDecimal selectDiscountByLevel(Integer userLevel);


    User saveUser(User user);

    Integer activeUserByEamil(String userEmail);

    Integer activeUserByPhone(String userPhone);

    User queryUserByEmail(String userEmail);

    User queryUserByEmailAndPwd(String userEmail, String userPwd);

    User queryUserByPhone(String userPhone);

    User queryUserByPhoneAndPwd(String userPhone, String userPwd);

    User queryUserByPhoneAndCode(String userPhone, String userVerifyCode);

    Integer updateCode(String userVerifyCode, String userPhone);

    User queryUserByEmailAndCode(String userEmail, String userVerifyCode);

    @Select("select user_phone from user where user_id = #{userId}")
    String selectPhoneByUserId(Integer userId);

    @Select("select * from coupon where coupon_id in (select coupon_id from user_coupon where user_id=#{userid}) and now() >= effective_date and now() < expiration_date")
    List<Coupon> findCouponByUserId(@Param("userid") int userid) throws SQLException;

    @Select("select * from coupon where coupon_id in (select coupon_id from user_coupon where user_id=#{userid})")
    List<Coupon> selectCouponByUserId(@Param("userid") int userid) ;

    @Select("select user_nickname nickname,user_headimg img,user_level level from user where user_id = #{userId}")
    UserVO findBriefInfoByUserId(Integer userId) throws SQLException;

    //查找我的关注数量
    @Select("select count(user_id) from follow where user_id =#{userId}")
    int selectFocusByUserId(int userId) throws SQLException;
    @Select("select user_id,user_headimg,user_nickname,user_level from user where user_id in(select user_id from follow where user_id =#{userId})")
    List<UserVO> selectAllMyFocus(int userId) throws  SQLException;
    //查找我的粉丝数量
    @Select("select count(followed_id) from follow where followed_id =#{userId}")
    int selectFansByUserId(int userId) throws SQLException;
    @Select("select user_id,user_headimg,user_nickname,user_level from user where user_id in(select followed_id from follow where followed_id =#{userId})")
    List<UserVO> selectAllMyFans(int userId) throws  SQLException;
    //查看我的动态数量
    @Select("select count(user_id) from homepage where user_id =#{userId}")
    int selectInfoByUserId(int userId) throws SQLException;
    @Select("select * from homepage where user_id = #{userId}")
    List<Homepage> selectAllMyInfos(int userId)throws SQLException;
    //查找我的课程收到的评论数量
    @Select("select count(comment_id) from comment where entity_type = 0 and entity_id in (select course_id from course where coach_id = (select coach_id from coach where user_id = #{userId}))")
    int selectCommentsByUserId(int userId) throws SQLException;
    @Select("select * from comment where entity_type = 0 and entity_id in (select course_id from course where coach_id = (select coach_id from coach where user_id = #{userId}))")
    List<Comment> selectAllMyComments(int userId)throws SQLException;
    //查看场馆的详细信息
    VenueDetailInfoVO getVenueDetailInfoByUserId(Integer userId) throws SQLException;
    //查看“userId”是否关注“followedId”，如果是返回followId
    @Select("select follow_id followId from follow where user_id =#{userId} and followed_id =#{friendId} and follow_status = 1")
    Integer selectOneFocus(Integer userId, Integer friendId) throws SQLException;

    UserDetailVO getStudentInfo(int userId) throws  SQLException;
    //根据瑜伽师的瑜伽师ID查询场馆的，姓名和userId
    @Select("select user_id userId,real_name realName from user where user_id =(select user_id from venue where venue_id =(select venue_id from venue_coach where coach_id = #{coachId} and cv_status = 1))")
    User selectVenueByCoachId(int coachId);
}