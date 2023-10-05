package cn.zoeci.sosobackend.manager;

import cn.zoeci.sosobackend.common.ErrorCode;
import cn.zoeci.sosobackend.dataSource.*;
import cn.zoeci.sosobackend.exception.BusinessException;
import cn.zoeci.sosobackend.exception.ThrowUtils;
import cn.zoeci.sosobackend.model.dto.post.PostQueryRequest;
import cn.zoeci.sosobackend.model.dto.search.SearchRequest;
import cn.zoeci.sosobackend.model.dto.user.UserQueryRequest;
import cn.zoeci.sosobackend.model.entity.Picture;
import cn.zoeci.sosobackend.model.enums.SearchTypeEnum;
import cn.zoeci.sosobackend.model.vo.PostVO;
import cn.zoeci.sosobackend.model.vo.SearchVO;
import cn.zoeci.sosobackend.model.vo.UserVO;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.concurrent.CompletableFuture;

@Component
@Slf4j
public class SearchFacade {

    @Resource
    private PostDataSource postDataSource;

    @Resource
    private UserDataSource userDataSource;

    @Resource
    private PictureDataSource pictureDataSource;

    @Resource
    private DataSourceRegistry dataSourceRegistry;

    public SearchVO searchAll(@RequestBody SearchRequest searchRequest, HttpServletRequest request){
        // 如果type为空，搜索出所有的数据
        // 如果type不为空，如果type合法，查出对应数据，否则报错
        String type = searchRequest.getType();
        SearchTypeEnum searchTypeEnum = SearchTypeEnum.getEnumByValue(type);
        ThrowUtils.throwIf(StringUtils.isBlank(type), ErrorCode.PARAMS_ERROR);
        String searchText = searchRequest.getSearchText();
        long current = searchRequest.getCurrent();
        long pageSize = searchRequest.getPageSize();

        // 搜索所有数据
        if (searchTypeEnum == null){
            CompletableFuture<Page<UserVO>> userTask = CompletableFuture.supplyAsync(() -> {
                UserQueryRequest userQueryRequest = new UserQueryRequest();
                userQueryRequest.setUserName(searchText);
                Page<UserVO> userPage = userDataSource.doSearch(searchText, current, pageSize);
                return userPage;
            });

            CompletableFuture<Page<PostVO>> postTask = CompletableFuture.supplyAsync(() -> {
                PostQueryRequest postQueryRequest = new PostQueryRequest();
                postQueryRequest.setSearchText(searchText);
                Page<PostVO> postPage = postDataSource.doSearch(searchText, current, pageSize);
                return postPage;
            });

            CompletableFuture<Page<Picture>> pictureTask = CompletableFuture.supplyAsync(() -> {
                Page<Picture> picturePage = pictureDataSource.doSearch(searchText, current, pageSize);
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
                return searchVO;
            }catch (Exception e){
                log.error("查询异常",e);
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "查询异常");
            }
        }
        else {
            SearchVO searchVO = new SearchVO();
            DataSource<?> dataSource = dataSourceRegistry.getDataSourceByType(type);
            Page<?> page = dataSource.doSearch(searchText, current, pageSize);
            searchVO.setDataList(page);
            return searchVO;
        }
    }
}
