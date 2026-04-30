package com.wyl.note.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wyl.note.entity.Category;
import com.wyl.note.mapper.CategoryMapper;
import com.wyl.note.service.CategoryService;
import com.wyl.note.vo.CategoryVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CategoryVO createCategory(String name, Long userId) {
        Category exist = getOne(
                new LambdaQueryWrapper<Category>().eq(Category::getUserId, userId).eq(Category::getName, name));
        if (exist != null) {
            throw new RuntimeException("分类名称已存在");
        }

        Category category = new Category();
        category.setUserId(userId);
        category.setName(name);
        category.setSort(0);
        category.setCreatedAt(LocalDateTime.now());
        category.setUpdatedAt(LocalDateTime.now());
        save(category);

        return toVO(category);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteCategory(Long id, Long userId) {
        Category category = getById(id);
        if (category == null || !category.getUserId().equals(userId)) {
            throw new RuntimeException("分类不存在或无权限操作");
        }
        removeById(id);
    }

    @Override
    public List<CategoryVO> getUserCategories(Long userId) {
        List<Category> categories = list(
                new LambdaQueryWrapper<Category>().eq(Category::getUserId, userId).orderByAsc(Category::getSort));
        return categories.stream().map(this::toVO).collect(Collectors.toList());
    }

    private CategoryVO toVO(Category category) {
        CategoryVO vo = new CategoryVO();
        BeanUtils.copyProperties(category, vo);
        return vo;
    }
}
