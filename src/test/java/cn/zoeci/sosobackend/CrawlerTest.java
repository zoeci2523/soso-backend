package cn.zoeci.sosobackend;

import cn.hutool.http.HttpRequest;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import cn.zoeci.sosobackend.model.entity.Picture;
import cn.zoeci.sosobackend.model.entity.Post;
import cn.zoeci.sosobackend.service.PostService;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@SpringBootTest
public class CrawlerTest{

    @Resource
    PostService postService;

    @Test
    void testFetchPicture() throws IOException{
        int current = 1;
        String url = "https://www.bing.com/images/search?q=二哈&form=HDRSC2&first=" + current;
        Document doc = null;
        doc = Jsoup.connect(url).get();
        System.out.println(doc);
        Elements elements = doc.select(".iuscp.isv");
        List<Picture> pictures = new ArrayList<>();
        for(Element element: elements){
            // 取图片地址
            String m = element.select(".iusc").get(0).attr("m");
            Map<String, Object> map = JSONUtil.toBean(m, Map.class);
            String address = (String)map.get("murl");
            // 取图片标题
            String title = element.select(".inflnk").get(0).attr("aria-label");
            Picture p = new Picture();
            p.setUrl(address);
            p.setTitle(title);
            pictures.add(p);
        }
        System.out.println(pictures);
    }

    @Test
    void testFetchPassage(){
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
         // 需要判空
        for (Object r: records){
            JSONObject tempRecord = (JSONObject) r;
            Post post = new Post();
            post.setTitle(tempRecord.getStr("title"));
            post.setContent(tempRecord.getStr("content"));
            JSONArray tags = (JSONArray)tempRecord.get("tags");
            List<String> tagList = tags.toList(String.class);
            post.setTags(JSONUtil.toJsonStr(tagList));
            post.setUserId(1L);
            postList.add(post);
        }
        //System.out.println(postList);
        // 3.数据入库
        boolean b = postService.saveBatch(postList);
        Assertions.assertTrue(b);
    }
}
