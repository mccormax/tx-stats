package com.logicpole.txstats;

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

   private DoubleAccumulator transactions;

   public RestResource() {
      this.transactions = new DoubleAccumulator();
   }

   @RequestMapping(method = RequestMethod.POST, value = "/transactions")
   void addTransaction(HttpServletResponse response, @RequestBody Map<String, String> payload) {
      long timestamp = 0;
      double amount = 0;
      try {
         timestamp = Long.parseLong(payload.get("timestamp"));
         amount = Double.parseDouble(payload.get("amount"));
      } catch (Throwable t) {
         // ignore
      }
      // make sure we have some realistic values.  a transaction of a negative
      // or zero amount would not make sense.
      if (timestamp == 0 || amount <= 0) {
         response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
         return;
      }
      if (transactions.accumulate(timestamp, amount))
         response.setStatus(HttpServletResponse.SC_CREATED);
      else
         response.setStatus(HttpServletResponse.SC_NO_CONTENT);
   }

   @RequestMapping("/statistics")
   StatsDTO statistics() {
      return transactions.statistics();
   }

}
