/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.pinot.query.runtime.operator.window.range;

import java.util.ArrayList;
import java.util.List;
import org.apache.pinot.common.utils.DataSchema;
import org.apache.pinot.query.planner.logical.RexExpression;
import org.apache.pinot.query.runtime.operator.WindowAggregateOperator;


public class RankWindowFunction extends RangeWindowFunction {

  public RankWindowFunction(RexExpression.FunctionCall aggCall, String functionName, DataSchema inputSchema,
      WindowAggregateOperator.OrderSetInfo orderSetInfo) {
    super(aggCall, functionName, inputSchema, orderSetInfo);
  }

  @Override
  public List<Object> processRows(List<Object[]> rows) {
    int rank = 1;
    List<Object> result = new ArrayList<>();
    for (int i = 0; i < rows.size(); i++) {
      if (i > 0) {
        Object[] prevRow = rows.get(i - 1);
        Object[] currentRow = rows.get(i);
        if (compareRows(prevRow, currentRow) != 0) {
          rank = i + 1;
        }
      }
      result.add(rank);
    }
    return result;
  }
}
