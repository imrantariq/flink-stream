/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ververica.field.dynamicrules.functions;

import static com.ververica.field.dynamicrules.functions.ProcessingUtils.handleRuleBroadcast;

import com.ververica.field.dynamicrules.*;
import com.ververica.field.dynamicrules.Rule.ControlType;
import com.ververica.field.dynamicrules.Rule.RuleState;
import com.ververica.field.dynamicrules.RulesEvaluator.Descriptors;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import lombok.extern.slf4j.Slf4j;
import org.apache.flink.api.common.state.BroadcastState;
import org.apache.flink.api.common.state.ReadOnlyBroadcastState;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.metrics.Gauge;
import org.apache.flink.streaming.api.functions.co.BroadcastProcessFunction;
import org.apache.flink.util.Collector;

/** Implements dynamic data partitioning based on a set of broadcasted rules. */
@Slf4j
public class DynamicKeyFunction
    extends BroadcastProcessFunction<Transaction, Rule, Keyed<Transaction, String, Integer>> {

  private RuleCounterGauge ruleCounterGauge;

  @Override
  public void open(Configuration parameters) {
    ruleCounterGauge = new RuleCounterGauge();
    getRuntimeContext().getMetricGroup().gauge("numberOfActiveRules", ruleCounterGauge);
  }

  @Override
  public void processElement(
      Transaction event, ReadOnlyContext ctx, Collector<Keyed<Transaction, String, Integer>> out)
      throws Exception {
    ReadOnlyBroadcastState<Integer, Rule> rulesState =
        ctx.getBroadcastState(Descriptors.rulesDescriptor);
    forkEventForEachGroupingKey(event, rulesState, out);
  }

  private void forkEventForEachGroupingKey(Transaction event,
                                           ReadOnlyBroadcastState<Integer, Rule> rulesState,
                                           Collector<Keyed<Transaction, String, Integer>> out)
                                            throws Exception {
    int ruleCounter = 0;
    for (Map.Entry<Integer, Rule> entry : rulesState.immutableEntries()) {
      final Rule rule = entry.getValue();
      System.out.println("rule: ------- " + rule);
      if (rule.getRuleType().equalsIgnoreCase("default") && event.getRuleType().equalsIgnoreCase("default")) {
        DefaultRule defaultRule = (DefaultRule) rule;
        out.collect(
                new Keyed<>(
                        event, KeysExtractor.getKey(defaultRule.getGroupingKeyNames(), event), rule.getRuleId()));
      }else if (rule.getRuleType().equalsIgnoreCase("hackrule") && event.getRuleType().equalsIgnoreCase("hackrule")) {
        HackRule ruleDefault = (HackRule) rule;
        out.collect(
                new Keyed<>(
                        event, KeysExtractor.getKey(ruleDefault.getGroupingKeyNames(), event), rule.getRuleId()));
      } else {
        System.out.println("Not implemented");
      }
      ruleCounter++;
    }
    ruleCounterGauge.setValue(ruleCounter);
  }

  @Override
  public void processBroadcastElement(
      Rule rule, Context ctx, Collector<Keyed<Transaction, String, Integer>> out) throws Exception {
    log.info("{}", rule);
    BroadcastState<Integer, Rule> broadcastState =
        ctx.getBroadcastState(Descriptors.rulesDescriptor);
    handleRuleBroadcast(rule, broadcastState);
    if (rule.getRuleState() == RuleState.CONTROL) {
      handleControlCommand(rule.getControlType(), broadcastState);
    }
  }

  private void handleControlCommand(
      ControlType controlType, BroadcastState<Integer, Rule> rulesState) throws Exception {
    switch (controlType) {
      case DELETE_RULES_ALL:
        Iterator<Entry<Integer, Rule>> entriesIterator = rulesState.iterator();
        while (entriesIterator.hasNext()) {
          Entry<Integer, Rule> ruleEntry = entriesIterator.next();
          rulesState.remove(ruleEntry.getKey());
          log.info("Removed Rule {}", ruleEntry.getValue());
        }
        break;
    }
  }

  private static class RuleCounterGauge implements Gauge<Integer> {

    private int value = 0;

    public void setValue(int value) {
      this.value = value;
    }

    @Override
    public Integer getValue() {
      return value;
    }
  }
}
