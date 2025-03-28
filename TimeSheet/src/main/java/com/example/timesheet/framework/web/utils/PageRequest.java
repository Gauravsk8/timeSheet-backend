package com.example.timesheet.framework.web.utils;

import com.fasterxml.jackson.databind.node.ArrayNode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class PageRequest {

    private Integer page;

    private Integer size;

    private ArrayNode filter;

    private String[] sort;

    private String graphql;

}
