package com.logicpole.txstats;

import static org.assertj.core.api.Assertions.assertThat;

import com.logicpole.txstats.accumulate.DoubleAccumulator;
import com.logicpole.txstats.dto.StatisticsDTO;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

/**
 * Tests for transaction statistics application
 *
 * @author Max McCormick
 */
public class AccumulatorTest {

   private DoubleAccumulator transactions;

   @Before
   public void setUp() throws Exception {
      transactions = new DoubleAccumulator();
   }

   /**
    * Generate a short burst of transactions and check that average is
    * calculated correctly.  This does not test for thread safety of the
    * DoubleAccumulator, nor does it verify that data is discarded properly.
    */
   @Test
   public void ensureCorrectAveragingSingleThreaded()
           throws Exception {

      // accumulate a set of transactions
      int count = 10000;
      double total = 0;
      for (int i = 0; i < count; i++) {

         // generate some values
         long now = System.currentTimeMillis();
         double amount = i + 1 + ((i % 100) / 100.0);

         total += amount;
         transactions.accumulate(now, amount);
      }
      double sum = total;

      double avg = sum / count;
      double min = 1.00;
      double max = (double) count + ((count - 1) % 100) / 100.0;

      // read back the stats
      StatisticsDTO stats = transactions.statistics();

      // check that the stats are correct.  note:  there should be no effect of
      // the 60 sec time window. Since adding doubles could create small
      // rounding errors and yield slightly different results if the numbers are
      // added in different orders, compare values only to a fixed number of
      // decimal places.
      BigDecimal expected = new BigDecimal(avg).setScale(4, RoundingMode.HALF_DOWN);
      BigDecimal actual = new BigDecimal(stats.getAvg()).setScale(4, RoundingMode.HALF_DOWN);
      assertThat(actual.equals(expected));
   }

   /**
    * Add a transaction every 10 seconds for 2 minutes and verify that the
    * sum never exceeds the sum it had at 1 minute.  This tests that the
    * time windowing functionality works properly.
    * <p>
    * Note:  @Ignore because testing this take over 2 minutes to execute.
    *
    * @throws Exception on exception
    */
   @Ignore
   @Test
   public void ensureConstantSumOverTime()
           throws Exception {

      int count = 0;
      while (count < 12) {
         System.out.println(count + ": Testing in progress, please wait...");

         // add a sample every 10 secs
         ZonedDateTime zdt;
         while (true) {
            Thread.sleep(1000);
            Instant instant = Instant.now();
            zdt = instant.atZone(ZoneOffset.UTC);
            if (zdt.getSecond() % 10 == 0)
               break;
         }

         // store a value of 10.0
         transactions.accumulate(System.currentTimeMillis(), 10.0);
         count++;

         StatisticsDTO stats = transactions.statistics();
         if (count > 6)
            assertThat(stats.getSum()).isEqualTo(60.0);
         else
            assertThat(stats.getSum()).isEqualTo(count * 10.0);
      }
   }
}
