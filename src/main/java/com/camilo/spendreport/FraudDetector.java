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

package com.camilo.spendreport;

import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.KeyedProcessFunction;
import org.apache.flink.util.Collector;
import org.apache.flink.walkthrough.common.entity.Alert;
import org.apache.flink.walkthrough.common.entity.Transaction;

/**
 * Skeleton code for implementing a fraud detector.
 */
public class FraudDetector extends KeyedProcessFunction<Long, Transaction, Alert> {

	private static final long serialVersionUID = 1L;

	private static final double SMALL_AMOUNT = 1.00;
	private static final double LARGE_AMOUNT = 500.00;
	private static final long TEN_SECONDS = 10 * 1000;
	
	private transient ValueState<Boolean> flagState;
	private transient ValueState<Double> accountAggregate;
	private transient ValueState<Long> timerState;
	
	@Override
	public void open(Configuration parameters) {
		// initialize flag ValueState wrapper
		ValueStateDescriptor<Boolean> flagDescriptor = new ValueStateDescriptor<>(
				"flag",
				Types.BOOLEAN);
		flagState = getRuntimeContext().getState(flagDescriptor);
		
		// initialize counter ValueState wrapper
		ValueStateDescriptor<Double> aggregateDescriptor = new ValueStateDescriptor<Double>(
				"account-aggregate", 
				Types.DOUBLE);
		accountAggregate = getRuntimeContext().getState(aggregateDescriptor);
		
		// initialize timer ValueState wrapper
		ValueStateDescriptor<Long> timerDescriptor = new ValueStateDescriptor<>(
				"timer-state",
				Types.LONG);
		timerState = getRuntimeContext().getState(timerDescriptor);
	}

	@Override
	public void processElement(
			Transaction transaction,
			Context context,
			Collector<Alert> collector) throws Exception {
		
		Boolean lastTransactionWasSmall = flagState.value();
		
		if (lastTransactionWasSmall != null) { // since it is a flag, work only with null and not null... no need of true or false... nice.
			if (transaction.getAmount() > LARGE_AMOUNT) {
				
				Alert alert = new Alert();
				alert.setId(transaction.getAccountId());
				
				collector.collect(alert);
			}
			
			flagState.clear();
		}
		
		if (transaction.getAmount() < SMALL_AMOUNT) {
			flagState.update(true);
			
			long timer = context.timerService().currentProcessingTime() + TEN_SECONDS;
			context.timerService().registerProcessingTimeTimer(timer);
			timerState.update(timer);
		}
		
		if (accountAggregate.value() == null) {
			accountAggregate.update(new Double(0));
		}
		
		accountAggregate.update(accountAggregate.value() + transaction.getAmount());
		// TODO: add a Sink to display the aggregates...
		// System.out.println(Thread.currentThread().getName() + ": " + accountAggregate.value());
		
	}
	
	/**
	 * This method overriden method is called after context.timerService().registerProcessingTimeTimer(timer)
	 * fires the timer.
	 */
	@Override
	public void onTimer(long timestamp, OnTimerContext ctx, Collector<Alert> out) {
		timerState.clear();
		flagState.clear();
	}

}
