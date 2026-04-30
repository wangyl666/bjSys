package com.wyl.note.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.wyl.note.entity.Category;
import com.wyl.note.vo.CategoryVO;

import java.util.List;

public interface CategoryService extends IService<Category> {

    CategoryVO createCategory(String name, Long userId);

    void deleteCategory(Long id, Long userId);

    List<CategoryVO> getUserCategories(Long userId);
}
