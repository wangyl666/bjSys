package com.wyl.note.vo;

import lombok.Data;

@Data
public class GraphNodeVO {

    private Long id;

    private String name;

    private String color;

    private Integer noteCount;

    private Integer category;
}
