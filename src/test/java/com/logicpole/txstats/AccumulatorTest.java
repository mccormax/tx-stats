package com.logicpole.txstats;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Tests for transaction statistics application
 *
 * @author Max McCormick
 */
//@RunWith(SpringRunner.class)
//@SpringBootTest
public class AccumulatorTest {

   DoubleAccumulator transactions;

   @Before
   public void setUp() throws Exception {
       transactions = new DoubleAccumulator();
   }

   @Test
	public void ensureCorrectAveragingSingleThreaded()
           throws Exception {

		// accumulate a set of transactions
      int count = 10000;
      double total = 0;
		for (int i=0; i<count; i++) {

		   // generate some values
         long now = System.currentTimeMillis();
         double amount = i + 1 + ( (i % 100) / 100.0 );

         total += amount;
         transactions.accumulate(now, amount);
      }
      double sum = total;

		double avg = sum / count;
		double min = 1.00;
		double max = (double) count + ( ( count - 1 ) % 100 ) / 100.0;

		// read back the stats
		StatsDTO stats = transactions.statistics();

		// check that the stats are correct.  note:  there should be no effect of
      // the 60 sec time window. Since adding doubles could create small
      // rounding errors and yield slightly different results if the numbers are
      // added in different orders, compare values only to a fixed number of
      // decimal places.
      BigDecimal expected = new BigDecimal(avg).setScale(4, RoundingMode.HALF_DOWN);
      BigDecimal actual = new BigDecimal(stats.getAvg()).setScale(4, RoundingMode.HALF_DOWN);
		assertThat(actual.equals(expected));
	}

}
