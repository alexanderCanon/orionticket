package com.orionticket.payments.infrastructure.adapters.in.rest.dto;

import java.util.List;

public class PayoutListResponse {

    private List<PayoutResponse> payouts;
    private int page;
    private int totalPages;

    public PayoutListResponse() {
    }

    public PayoutListResponse(List<PayoutResponse> payouts, int page, int totalPages) {
        this.payouts = payouts;
        this.page = page;
        this.totalPages = totalPages;
    }

    public List<PayoutResponse> getPayouts() {
        return payouts;
    }

    public void setPayouts(List<PayoutResponse> payouts) {
        this.payouts = payouts;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }
}