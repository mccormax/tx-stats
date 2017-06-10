package com.logicpole.txstats.resource;

import com.logicpole.txstats.accumulate.DoubleAccumulator;
import com.logicpole.txstats.dto.StatisticsDTO;
import com.logicpole.txstats.dto.TransactionDTO;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * REST API resource for transaction statistics
 *
 * @author Max McCormick
 */
@EnableAutoConfiguration
@RestController
public class RestResource {

   /**
    * This object can accumulates double values and give simple statistics
    * based on data timestamped within the last minute
    */
   private DoubleAccumulator transactions;

   public RestResource() {
      this.transactions = new DoubleAccumulator();
   }

   /**
    * Create a new transaction.
    * <p>
    * Example body:
    * {
    * "amount": 12.3,
    * "timestamp": 1478192204000
    * }
    * <p>
    * Where:
    * amount  - transaction amount
    * timestamp  - transaction time in epoch in millis in UTC time zone.
    * <p>
    * Returns:  Empty body with either 201 or 204.
    * 201 - in case of success
    * 204 - if transaction is older than 60 seconds
    * <p>
    * Where:
    * amount  is a double specifying the amount
    * time  is a long specifying unix epoch time format in milliseconds
    *
    * @param response the http response object
    * @param transaction the http request payload, mapped into the correct dto.
    */
   @RequestMapping(method = RequestMethod.POST, value = "/transactions")
   public void addTransaction(HttpServletResponse response, @RequestBody TransactionDTO transaction) {
      /*long timestamp = 0;
      double amount = 0;
      try {
         timestamp = Long.parseLong(payload.get("timestamp"));
         amount = Double.parseDouble(payload.get("amount"));
      } catch (Throwable t) {
         // ignore
      }*/
      // make sure we have some realistic values.  a transaction of a negative
      // or zero amount would not make sense.
      if (transaction.getTimestamp() == 0 || transaction.getAmount() <= 0) {
         response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
         return;
      }
      if (transactions.accumulate(transaction.getTimestamp(), transaction.getAmount()))
         response.setStatus(HttpServletResponse.SC_CREATED);
      else
         response.setStatus(HttpServletResponse.SC_NO_CONTENT);
   }

   /**
    * Get current transaction statistics
    * <p>
    * Example Response:
    * {
    * "sum": 1000, "avg": 100, "max": 200, "min": 50, "count": 10
    * }
    * <p>
    * Where:
    * sum  is a double specifying the total sum of transaction value in the last 60 seconds
    * avg  is a double specifying the average amount of transaction value in the last 60 seconds
    * max  is a double specifying single highest transaction value in the last 60 seconds
    * min  is a double specifying single lowest transaction value in the last 60 seconds
    * count  is a long specifying the total number of transactions happened in the last 60 seconds
    *
    * @return the statistics as a data transfer object.
    */
   @RequestMapping("/statistics")
   public StatisticsDTO statistics() {
      return transactions.statistics();
   }

}
