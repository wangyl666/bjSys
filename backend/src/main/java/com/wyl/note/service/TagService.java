package com.wyl.note.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.wyl.note.entity.Tag;
import com.wyl.note.vo.TagVO;

import java.util.List;

public interface TagService extends IService<Tag> {

    TagVO createTag(String name, String color, Long userId);

    void deleteTag(Long id, Long userId);

    List<TagVO> getUserTags(Long userId);
}
