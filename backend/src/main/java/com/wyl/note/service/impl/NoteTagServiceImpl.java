package com.wyl.note.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wyl.note.entity.NoteTag;
import com.wyl.note.mapper.NoteTagMapper;
import com.wyl.note.service.NoteTagService;
import org.springframework.stereotype.Service;

@Service
public class NoteTagServiceImpl extends ServiceImpl<NoteTagMapper, NoteTag> implements NoteTagService {
}
