package com.wyl.note.vo;

import lombok.Data;

import java.util.List;

@Data
public class TagGraphVO {

    private List<GraphNodeVO> nodes;

    private List<GraphEdgeVO> edges;
}
