package com.zoom.risk.operating.decision.service;

import com.zoom.risk.operating.decision.po.DBNode;
import com.zoom.risk.operating.decision.vo.RouteMode;

import java.util.List;

/**
 * Created by jiangyulin on 2017/5/16.
 */
public interface DecisionNodeService {

    public  List<List<RouteMode>> generateRoutes(List<DBNode> list);

    public DBNode buildDecisionTree(List<DBNode> list);

    public DBNode buildDecisionTree(List<DBNode> list, boolean validate);

}
