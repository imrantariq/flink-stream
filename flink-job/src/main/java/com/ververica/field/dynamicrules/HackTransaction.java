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

import lombok.*;

import java.math.BigDecimal;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

@Data
//@Builder
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
//@AllArgsConstructor
@Getter
public class HackTransaction extends Transaction {
//  public long transactionId;
//  public long eventTime;
  public String email;
  public long attempt;
//  private Long ingestionTimestamp;

  @Builder
  public HackTransaction(long transactionId, long eventTime, String ruleType, Long ingestionTimestamp, String email, long attempt) {
    super(transactionId, eventTime, ruleType, ingestionTimestamp);
    this.email = email;
    this.attempt = attempt;
  }

  private static transient DateTimeFormatter timeFormatter =
      DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
          .withLocale(Locale.US)
          .withZone(ZoneOffset.UTC);


  public static HackTransaction fromString(String line) {
    List<String> tokens = Arrays.asList(line.split(","));
    int numArgs = 5;
    if (tokens.size() != numArgs) {
      throw new RuntimeException(
          "Invalid transaction: "
              + line
              + ". Required number of arguments: "
              + numArgs
              + " found "
              + tokens.size());
    }

    HackTransaction transaction = new HackTransaction();

    try {
      Iterator<String> iter = tokens.iterator();
      transaction.transactionId = Long.parseLong(iter.next());
      transaction.eventTime =
          ZonedDateTime.parse(iter.next(), timeFormatter).toInstant().toEpochMilli();
      transaction.email = iter.next();
      transaction.attempt = Long.parseLong(iter.next());
      transaction.ingestionTimestamp = Long.parseLong(iter.next());
    } catch (NumberFormatException nfe) {
      throw new RuntimeException("Invalid record: " + line, nfe);
    }

    return transaction;
  }

  @Override
  public void assignIngestionTimestamp(Long timestamp) {
    this.ingestionTimestamp = timestamp;
  }
}
