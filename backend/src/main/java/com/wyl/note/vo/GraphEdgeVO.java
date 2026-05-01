package com.wyl.note.vo;

import lombok.Data;

@Data
public class GraphEdgeVO {

    private Long source;

    private Long target;

    private Integer value;
}
