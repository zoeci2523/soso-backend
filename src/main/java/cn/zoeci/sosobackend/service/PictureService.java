package cn.zoeci.sosobackend.service;

import cn.zoeci.sosobackend.model.entity.Picture;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

/**
 * 图片服务
 *
 */
public interface PictureService{
    Page<Picture> searchPicture(String searchText, long pageNum, long pageSize);
}
