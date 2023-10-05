package cn.zoeci.sosobackend.controller;

import cn.zoeci.sosobackend.common.BaseResponse;
import cn.zoeci.sosobackend.common.ErrorCode;
import cn.zoeci.sosobackend.common.ResultUtils;
import cn.zoeci.sosobackend.exception.BusinessException;
import cn.zoeci.sosobackend.manager.SearchFacade;
import cn.zoeci.sosobackend.model.dto.post.PostQueryRequest;
import cn.zoeci.sosobackend.model.dto.search.SearchRequest;
import cn.zoeci.sosobackend.model.dto.user.UserQueryRequest;
import cn.zoeci.sosobackend.model.entity.Picture;
import cn.zoeci.sosobackend.model.vo.PostVO;
import cn.zoeci.sosobackend.model.vo.SearchVO;
import cn.zoeci.sosobackend.model.vo.UserVO;
import cn.zoeci.sosobackend.service.PictureService;
import cn.zoeci.sosobackend.service.PostService;
import cn.zoeci.sosobackend.service.UserService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.concurrent.CompletableFuture;

/**
 * 聚合搜索接口
 *
 */
@RestController
@RequestMapping("/search")
@Slf4j
public class SearchController {

    @Resource
    private UserService userService;

    @Resource
    private PostService postService;

    @Resource
    private PictureService pictureService;

    @Resource
    private SearchFacade searchFacade;

    @PostMapping("/all")
    public BaseResponse<SearchVO> searchAll(@RequestBody SearchRequest searchRequest, HttpServletRequest request){
            return ResultUtils.success(searchFacade.searchAll(searchRequest, request));

    }

//    public BaseResponse<SearchVO> searchAll(@RequestBody SearchRequest searchRequest, HttpServletRequest request){
//        String searchText = searchRequest.getSearchText();
//        Page<Picture> picturePage = pictureService.searchPicture(searchText, 1, 10);
//
//        UserQueryRequest userQueryRequest = new UserQueryRequest();
//        userQueryRequest.setUserName(searchText);
//        Page<UserVO> userPage = userService.listUserVOByPage(userQueryRequest);
//
//        PostQueryRequest postQueryRequest = new PostQueryRequest();
//        postQueryRequest.setSearchText(searchText);
//        Page<PostVO> postPage = postService.listPostVOByPage(postQueryRequest, request);
//
//        SearchVO searchVO = new SearchVO();
//        searchVO.setUserVOList(userPage.getRecords());
//        searchVO.setPostVOList(postPage.getRecords());
//        searchVO.setPictureList(picturePage.getRecords());
//
//        return ResultUtils.success(searchVO);
//    }

    // 并发访问不同类型
    @PostMapping("/allAsync")
    public BaseResponse<SearchVO> searchAllAsync(@RequestBody SearchRequest searchRequest, HttpServletRequest request){
        String searchText = searchRequest.getSearchText();

        CompletableFuture<Page<UserVO>> userTask = CompletableFuture.supplyAsync(() -> {
            UserQueryRequest userQueryRequest = new UserQueryRequest();
            userQueryRequest.setUserName(searchText);
            Page<UserVO> userPage = userService.listUserVOByPage(userQueryRequest);
            return userPage;
        });

        CompletableFuture<Page<PostVO>> postTask = CompletableFuture.supplyAsync(() -> {
            PostQueryRequest postQueryRequest = new PostQueryRequest();
            postQueryRequest.setSearchText(searchText);
            Page<PostVO> postPage = postService.listPostVOByPage(postQueryRequest, request);
            return postPage;
        });

        CompletableFuture<Page<Picture>> pictureTask = CompletableFuture.supplyAsync(() -> {
            Page<Picture> picturePage = pictureService.searchPicture(searchText, 1, 10);
            return picturePage;
        });

        // 等待异步任务完成并组合起来
        CompletableFuture.allOf(userTask, postTask, pictureTask).join();
        try{
            Page<UserVO> userPage = userTask.get();
            Page<PostVO> postPage = postTask.get();
            Page<Picture> picturePage = pictureTask.get();

            SearchVO searchVO = new SearchVO();
            searchVO.setUserVOList(userPage.getRecords());
            searchVO.setPostVOList(postPage.getRecords());
            searchVO.setPictureList(picturePage.getRecords());
            return ResultUtils.success(searchVO);
        }catch (Exception e){
            log.error("查询异常",e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "查询异常");
        }

    }


}
