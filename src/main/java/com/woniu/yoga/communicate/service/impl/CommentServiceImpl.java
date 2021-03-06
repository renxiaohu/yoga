package com.woniu.yoga.communicate.service.impl;

import com.github.pagehelper.PageInfo;
import com.woniu.yoga.commom.utils.CommentUtil;
import com.woniu.yoga.communicate.constant.SysConstant;
import com.woniu.yoga.communicate.dao.CommentMapper;
import com.woniu.yoga.communicate.pojo.Comment;
import com.woniu.yoga.communicate.service.CommentService;
import com.woniu.yoga.communicate.vo.CommentVo;
import com.woniu.yoga.home.vo.Result;
import com.woniu.yoga.user.pojo.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpSession;
import java.util.List;

/**
 * @author huijie yan
 * @version 1.0.0
 * @description 评论业务层
 * @date 2019/4/23 15:10
 */
@Service
public class CommentServiceImpl implements CommentService {

    @Autowired
    private CommentMapper commentMapper;

    /**
    * @Description 展示评论
    * @param mid
    * @param currentPage
    * @param pageSize
    * @author huijie yan
    * @date 2019/4/23
    * @return com.woniu.yoga.commom.vo.Result
    */
    @Override
    public Result showComments(Integer mid) {
        if (mid == 0){
            return Result.error("未获取到动态内容id:m_id");
        }
        List<CommentVo> list = commentMapper.queryComments(mid);
        for (CommentVo vo:list) {
            String publishTime = CommentUtil.publishTime(vo.getCommentCreateTime());
            vo.setPublishTime(publishTime);
        }

        return Result.success("成功",list);

    }

    /**
    * @Description 添加评论
    * @param comment
    * @author huijie yan
    * @date 2019/4/23
    * @return com.woniu.yoga.commom.vo.Result
    */
    @Override
    @Transactional
    public Result addComment(Comment comment, HttpSession session) {
        User user = (User) session.getAttribute(SysConstant.CURRENT_USER);
        if (user.getUserPhone() == null){
            return Result.error("未绑定手机不能评论");
        }
        commentMapper.insertSelective(comment);
        if (comment.getEntityType() == 1){
            commentMapper.addHomepageCount(comment.getEntityId());
        }else if (comment.getEntityType() == 2){
            int homepageId = commentMapper.selectHomepageId(comment.getEntityId());
            commentMapper.addHomepageCount(homepageId);
        }
        return Result.success("添加成功");
    }

    /**
    * @Description 删除评论
    * @param commentId
    * @author huijie yan
    * @date 2019/4/26
    * @return com.woniu.yoga.commom.vo.Result
    */
    @Override
    @Transactional
    public Result deleteComment(Integer commentId) {
        Comment comment = commentMapper.selectByPrimaryKey(commentId);
        if (comment.getEntityType() == 1){
            commentMapper.reduceCommentCount(comment.getEntityId());
        }else if (comment.getEntityType() == 2){
            int homepageId = commentMapper.selectHomepageId(comment.getEntityId());
            commentMapper.reduceCommentCount(homepageId);
        }
        commentMapper.deleteByPrimaryKey(commentId);
        return Result.success("删除成功");
    }


}
