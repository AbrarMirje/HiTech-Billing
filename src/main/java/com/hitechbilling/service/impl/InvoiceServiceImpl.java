package com.hitechbilling.service.impl;

import com.hitechbilling.entity.Invoice;
import com.hitechbilling.entity.ItemDescription;
import com.hitechbilling.exception.InvoiceNotFoundException;
import com.hitechbilling.repository.InvoiceRepository;
import com.hitechbilling.service.InvoiceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class InvoiceServiceImpl implements InvoiceService {

    private final InvoiceRepository invoiceRepository;

    @Override
    @Transactional
    public Invoice createInvoice(Invoice invoice) {

        double totalAmount = 0.0;

        for (ItemDescription item : invoice.getDescriptions()){
            item.setAmount(item.getQuantity() * item.getRate());
            item.setInvoice(invoice);

            totalAmount += item.getAmount();
        }

        invoice.setTaxableAmount(totalAmount);
        invoice.setTotalAmount(totalAmount);
        invoice.setPendingAmount(invoice.getTotalAmount() - invoice.getReceivedAmount());

        return invoiceRepository.save(invoice);
    }


    @Override
    public Invoice getInvoice(Long id) {
        return invoiceRepository.findById(id).orElseThrow(
                () -> new InvoiceNotFoundException("Invoice not found with id " + id)
        );
    }

    @Override
    public List<Invoice> getInvoices() {
        return invoiceRepository.findAll();
    }

    @Override
    @Transactional
    public Boolean updateInvoice(Invoice invoice, Long id) {
        Invoice oldInvoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invoice not found with ID: " + id));

        if (invoice.getInvoiceDate() != null) oldInvoice.setInvoiceDate(invoice.getInvoiceDate());
        if (invoice.getDueDate() != null) oldInvoice.setDueDate(invoice.getDueDate());
        if (invoice.getCustomerName() != null) oldInvoice.setCustomerName(invoice.getCustomerName());
        if (invoice.getCustomerAddress() != null) oldInvoice.setCustomerAddress(invoice.getCustomerAddress());
        if (invoice.getCustomerMobile() != null) oldInvoice.setCustomerMobile(invoice.getCustomerMobile());
        if (invoice.getTermsAndConditions() != null) oldInvoice.setTermsAndConditions(invoice.getTermsAndConditions());
        if (invoice.getNote() != null) oldInvoice.setNote(invoice.getNote());
        if (invoice.getTotalAmountInWords() != null) oldInvoice.setTotalAmountInWords(invoice.getTotalAmountInWords());

        List<ItemDescription> oldDescriptions = oldInvoice.getDescriptions();

        if (invoice.getDescriptions() != null && !invoice.getDescriptions().isEmpty()) {
            if (oldDescriptions.size() != invoice.getDescriptions().size()) {
                throw new IllegalArgumentException("Mismatch in the number of descriptions. Cannot update.");
            }

            for (int i = 0; i < invoice.getDescriptions().size(); i++) {
                ItemDescription newItem = invoice.getDescriptions().get(i);
                ItemDescription existingItem = oldDescriptions.get(i);

                existingItem.setDescription(newItem.getDescription());
                existingItem.setSquareFt(newItem.getSquareFt());
                existingItem.setQuantity(newItem.getQuantity());
                existingItem.setRate(newItem.getRate());

                double itemAmount = newItem.getQuantity() * newItem.getRate();
                existingItem.setAmount(itemAmount);
            }
        }

        double totalAmount = oldDescriptions.stream()
                .mapToDouble(ItemDescription::getAmount)
                .sum();
        oldInvoice.setTotalAmount(totalAmount);

        oldInvoice.setTaxableAmount(totalAmount);

        if (invoice.getReceivedAmount() != null) {
            double newReceivedAmount = oldInvoice.getReceivedAmount() + invoice.getReceivedAmount();

            if (newReceivedAmount > totalAmount) {
                throw new IllegalArgumentException("Received amount cannot be greater than total amount.");
            }

            oldInvoice.setReceivedAmount(newReceivedAmount);
        }

        double pendingAmount = totalAmount - oldInvoice.getReceivedAmount();

        if (pendingAmount < 0) {
            throw new IllegalArgumentException("Pending amount cannot be negative. Check received amount.");
        }

        oldInvoice.setPendingAmount(pendingAmount);

        invoiceRepository.save(oldInvoice);
        return true;
    }


    @Override
    public Boolean deleteInvoice(Long id) {
        try {
            invoiceRepository.findById(id).orElseThrow(
                    () -> new InvoiceNotFoundException("Invoice not found with id " + id)
            );

            invoiceRepository.deleteById(id);
            return true;
        } catch (InvoiceNotFoundException e){
            throw new RuntimeException();
        }
    }
}
