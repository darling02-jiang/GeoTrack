package com.geotrack.common.guard;

import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class SentinelFlowRuleRegistrar {

    private static final Map<String, FlowRule> RULES = new LinkedHashMap<>();

    private SentinelFlowRuleRegistrar() {
    }

    public static synchronized void registerQpsRule(String resource, double qps) {
        FlowRule rule = new FlowRule(resource);
        rule.setGrade(RuleConstant.FLOW_GRADE_QPS);
        rule.setCount(qps);
        RULES.put(resource, rule);
        FlowRuleManager.loadRules(new ArrayList<>(RULES.values()));
    }

    public static synchronized List<FlowRule> currentRules() {
        return List.copyOf(RULES.values());
    }
}
