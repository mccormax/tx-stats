package com.logicpole.txstats.accumulate;

import com.logicpole.txstats.dto.StatisticsDTO;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

/**
 * Double accumulator
 * <p>
 * This class is designed to accumulate timestamped double values over the last
 * 60 seconds, and provide simple statistics based on the accumulated values.
 *
 * @author Max McCormick
 */
public class DoubleAccumulator {

   private static final int MIN_IN_HOUR = 60;
   private static final int NUM_SLICES = MIN_IN_HOUR;
   private static final int MSEC_IN_MINUTE = MIN_IN_HOUR * 100;

   /*
    * Implementation Notes:
    *
    * Data is maintained in fixed-size arrays of primitives (for performance
    * reasons).  Each array index is associated with a "slice" of data.
    * For simplicity there are 60 slices, each of which is associated with one
    * second of data.  Data is assigned to a slice based on its timestamp,
    * specifically the second of minute.  For example, a data item with
    * a timestamp of 12:16:57.897 UTC would be assigned the slice 57.  The
    * minute of the timestamp is stored and used to determine the relevance of
    * the data, ie. whether it occurred within the last minute.
    */

   /*
    * The minute of the data values in a given slice.
    */
   private int[] sliceMinute;

   /*
    * The count of data values in a given slice.
    */
   private int[] sliceCount;

   /*
    * The sum of data values in a given slice.
    */
   private double[] sliceSum;

   /*
    * The maximum of the data values in a given slice.
    */
   private double[] sliceMax;

   /*
    * The minimum of the data values in a given slice.
    */
   private double[] sliceMin;

   /*
    *  Lock object to maintain thread safety
    */
   private final Object lock;

   /**
    * Construct an empty DoubleAccumulator which accumulates double data
    * values over a moving window of 60 seconds from the current instant
    * and back.
    */
   public DoubleAccumulator() {

      // initialize fixed arrays in which to accumulate data
      sliceMinute = new int[NUM_SLICES];
      sliceCount = new int[NUM_SLICES];
      sliceSum = new double[NUM_SLICES];
      sliceMax = new double[NUM_SLICES];
      sliceMin = new double[NUM_SLICES];

      lock = new Object();
   }

   /**
    * Accumulate a double value with the given timestamp.  Values with a
    * timestamp older than 60 seconds will be discarded.
    *
    * This function executes in approximately constant time and memory (O(1)).
    *
    * @param timestamp the unix epochtime (msec) associated with the data value.
    * @param value     the value to accumulate.
    * @return <tt>true</tt> if the data was accumulated, false if the timestamp
    * fell outside the current time window.
    */
   public boolean accumulate(long timestamp, double value) {

      // check whether timestamp is older than a minute and discard if outside
      // the window of interest.  note:  also discarding values from the future.
      // Although this scenario is possible due to clock drift we will not deal
      // with it here.
      long now = System.currentTimeMillis();
      if (timestamp <= now - MSEC_IN_MINUTE || timestamp > now)
         return false;

      // convert the moment of the timestamp into a "slice" used to index the
      // arrays containing the stored values
      Slice slice = getSlice(timestamp);

      synchronized (lock) {
         // clear the accrued amount if the slice minute is less than the
         // current one.  note:  we could also skip slices which don't yet
         // have a minute value, however over time this will become irrelevant
         // as the array becomes filled in
         if (sliceMinute[slice.second] < slice.minute)
            clearSlice(slice);

         // add the current value to the slice
         addToSlice(slice, value);
      }
      return true;
   }

   /**
    * Get the statistics corresponding to the data values accumulated over the
    * last 60 seconds.
    *
    * This function executes in approximately constant time and memory (O(1)).
    *
    * @return the statistics.
    */
   public StatisticsDTO statistics() {

      // get the slice for right now
      long now = System.currentTimeMillis();
      Slice slice = getSlice(now);

      // dynamically generate stats covering the last minute
      synchronized (lock) {
         return generateStats(slice);
      }
   }

   /**
    * Generate a time slice based on the given timestamp.
    */
   private Slice getSlice(long timestamp) {

      // use the timestamp and timezone to generate minute and second values
      // note:  the timezone doesn't really matter in this case
      Instant instant = Instant.ofEpochMilli(timestamp);
      ZonedDateTime zdt = instant.atZone(ZoneOffset.UTC);

      return new Slice(zdt.getMinute(), zdt.getSecond());
   }

   /**
    * Add the given data value to the given slice
    */
   private void addToSlice(Slice slice, double value) {
      // increment count of data values in slice
      sliceCount[slice.second]++;
      // add value to the slice sum
      sliceSum[slice.second] += value;
      // set slice minimum if less than current
      if (value < sliceMin[slice.second] || sliceMin[slice.second] == 0)
         sliceMin[slice.second] = value;
      // set slice maximum if greater than current
      if (value > sliceMax[slice.second])
         sliceMax[slice.second] = value;
      // also set to the current minute
      // note:  this could be conditional on count==0
      sliceMinute[slice.second] = slice.minute;
   }

   /**
    * Clear data in the given slice
    */
   private void clearSlice(Slice slice) {
      sliceSum[slice.second] = 0;
      sliceCount[slice.second] = 0;
      sliceMin[slice.second] = 0;
      sliceMax[slice.second] = 0;
   }

   /**
    * Generate statistics based on the instant represented by the slice.
    */
   private StatisticsDTO generateStats(Slice slice) {
      int count = 0;
      double min = 0;
      double max = 0;
      double sum = 0;

      // iterate through all the slices
      for (int i = 0; i < NUM_SLICES; i++) {

         // use the current minute up until we hit the current second
         // then switch to the previous minute.  this is how the minute
         // rollover is handled.
         // 0  .... 31 32 33 34 35 ..... 59 - second
         // 15 .... 15 15 15 14 14 ..... 14 - minute
         //                |
         //               now
         if (i == slice.second + 1) {
            slice.minute -= 1;
            if (slice.minute < 0)
               slice.minute = 59;
         }
         // skip if there is no data in the slice
         if (sliceCount[i] == 0)
            continue;

         // if the minute doesn't match then clear it out.  this will ensure
         // that old data from N hours ago doesn't get into the stats
         if (sliceMinute[i] != slice.minute) {
            clearSlice(slice);
            continue;
         }
         // if so, update sum, count, max and min values
         sum += sliceSum[i];
         count += sliceCount[i];
         if (sliceMin[i] < min || min == 0)
            min = sliceMin[i];
         if (sliceMax[i] > max)
            max = sliceMax[i];
      }
      // generate average based on sum and count
      double avg = 0;
      if (count > 0)
         avg = sum / count;

      return new StatisticsDTO(sum, avg, max, min, count);
   }

   /**
    * Simple data structure to contain the minute and second coordinates of a
    * slice.
    */
   private class Slice {

      private int minute;
      private int second;

      private Slice(int minute, int second) {
         this.minute = minute;
         this.second = second;
      }
   }
}
