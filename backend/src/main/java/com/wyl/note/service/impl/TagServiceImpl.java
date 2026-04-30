package com.wyl.note.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wyl.note.entity.Tag;
import com.wyl.note.mapper.TagMapper;
import com.wyl.note.service.TagService;
import com.wyl.note.vo.TagVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TagServiceImpl extends ServiceImpl<TagMapper, Tag> implements TagService {

    private static final List<String> COLORS = Arrays.asList(
            "#409EFF", "#67C23A", "#E6A23C", "#F56C6C", "#909399",
            "#00D4FF", "#00B42A", "#FF7D00", "#F53F3F", "#722ED1"
    );

    private final Random random = new Random();

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TagVO createTag(String name, String color, Long userId) {
        Tag exist = getOne(
                new LambdaQueryWrapper<Tag>().eq(Tag::getUserId, userId).eq(Tag::getName, name));
        if (exist != null) {
            return toVO(exist);
        }

        Tag tag = new Tag();
        tag.setUserId(userId);
        tag.setName(name);
        tag.setColor(color != null ? color : COLORS.get(random.nextInt(COLORS.size())));
        tag.setCreatedAt(LocalDateTime.now());
        save(tag);

        return toVO(tag);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteTag(Long id, Long userId) {
        Tag tag = getById(id);
        if (tag == null || !tag.getUserId().equals(userId)) {
            throw new RuntimeException("标签不存在或无权限操作");
        }
        removeById(id);
    }

    @Override
    public List<TagVO> getUserTags(Long userId) {
        List<Tag> tags = list(
                new LambdaQueryWrapper<Tag>().eq(Tag::getUserId, userId).orderByDesc(Tag::getCreatedAt));
        return tags.stream().map(this::toVO).collect(Collectors.toList());
    }

    private TagVO toVO(Tag tag) {
        TagVO vo = new TagVO();
        BeanUtils.copyProperties(tag, vo);
        return vo;
    }
}
