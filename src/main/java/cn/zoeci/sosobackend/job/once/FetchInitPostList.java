package cn.zoeci.sosobackend.job.once;

import cn.hutool.http.HttpRequest;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import cn.zoeci.sosobackend.common.ErrorCode;
import cn.zoeci.sosobackend.exception.BusinessException;
import cn.zoeci.sosobackend.model.entity.Post;
import cn.zoeci.sosobackend.service.PostService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.CommandLineRunner;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 获取初始帖子列表
 *
 */
// 取消注释后，每次启动springboot项目会只执行一次run方法
//@Component
@Slf4j
public class FetchInitPostList implements CommandLineRunner {

    @Resource
    private PostService postService;

    @Override
    public void run(String... args) {
        // 1。获取数据
        String json = "{\"current\":1,\"pageSize\":8,\"sortField\":\"createTime\",\"sortOrder\":\"descend\",\"category\":\"文章\",\"reviewStatus\":1}";
        String url = "https://www.code-nav.cn/api/post/search/page/vo";
        String result = HttpRequest.post(url)
                .body(json)
                .execute().body();
        // 2. Json转对象
        Map<String, Object> map = JSONUtil.toBean(result, Map.class);
        System.out.println(map);
        JSONObject data = (JSONObject) map.get("data");
        JSONArray records = (JSONArray) data.get("records");
        List<Post> postList = new ArrayList<>();
        for (Object r: records){
            JSONObject tempRecord = (JSONObject) r;
            Post post = new Post();
            String title = tempRecord.getStr("title");
            String content = tempRecord.getStr("content");
            // 判空
            if (StringUtils.isAnyBlank(title, content)){
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "帖子必须包括标题和内容");
            }
            post.setTitle(title);
            post.setContent(content);
            JSONArray tags = (JSONArray)tempRecord.get("tags");
            List<String> tagList = tags.toList(String.class);
            post.setTags(JSONUtil.toJsonStr(tagList));
            post.setUserId(1L); //应该根据业务需要填上特殊的userId
            postList.add(post);
        }
        // 3.数据入库
        boolean b = postService.saveBatch(postList);

        if (b){
            log.info("FetchInitPostList succeed, return {} number of records", postList.size());
        }else {
            log.error("FetchInitPostList failed");
        }

    }
}
