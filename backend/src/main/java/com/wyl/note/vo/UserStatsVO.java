package com.wyl.note.vo;

import lombok.Data;

@Data
public class UserStatsVO {

    private Long noteCount;

    private Long errorQuestionCount;

    private Long categoryCount;

    private Long tagCount;
}
