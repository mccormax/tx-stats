package com.logicpole.txstats;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;

/**
 * Transaction statistics
 *
 * @author Max McCormick
 */
@Component
public class TransactionStatistics {

   private static final int NUM_SLICES = 60;
   private static final int MIN_IN_HOUR = 60;
   private static final int MSEC_IN_MINUTE = MIN_IN_HOUR * 100;

   private double[] sliceAmount;
   private double[] sliceSum;
   private int[] sliceMax;
   private int[] sliceMin;
   private int[] sliceMinute;
   private int[] sliceCount;

   TransactionStatistics() {
      sliceAmount = new double[NUM_SLICES];
      sliceSum = new double[NUM_SLICES];
      sliceMax = new int[NUM_SLICES];
      sliceMin = new int[NUM_SLICES];
      sliceMinute = new int[NUM_SLICES];
      sliceCount = new int[NUM_SLICES];
   }

   boolean addTransaction(BigDecimal amount, long epochTime) {

      long now = System.currentTimeMillis();

      // check whether timestamp is older than a minute and discard if outside
      // the window of interest.  note:  also discarding points in the future.
      // though this scenario is possible due to clock drift we will not deal
      // with it here.
      if (epochTime < now - MSEC_IN_MINUTE || epochTime > now)
         return false;

      int amountInCents = amount.scaleByPowerOfTen(2).intValue();
      // it doesn't make sense to have amounts less than or equal to zero
      if (amountInCents <= 0) {
         System.out.println("Uh-oh... don't like this amount: "+amountInCents);
         return false;
      }
      int minute = getMinute(epochTime);

      // convert the moment of the transaction into a "slice" which indexes
      // into the arrays containing the stored data
      int slice =  getSlice(epochTime);

      System.out.println("amountInCents="+amountInCents+", minute="+minute+
              ", slice="+slice);

      synchronized(sliceAmount) {

         // clear the accrued amount if the slice minute is less than the
         // current one.  note:  we could also check for minute > 0 but over
         // time this will become irrelevant as the array becomes filled in
         if (sliceMinute[slice] < minute) {

            // data is from a previous minute, so clear it out
            sliceAmount[slice] = 0;
            sliceSum[slice] = 0;
            sliceCount[slice] = 0;
            sliceMin[slice] = 0;
            sliceMax[slice] = 0;
         }
         // incrementally update the current amount average.
         sliceCount[slice]++;
         sliceAmount[slice] += ( amountInCents - sliceAmount[slice] ) / sliceCount[slice];

         sliceSum[slice] += amountInCents;

         if (amountInCents < sliceMin[slice] || sliceMin[slice] == 0)
            sliceMin[slice] = amountInCents;

         if (amountInCents > sliceMax[slice])
            sliceMax[slice] = amountInCents;

         // also set to the current minute
         // note:  this could be conditional on count==0
         sliceMinute[slice] = minute;
      }
      return true;
   }

   StatsDTO current() {
      long now = System.currentTimeMillis();
      int minute = getMinute(now);
      int lastMinute = minute - 1;
      int slice = getSlice(now);

      // traverse the first part of the array which deals the current minute
      // up until now
      double sum = 0;
      double min = 0;
      double max = 0;
      double sumSum = 0;
      int count = 0;

      for (int i=0; i<=slice; i++) {

         if (sliceCount[i] > 0 && sliceMinute[i] == minute) {
            sum += sliceAmount[i];
            sumSum += sliceSum[i];
            count += sliceCount[i];
            if (sliceMin[i] < min || min == 0)
               min = sliceMin[i];
            if (sliceMax[i] > max)
               max = sliceMax[i];
         }
      }
      for (int i=slice+1; i<NUM_SLICES; i++) {

         if (sliceCount[i] > 0 && sliceMinute[i] == lastMinute) {
            sum += sliceAmount[i];
            sumSum += sliceSum[i];
            count += sliceCount[i];
            if (sliceMin[i] < min || min == 0)
               min = sliceMin[i];
            if (sliceMax[i] > max)
               max = sliceMax[i];
         }
      }
      double avg = 0;
      if (count > 0)
         avg = sumSum / count;

      double sumDouble = sum / 100.0;

      avg /= 100;
      max /= 100;
      min /= 100;
      sumSum /= 100;

      return new StatsDTO(sumSum, avg, max, min, count);
   }

   private int getSlice(long epochTime) {

      Instant timestamp = Instant.ofEpochMilli(epochTime);

      ZonedDateTime zdt = timestamp.atZone(ZoneId.systemDefault());

      // KISS:
      return zdt.getSecond();
   }

   private int getMinute(long epochTime) {

      Instant timestamp = Instant.ofEpochMilli(epochTime);

      ZonedDateTime zdt = timestamp.atZone(ZoneId.systemDefault());

      return zdt.getMinute();
   }
}
