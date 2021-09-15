package com.ververica.field.dynamicrules;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

public class TransactionJsonMapper<T> {

//  private final Class<T> targetClass;
  private final ObjectMapper objectMapper;

  public TransactionJsonMapper() {
//    this.targetClass = targetClass;
    objectMapper = new ObjectMapper();
  }

  public T fromString(String line) throws IOException {
    System.out.println(line);
    JsonElement j_element = new JsonParser().parse(line);
    JsonObject j_object = j_element.getAsJsonObject();
    String ruleType = j_object.get("ruleType").getAsString();
    System.out.println(ruleType);
    if (ruleType.equalsIgnoreCase("default")){
        DefaultTransaction defaultTransaction = objectMapper.readValue(line, DefaultTransaction.class);
        return (T) defaultTransaction;
    }
    else if (ruleType.equalsIgnoreCase("hackrule")) {
      HackTransaction hackTransaction = objectMapper.readValue(line, HackTransaction.class);
      return (T) hackTransaction;
    } else {
      System.out.println("Wrong Rule Type");
      return null;
    }

  }

  public String toString(T line) throws IOException {
    return objectMapper.writeValueAsString(line);
  }
}
