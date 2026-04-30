package com.wyl.note.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

@Data
@TableName("note_tag")
public class NoteTag {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long noteId;

    private Long tagId;
}
