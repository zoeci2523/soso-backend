package cn.zoeci.sosobackend.dataSource;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

/**
 * 数据源接口（新接入的数据源必须实现）
 */
public interface DataSource<T> {
    /**
     * 搜索接口
     */
    Page<T> doSearch(String searchText, long pageNum, long pageSize);
}
