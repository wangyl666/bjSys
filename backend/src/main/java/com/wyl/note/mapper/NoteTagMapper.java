package com.wyl.note.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wyl.note.entity.NoteTag;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface NoteTagMapper extends BaseMapper<NoteTag> {

    @Select("SELECT nt.tag_id, COUNT(DISTINCT nt.note_id) as note_count " +
            "FROM note_tag nt " +
            "INNER JOIN note n ON nt.note_id = n.id " +
            "WHERE n.user_id = #{userId} AND n.deleted = 0 " +
            "GROUP BY nt.tag_id")
    List<Map<String, Object>> countNotesByTag(@Param("userId") Long userId);

    @Select("SELECT nt1.tag_id as source, nt2.tag_id as target, COUNT(DISTINCT nt1.note_id) as value " +
            "FROM note_tag nt1 " +
            "INNER JOIN note_tag nt2 ON nt1.note_id = nt2.note_id AND nt1.tag_id < nt2.tag_id " +
            "INNER JOIN note n ON nt1.note_id = n.id " +
            "WHERE n.user_id = #{userId} AND n.deleted = 0 " +
            "GROUP BY nt1.tag_id, nt2.tag_id " +
            "HAVING COUNT(DISTINCT nt1.note_id) > 0")
    List<Map<String, Object>> findTagRelations(@Param("userId") Long userId);
}
