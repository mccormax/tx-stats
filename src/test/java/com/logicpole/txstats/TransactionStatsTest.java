package com.logicpole.txstats;

import com.logicpole.txstats.dto.StatisticsDTO;
import com.logicpole.txstats.dto.TransactionDTO;

import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

/**
 * Test transaction statistics rest service
 *
 * @author Max McCormick
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment= SpringBootTest.WebEnvironment.RANDOM_PORT)
public class TransactionStatsTest {

   @Autowired
   private TestRestTemplate restTemplate;

   /**
    * Simple single-threaded test of transaction stats service.
    * This test simply posts 10 transactions of different amounts,
    * getting the statistics and checking the average after each
    * transaction is posted.
    */
   @Test
   public void testTransactionStatsSingleThreaded() {
      int count = 10;
      double sum = 0.0;
      for (int i=1; i<=count; i++) {
         double amt = i * 10.0;
         addTransaction(amt);
         StatisticsDTO stats = getStats();
         sum += amt;
         double avg = sum / i;
         assertThat(stats.getAvg(), is(avg));
      }
   }

   private void addTransaction(double amount) {
      HttpEntity<TransactionDTO> request = new HttpEntity<>(new TransactionDTO(System.currentTimeMillis(), amount));
      ResponseEntity<TransactionDTO> response = restTemplate
              .exchange("/transactions", HttpMethod.POST, request, TransactionDTO.class);
      assertThat(response.getStatusCode(), is(HttpStatus.CREATED));
   }

   private StatisticsDTO getStats() {
      return this.restTemplate.getForObject("/statistics", StatisticsDTO.class);
   }
}
