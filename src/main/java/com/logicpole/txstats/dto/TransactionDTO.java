package com.logicpole.txstats.dto;

import java.io.Serializable;

/**
 * Data transfer object for transaction.
 *
 * @author Max McCormick
 */
public final class TransactionDTO implements Serializable {

   private static final long serialVersionUID = 1497016756L;

   private long timestamp;
   private double amount;

   // for auto-creation using introspection
   public TransactionDTO() {
   }

   public TransactionDTO(long timestamp, double amount) {
      this.timestamp = timestamp;
      this.amount = amount;
   }

   public long getTimestamp() {
      return timestamp;
   }

   public void setTimestamp(long timestamp) {
      this.timestamp = timestamp;
   }

   public double getAmount() {
      return amount;
   }

   public void setAmount(double amount) {
      this.amount = amount;
   }
}
