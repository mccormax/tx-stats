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
	public void checkForCorrectStatsAfterAddingTenEqualTransactions() {

		// create some simple transaction values
		long now = System.currentTimeMillis();
		BigDecimal amount = new BigDecimal(10.10);

		// add these 10x
		for (int i=0; i<10; i++)
			 txStats.addTransaction(amount, now);

		// read back the stats
		StatsDTO stats = txStats.current();

		// check that the stats are correct
      // note:  there should be no effect of the 60 sec time window
		assertThat(stats.getAvg()).isEqualTo(10.1);
		assertThat(stats.getSum()).isEqualTo(101.0);
      assertThat(stats.getMax()).isEqualTo(10.1);
      assertThat(stats.getMin()).isEqualTo(10.1);
      assertThat(stats.getCount()).isEqualTo(10);
	}

}
