package com.hitechbilling.service;

import com.hitechbilling.entity.Invoice;

import java.util.List;

public interface InvoiceService {

    Invoice createInvoice(Invoice invoice);
    Invoice getInvoice(Long id);
    List<Invoice> getInvoices();
    Boolean updateInvoice(Invoice invoice, Long id);
    Boolean deleteInvoice(Long id);

    Boolean deleteInvoices();
}
