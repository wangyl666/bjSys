package com.wyl.note.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wyl.note.entity.Note;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface NoteMapper extends BaseMapper<Note> {

    @Select("SELECT n.* FROM note n INNER JOIN note_tag nt ON n.id = nt.note_id " +
            "WHERE nt.tag_id = #{tagId} AND n.user_id = #{userId} " +
            "ORDER BY n.updated_at DESC")
    Page<Note> selectNotesByTagId(Page<Note> page, @Param("tagId") Long tagId, @Param("userId") Long userId);

    @Select("SELECT n.* FROM note n INNER JOIN note_tag nt ON n.id = nt.note_id " +
            "WHERE nt.tag_id = #{tagId} AND n.user_id = #{userId} AND n.category_id = #{categoryId} " +
            "ORDER BY n.updated_at DESC")
    Page<Note> selectNotesByTagIdAndCategoryId(Page<Note> page, @Param("tagId") Long tagId, 
                                                 @Param("categoryId") Long categoryId, @Param("userId") Long userId);

    @Select("SELECT n.* FROM note n INNER JOIN note_tag nt ON n.id = nt.note_id " +
            "WHERE nt.tag_id = #{tagId} AND n.user_id = #{userId} " +
            "AND (n.title LIKE CONCAT('%', #{keyword}, '%') OR n.content LIKE CONCAT('%', #{keyword}, '%') OR n.summary LIKE CONCAT('%', #{keyword}, '%')) " +
            "ORDER BY n.updated_at DESC")
    Page<Note> selectNotesByTagIdAndKeyword(Page<Note> page, @Param("tagId") Long tagId, 
                                              @Param("keyword") String keyword, @Param("userId") Long userId);

    @Select("SELECT n.* FROM note n INNER JOIN note_tag nt ON n.id = nt.note_id " +
            "WHERE nt.tag_id = #{tagId} AND n.user_id = #{userId} AND n.category_id = #{categoryId} " +
            "AND (n.title LIKE CONCAT('%', #{keyword}, '%') OR n.content LIKE CONCAT('%', #{keyword}, '%') OR n.summary LIKE CONCAT('%', #{keyword}, '%')) " +
            "ORDER BY n.updated_at DESC")
    Page<Note> selectNotesByTagIdCategoryIdAndKeyword(Page<Note> page, @Param("tagId") Long tagId, 
                                                        @Param("categoryId") Long categoryId, @Param("keyword") String keyword, 
                                                        @Param("userId") Long userId);
}
