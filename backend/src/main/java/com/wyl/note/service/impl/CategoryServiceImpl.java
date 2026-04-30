package com.wyl.note.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wyl.note.entity.Category;
import com.wyl.note.entity.Note;
import com.wyl.note.mapper.CategoryMapper;
import com.wyl.note.service.CategoryService;
import com.wyl.note.service.NoteService;
import com.wyl.note.vo.CategoryVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {

    private final NoteService noteService;

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

        return toVO(category, 0L);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CategoryVO updateCategory(Long id, String name, Long userId) {
        Category category = getById(id);
        if (category == null || !category.getUserId().equals(userId)) {
            throw new RuntimeException("分类不存在或无权限操作");
        }

        Category exist = getOne(
                new LambdaQueryWrapper<Category>()
                        .eq(Category::getUserId, userId)
                        .eq(Category::getName, name)
                        .ne(Category::getId, id));
        if (exist != null) {
            throw new RuntimeException("分类名称已存在");
        }

        category.setName(name);
        category.setUpdatedAt(LocalDateTime.now());
        updateById(category);

        Long noteCount = noteService.count(new LambdaQueryWrapper<Note>()
                .eq(Note::getUserId, userId)
                .eq(Note::getCategoryId, id));

        return toVO(category, noteCount);
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

        List<Long> categoryIds = categories.stream().map(Category::getId).collect(Collectors.toList());
        
        Map<Long, Long> noteCountMap = Map.of();
        if (!categoryIds.isEmpty()) {
            List<Note> notes = noteService.list(new LambdaQueryWrapper<Note>()
                    .eq(Note::getUserId, userId)
                    .in(Note::getCategoryId, categoryIds));
            
            noteCountMap = notes.stream()
                    .collect(Collectors.groupingBy(Note::getCategoryId, Collectors.counting()));
        }

        return categories.stream()
                .map(cat -> toVO(cat, noteCountMap.getOrDefault(cat.getId(), 0L)))
                .collect(Collectors.toList());
    }

    private CategoryVO toVO(Category category, Long noteCount) {
        CategoryVO vo = new CategoryVO();
        BeanUtils.copyProperties(category, vo);
        vo.setNoteCount(noteCount);
        return vo;
    }
}
