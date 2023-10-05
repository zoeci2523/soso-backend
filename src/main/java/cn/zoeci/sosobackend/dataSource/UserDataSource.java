package cn.zoeci.sosobackend.dataSource;

import cn.zoeci.sosobackend.model.dto.user.UserQueryRequest;
import cn.zoeci.sosobackend.model.vo.UserVO;
import cn.zoeci.sosobackend.service.UserService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * 用户服务实现
 *
 */
@Service
@Slf4j
public class UserDataSource implements DataSource<UserVO> {

    @Resource
    private UserService userService;

    @Override
    public Page<UserVO> doSearch(String searchText, long pageNum, long pageSize){
        UserQueryRequest userQueryRequest = new UserQueryRequest();
        userQueryRequest.setUserName(searchText);
        userQueryRequest.setCurrent(pageNum);
        userQueryRequest.setPageSize(pageSize);

        Page<UserVO> userVOPage = userService.listUserVOByPage(userQueryRequest);

        return userVOPage;
    }

}
