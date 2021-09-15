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

package com.ververica.field.dynamicrules;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.ververica.field.dynamicrules.Rule.AggregatorFunctionType;
import com.ververica.field.dynamicrules.DefaultRule.LimitOperatorType;
import com.ververica.field.dynamicrules.Rule.RuleState;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.ObjectMapper;

public class RuleParser {

  private final ObjectMapper objectMapper = new ObjectMapper();

  public Rule fromString(String line) throws IOException {
    if (line.length() > 0 && '{' == line.charAt(0)) {
      return parseJson(line);
    } else {
      return parsePlain(line);
    }
  }

  private Rule parseJson(String ruleString) throws IOException {
    System.out.println(ruleString);
    JsonElement j_element = new JsonParser().parse(ruleString);
    JsonObject j_object = j_element.getAsJsonObject();
    String ruleType = j_object.get("ruleType").getAsString();
    System.out.println(ruleType);

    if (ruleType.equalsIgnoreCase("default")){
      return objectMapper.readValue(ruleString, DefaultRule.class);
    }
    else if (ruleType.equalsIgnoreCase("hackrule")) {
      return objectMapper.readValue(ruleString, HackRule.class);
    } else {
      System.out.println("Wrong Rule Type");
      return null;
    }

  }

  private static Rule parsePlain(String ruleString) throws IOException {
    List<String> tokens = Arrays.asList(ruleString.split(","));
    if (tokens.size() != 9) {
      throw new IOException("Invalid rule (wrong number of tokens): " + ruleString);
    }

    Iterator<String> iter = tokens.iterator();
    String ruleType = iter.next();
    System.out.println(ruleType);
    Rule rule = new Rule();
    rule.setRuleType(ruleType);
    if (rule.getRuleType().equalsIgnoreCase("default")){
      DefaultRule defaultRule = (DefaultRule) rule;
      defaultRule.setRuleId(Integer.parseInt(stripBrackets(iter.next())));
      defaultRule.setRuleState(RuleState.valueOf(stripBrackets(iter.next()).toUpperCase()));
      defaultRule.setGroupingKeyNames(getNames(iter.next()));
      defaultRule.setUnique(getNames(iter.next()));
      defaultRule.setAggregateFieldName(stripBrackets(iter.next()));
      defaultRule.setAggregatorFunctionType(
          AggregatorFunctionType.valueOf(stripBrackets(iter.next()).toUpperCase()));
      defaultRule.setLimitOperatorType(LimitOperatorType.fromString(stripBrackets(iter.next())));
      defaultRule.setLimit(new BigDecimal(stripBrackets(iter.next())));
      defaultRule.setWindowMinutes(Integer.parseInt(stripBrackets(iter.next())));
      return defaultRule;
    } else {
      System.out.println("No other rule type implemented");
      return null;
    }


  }

  private static String stripBrackets(String expression) {
    return expression.replaceAll("[()]", "");
  }

  private static List<String> getNames(String expression) {
    String keyNamesString = expression.replaceAll("[()]", "");
    if (!"".equals(keyNamesString)) {
      String[] tokens = keyNamesString.split("&", -1);
      return Arrays.asList(tokens);
    } else {
      return new ArrayList<>();
    }
  }
}
