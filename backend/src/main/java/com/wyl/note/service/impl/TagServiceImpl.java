package com.wyl.note.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wyl.note.entity.Tag;
import com.wyl.note.mapper.NoteTagMapper;
import com.wyl.note.mapper.TagMapper;
import com.wyl.note.service.TagService;
import com.wyl.note.vo.GraphEdgeVO;
import com.wyl.note.vo.GraphNodeVO;
import com.wyl.note.vo.TagGraphVO;
import com.wyl.note.vo.TagVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TagServiceImpl extends ServiceImpl<TagMapper, Tag> implements TagService {

    private static final List<String> COLORS = Arrays.asList(
            "#409EFF", "#67C23A", "#E6A23C", "#F56C6C", "#909399",
            "#00D4FF", "#00B42A", "#FF7D00", "#F53F3F", "#722ED1"
    );

    private final Random random = new Random();
    private final NoteTagMapper noteTagMapper;

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

    @Override
    public TagGraphVO getTagGraph(Long userId) {
        TagGraphVO graphVO = new TagGraphVO();
        List<GraphNodeVO> nodes = new ArrayList<>();
        List<GraphEdgeVO> edges = new ArrayList<>();

        List<Tag> tags = list(new LambdaQueryWrapper<Tag>().eq(Tag::getUserId, userId));
        if (CollectionUtils.isEmpty(tags)) {
            graphVO.setNodes(nodes);
            graphVO.setEdges(edges);
            return graphVO;
        }

        Map<Long, Tag> tagMap = tags.stream().collect(Collectors.toMap(Tag::getId, tag -> tag));

        List<Map<String, Object>> noteCounts = noteTagMapper.countNotesByTag(userId);
        Map<Long, Integer> tagNoteCountMap = new HashMap<>();
        for (Map<String, Object> row : noteCounts) {
            Long tagId = ((Number) row.get("tag_id")).longValue();
            Integer count = ((Number) row.get("note_count")).intValue();
            tagNoteCountMap.put(tagId, count);
        }

        int category = 0;
        for (Tag tag : tags) {
            GraphNodeVO node = new GraphNodeVO();
            node.setId(tag.getId());
            node.setName(tag.getName());
            node.setColor(tag.getColor());
            node.setNoteCount(tagNoteCountMap.getOrDefault(tag.getId(), 0));
            node.setCategory(category % 5);
            category++;
            nodes.add(node);
        }

        List<Map<String, Object>> relations = noteTagMapper.findTagRelations(userId);
        for (Map<String, Object> row : relations) {
            Long source = ((Number) row.get("source")).longValue();
            Long target = ((Number) row.get("target")).longValue();
            Integer value = ((Number) row.get("value")).intValue();

            if (tagMap.containsKey(source) && tagMap.containsKey(target)) {
                GraphEdgeVO edge = new GraphEdgeVO();
                edge.setSource(source);
                edge.setTarget(target);
                edge.setValue(value);
                edges.add(edge);
            }
        }

        graphVO.setNodes(nodes);
        graphVO.setEdges(edges);
        return graphVO;
    }

    private TagVO toVO(Tag tag) {
        TagVO vo = new TagVO();
        BeanUtils.copyProperties(tag, vo);
        return vo;
    }
}
