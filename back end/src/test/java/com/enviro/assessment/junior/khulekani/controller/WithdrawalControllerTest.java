package com.enviro.assessment.junior.khulekani.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.enviro.assessment.junior.khulekani.dto.WithdrawalRequest;
import com.enviro.assessment.junior.khulekani.dto.WithdrawalResponse;
import com.enviro.assessment.junior.khulekani.exception.BusinessRuleViolationException;
import com.enviro.assessment.junior.khulekani.model.Investor;
import com.enviro.assessment.junior.khulekani.model.Portfolio;
import com.enviro.assessment.junior.khulekani.model.WithdrawalNotice;
import com.enviro.assessment.junior.khulekani.model.WithdrawalStatus;
import com.enviro.assessment.junior.khulekani.model.WithdrawalType;
import com.enviro.assessment.junior.khulekani.service.WithdrawalService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(WithdrawalController.class)
class WithdrawalControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private WithdrawalService withdrawalService;

    @Test
    void submitWithdrawalReturnsCreatedWithLocationOnSuccess() throws Exception {
        when(withdrawalService.submitWithdrawal(any()))
                .thenReturn(sampleResponse(WithdrawalStatus.APPROVED, "Withdrawal approved"));

        mockMvc.perform(post("/api/withdrawals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleRequest())))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.status").value("APPROVED"));
    }

    @Test
    void submitWithdrawalReturnsBadRequestWithRejectionWhenBusinessRuleFails() throws Exception {
        WithdrawalResponse rejection = sampleResponse(WithdrawalStatus.REJECTED,
                "Withdrawal amount must not exceed the portfolio balance");
        when(withdrawalService.submitWithdrawal(any()))
                .thenThrow(new BusinessRuleViolationException(
                        "Withdrawal amount must not exceed the portfolio balance", rejection));

        mockMvc.perform(post("/api/withdrawals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleRequest())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Withdrawal amount must not exceed the portfolio balance"))
                .andExpect(jsonPath("$.rejection.status").value("REJECTED"));
    }

    @Test
    void submitWithdrawalReturnsFieldValidationErrorsForMissingFields() throws Exception {
        mockMvc.perform(post("/api/withdrawals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validationErrors.investorId").exists())
                .andExpect(jsonPath("$.validationErrors.amount").exists())
                .andExpect(jsonPath("$.validationErrors.type").exists());
    }

    @Test
    void getHistoryReturnsWithdrawalsForInvestor() throws Exception {
        when(withdrawalService.getHistory(1L))
                .thenReturn(List.of(sampleResponse(WithdrawalStatus.APPROVED, "Withdrawal approved")));

        mockMvc.perform(get("/api/withdrawals/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("APPROVED"));
    }

    @Test
    void exportStatementReturnsCsvWithContentType() throws Exception {
        when(withdrawalService.findForExport(eq(1L), any(), any(), any()))
                .thenReturn(List.of(sampleResponse(WithdrawalStatus.APPROVED, "Withdrawal approved")));

        mockMvc.perform(get("/api/withdrawals/1/export"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("text/csv;charset=UTF-8"));
    }

    @Test
    void exportStatementRejectsFromDateAfterToDate() throws Exception {
        mockMvc.perform(get("/api/withdrawals/1/export")
                        .param("from", "2026-02-01")
                        .param("to", "2026-01-01"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("from date must not be after to date"));
    }

    private WithdrawalRequest sampleRequest() {
        WithdrawalRequest request = new WithdrawalRequest();
        request.setInvestorId(1L);
        request.setAmount(new BigDecimal("250.00"));
        request.setType(WithdrawalType.STANDARD);
        return request;
    }

    private WithdrawalResponse sampleResponse(WithdrawalStatus status, String reason) {
        Investor investor = new Investor("Test Investor", 70, "test@example.com");
        investor.setId(1L);
        Portfolio portfolio = new Portfolio(investor, new BigDecimal("1000.00"));
        investor.setPortfolio(portfolio);
        WithdrawalNotice notice = new WithdrawalNotice(portfolio, new BigDecimal("250.00"),
                WithdrawalType.STANDARD, status, LocalDateTime.now(), reason);
        notice.setId(1L);
        return WithdrawalResponse.from(notice);
    }
}
