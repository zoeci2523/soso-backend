package cn.zoeci.sosobackend.model.vo;

import cn.zoeci.sosobackend.model.entity.Picture;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 聚合搜索视图
 *
 */
@Data
public class SearchVO implements Serializable {
    private static final long serialVersionUID = 1L;

    private List<UserVO> userVOList;

    private List<PostVO> postVOList;

    private List<Picture> pictureList;

    private Page dataList;
}
