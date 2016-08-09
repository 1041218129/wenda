package com.nowcoder.controller;

import com.nowcoder.model.*;
import com.nowcoder.service.CommentService;
import com.nowcoder.service.LikeService;
import com.nowcoder.service.QuestionService;
import com.nowcoder.service.UserService;
import com.nowcoder.util.WendaUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by 10412 on 2016/8/1.
 */
@Controller
public class QuestionController
{

    private static final Logger logger = LoggerFactory.getLogger(QuestionController.class);

    @Autowired
    QuestionService questionService;

    @Autowired
    UserService userService;


    @Autowired
    HostHolder hostHolder;


    @Autowired
    CommentService commentService;

    @Autowired
    LikeService likeService;


    //提问
    @RequestMapping(value = "/question/add", method = {RequestMethod.POST})
    @ResponseBody
    public String addQuestion(@RequestParam("title") String title, @RequestParam("content") String content)
    {
        try {

            Question question = new Question();
            question.setContent(content);
            question.setTitle(title);
            question.setCreatedDate(new Date());
            question.setCommentCount(0);
            if (hostHolder.getUser() == null)
            {
//                question.setUserId(WendaUtil.ANONYMOUS_USERID);
                return WendaUtil.getJSONString(999);

            }
            else
            {
                question.setUserId(hostHolder.getUser().getId());
            }

            if (questionService.addQuestion(question) > 0)
            {
                return WendaUtil.getJSONString(0);
            }

        }catch (Exception e)
        {
            logger.error("增加题目失败"+e.getMessage());
        }

        return WendaUtil.getJSONString(1, "失败");

    }


    //问题详情
    @RequestMapping(value = "/question/{qid}")
    public String questionDetail(Model model, @PathVariable("qid") int qid)
    {
        Question question = questionService.selectById(qid);
        model.addAttribute("question", question);
        model.addAttribute("user", userService.getUser(question.getUserId()));


        //评论
        List<Comment> commentList = commentService.getCommentsByEntity(qid, EntityType.ENTITY_QUESTION);
        List<ViewObject> comments = new ArrayList<ViewObject>();

        for (Comment comment : commentList)
        {
            ViewObject vo = new ViewObject();
            vo.set("comment", comment);

            if (hostHolder.getUser() == null)
            {
                vo.set("liked", 0);
            }
            else
            {
                vo.set("liked", likeService.getLikeStatus(hostHolder.getUser().getId(), EntityType.ENTITY_COMMENT, comment.getId()));
            }

            vo.set("likeCount", likeService.getLikeCount(EntityType.ENTITY_COMMENT, comment.getId()));



            vo.set("user", userService.getUser(comment.getUserId()));
            comments.add(vo);

        }

        model.addAttribute("comments", comments);

        return "detail";
    }


}
