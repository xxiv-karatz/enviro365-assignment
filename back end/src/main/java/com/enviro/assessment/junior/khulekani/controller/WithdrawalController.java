package com.enviro.assessment.junior.khulekani.controller;

import com.enviro.assessment.junior.khulekani.dto.WithdrawalRequest;
import com.enviro.assessment.junior.khulekani.dto.WithdrawalResponse;
import com.enviro.assessment.junior.khulekani.model.WithdrawalType;
import com.enviro.assessment.junior.khulekani.service.WithdrawalService;
import jakarta.validation.Valid;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/api/withdrawals")
public class WithdrawalController {
    private final WithdrawalService withdrawalService;

    public WithdrawalController(WithdrawalService withdrawalService) {
        this.withdrawalService = withdrawalService;
    }

    @PostMapping
    public ResponseEntity<WithdrawalResponse> submitWithdrawal(@Valid @RequestBody WithdrawalRequest request) {
        WithdrawalResponse response = withdrawalService.submitWithdrawal(request);
        // There isn't a single-resource GET endpoint for one withdrawal notice yet, so the
        // Location header points at the investor's withdrawal history - the closest existing
        // representation of the resource that was just created.
        URI location = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/api/withdrawals/{investorId}")
                .buildAndExpand(response.getInvestorId())
                .toUri();
        return ResponseEntity.created(location).body(response);
    }

    @GetMapping("/{investorId}")
    public List<WithdrawalResponse> getHistory(@PathVariable Long investorId) {
        return withdrawalService.getHistory(investorId);
    }

    @GetMapping("/{investorId}/export")
    public ResponseEntity<byte[]> exportStatement(
            @PathVariable Long investorId,
            @RequestParam(required = false) WithdrawalType type,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        if (from != null && to != null && from.isAfter(to)) {
            throw new IllegalArgumentException("from date must not be after to date");
        }
        List<WithdrawalResponse> withdrawals = withdrawalService.findForExport(investorId, type, from, to);
        String csv = toCsv(withdrawals);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(new MediaType("text", "csv", StandardCharsets.UTF_8));
        headers.setContentDisposition(ContentDisposition.attachment().filename("withdrawal-statement.csv").build());
        return ResponseEntity.ok().headers(headers).body(csv.getBytes(StandardCharsets.UTF_8));
    }

    private String toCsv(List<WithdrawalResponse> withdrawals) {
        StringBuilder csv = new StringBuilder("id,investorId,amount,type,status,requestDate,reason\n");
        for (WithdrawalResponse withdrawal : withdrawals) {
            csv.append(value(withdrawal.getId())).append(',')
                    .append(value(withdrawal.getInvestorId())).append(',')
                    .append(value(withdrawal.getAmount())).append(',')
                    .append(value(withdrawal.getType())).append(',')
                    .append(value(withdrawal.getStatus())).append(',')
                    .append(value(withdrawal.getRequestDate())).append(',')
                    .append(value(withdrawal.getReason())).append('\n');
        }
        return csv.toString();
    }

    private String value(Object value) {
        if (value == null) {
            return "";
        }
        String text = value.toString();
        if (text.contains(",") || text.contains("\"") || text.contains("\n")) {
            return "\"" + text.replace("\"", "\"\"") + "\"";
        }
        return text;
    }
}
