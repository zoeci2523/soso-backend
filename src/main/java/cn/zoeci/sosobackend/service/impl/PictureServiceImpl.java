package cn.zoeci.sosobackend.service.impl;

import cn.hutool.json.JSONUtil;
import cn.zoeci.sosobackend.common.ErrorCode;
import cn.zoeci.sosobackend.exception.BusinessException;
import cn.zoeci.sosobackend.model.entity.Picture;
import cn.zoeci.sosobackend.service.PictureService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 图片服务实现类
 */
@Service
@Slf4j
public class PictureServiceImpl implements PictureService {


    @Override
    public Page<Picture> searchPicture(String searchText, long pageNum, long pageSize) {
        long current = (pageNum-1) * pageSize;
        String url = String.format("https://www.bing.com/images/search?q=%s&form=HDRSC2&first=%d", searchText, current);
        Document doc = null;
        try {
            doc = Jsoup.connect(url).get();
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"数据获取异常");
        }
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
            if (pictures.size() >= pageSize) break;
        }
        Page<Picture> picturePage = new Page<>(pageNum, pageSize);
        picturePage.setRecords(pictures);
        return picturePage;
    }
}




