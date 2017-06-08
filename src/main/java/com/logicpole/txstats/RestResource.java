package com.logicpole.txstats;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.util.Map;

/**
 * REST API resource for transaction statistics
 *
 * @author Max McCormick
 */
@EnableAutoConfiguration
@RestController
public class RestResource {

   private TransactionStatistics txStats;

   @Autowired
   public RestResource(TransactionStatistics txStats) {
      this.txStats = txStats;
   }

   @RequestMapping(method = RequestMethod.POST, value = "/transactions")
   void addTransaction(HttpServletResponse response, @RequestBody Map<String, String> payload) {
      Long timestamp = null;
      BigDecimal amount = null;
      try {
         timestamp = Long.parseLong(payload.get("timestamp"));
         amount = new BigDecimal(payload.get("amount"));
      } catch (Throwable t) {
         // ignore
      }
      if (timestamp == null || amount == null) {
         response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
         return;
      }
      if (txStats.addTransaction(amount, timestamp))
         response.setStatus(HttpServletResponse.SC_CREATED);
      else
         response.setStatus(HttpServletResponse.SC_NO_CONTENT);
   }

   @RequestMapping("/statistics")
   StatsDTO statistics() {
      return txStats.current();
   }

}
