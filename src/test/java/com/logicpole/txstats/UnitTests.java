package com.logicpole.txstats;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;

/**
 * Tests for transaction statistics application
 *
 * @author Max McCormick
 */
//@RunWith(SpringRunner.class)
//@SpringBootTest
public class UnitTests {

   TransactionStatistics txStats;

   @Before
   public void setUp() throws Exception {
       txStats = new TransactionStatistics();
   }

   @Test
	public void checkForCorrectStatsAfterAddingTenEqualTransactions()
           throws Exception {

		// create some simple transaction values
      int count = 10000;

		// add count transactions
      double total = 0;
		for (int i=0; i<count; i++) {
         long now = System.currentTimeMillis();
         String amt = String.format("%02d.%02d", i+1, i%100);
         BigDecimal amount = new BigDecimal(amt);
         total += amount.doubleValue();

         txStats.addTransaction(amount, now);
      }
      double sum = total;

		double avg = sum / count;
		double min = 1.00;
		double max = (double) count + ( ( count - 1 ) % 100 ) / 100.0;

		// read back the stats
		StatsDTO stats = txStats.current();

		// check that the stats are correct
      // note:  there should be no effect of the 60 sec time window
		assertThat(stats.getAvg()).isEqualTo(avg);
		assertThat(stats.getSum()).isEqualTo(sum);
      assertThat(stats.getMax()).isEqualTo(max);
      assertThat(stats.getMin()).isEqualTo(min);
      assertThat(stats.getCount()).isEqualTo(count);
	}

}
