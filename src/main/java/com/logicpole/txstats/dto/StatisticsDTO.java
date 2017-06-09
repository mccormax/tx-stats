package com.logicpole.txstats.dto;

/**
 * Data transfer object for statistics of a set of double values
 *
 * @author Max McCormick
 */
public final class StatisticsDTO {

   private static final long serialVersionUID = 1497016756L;

   private double sum;
   private double avg;
   private double max;
   private double min;
   private long count;

   // default constructor needed for test
   public StatisticsDTO() {
   }

   public StatisticsDTO(double sum,
                        double avg,
                        double max,
                        double min,
                        long count) {
      this.sum = sum;
      this.avg = avg;
      this.max = max;
      this.min = min;
      this.count = count;
   }

   public double getSum() {
      return sum;
   }

   public double getAvg() {
      return avg;
   }

   public double getMax() {
      return max;
   }

   public double getMin() {
      return min;
   }

   public long getCount() {
      return count;
   }
}
