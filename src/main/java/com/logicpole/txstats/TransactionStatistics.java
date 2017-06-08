package com.logicpole.txstats;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Transaction statistics
 *
 * @author Max McCormick
 */
@Component
public class TransactionStatistics {

   boolean addTransaction(BigDecimal amount, Long epochTime) {
      return true;
   }

   StatsDTO current() {
      return new StatsDTO(1.0, 2.0, 3.0, 4.0, 5);
   }
}
