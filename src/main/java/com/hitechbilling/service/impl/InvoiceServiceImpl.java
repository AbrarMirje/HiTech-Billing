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
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class InvoiceServiceImpl implements InvoiceService {

    private final InvoiceRepository invoiceRepository;

    @Override
    public Invoice createInvoice(Invoice invoice) {
        double totalAmount = 0.0;

        for (ItemDescription item : invoice.getDescriptions()) {
            item.setInvoice(invoice);

            // Size conversion logic
            int squareFeet = convertToSquareFeet(item.getSize());
            item.setSquareFt(squareFeet);

            // Amount Calculation
            double originalItemAmount = item.getQuantity() * item.getRate();
            double itemAmount = originalItemAmount;

            // Discount Handling
            if (item.getDiscountAmount() != null && item.getDiscountAmount() > 0) {
                double discountAmount = item.getDiscountAmount();
                item.setDiscountAmountPercentage((discountAmount / originalItemAmount) * 100);
                itemAmount -= discountAmount;
            }

            item.setAmount(itemAmount);
            totalAmount += itemAmount;
        }

        // Invoice Level Discount
        if (invoice.getDiscountAmount() != null && invoice.getDiscountAmount() > 0) {
            double discountAmount = invoice.getDiscountAmount();
            invoice.setDiscountAmountPercentage((discountAmount / totalAmount) * 100);
            totalAmount -= discountAmount;
        }

        // GST Calculation
        double gstAmount = calculateGST(invoice, totalAmount);
        totalAmount += gstAmount;

        // Final Amounts
        invoice.setTotalAmount(totalAmount);
        invoice.setPendingAmount(totalAmount - Optional.ofNullable(invoice.getReceivedAmount()).orElse(0.0));

        return invoiceRepository.save(invoice);
    }


    @Override
    public Invoice getInvoice(Long id) {
        return invoiceRepository.findById(id).orElseThrow(() -> new InvoiceNotFoundException("Invoice not found with id " + id));
    }

    @Override
    public List<Invoice> getInvoices() {
        return invoiceRepository.findAll();
    }

    @Override
    @Transactional
    public Boolean updateInvoice(Invoice invoice, Long id) {
        Invoice oldInvoice = getInvoice(id);

        oldInvoice.setInvoiceDate(invoice.getInvoiceDate());
        oldInvoice.setDueDate(invoice.getDueDate());
        oldInvoice.setCustomerName(invoice.getCustomerName());
        oldInvoice.setCustomerAddress(invoice.getCustomerAddress());
        oldInvoice.setCustomerMobile(invoice.getCustomerMobile());
        oldInvoice.setTermsAndConditions(invoice.getTermsAndConditions());
        oldInvoice.setNote(invoice.getNote());
        oldInvoice.setTotalAmountInWords(invoice.getTotalAmountInWords());

        List<ItemDescription> oldDescriptions = oldInvoice.getDescriptions();
        double totalAmount = 0.0;

        if (invoice.getDescriptions() != null && !invoice.getDescriptions().isEmpty()) {
            for (int i = 0; i < invoice.getDescriptions().size(); i++) {
                ItemDescription newItem = invoice.getDescriptions().get(i);

                ItemDescription existingItem = (i < oldDescriptions.size())
                        ? oldDescriptions.get(i)
                        : new ItemDescription();

                existingItem.setInvoice(oldInvoice);
                existingItem.setDescription(newItem.getDescription());
                existingItem.setSize(newItem.getSize());
                existingItem.setQuantity(newItem.getQuantity());
                existingItem.setRate(newItem.getRate());

                // Size conversion logic
                int squareFeet = convertToSquareFeet(newItem.getSize());
                existingItem.setSquareFt(squareFeet);

                double originalItemAmount = newItem.getQuantity() * newItem.getRate();
                double itemAmount = originalItemAmount;

                // Discount Handling
                double discountAmount = Optional.ofNullable(newItem.getDiscountAmount()).orElse(0.0);
                existingItem.setDiscountAmount(discountAmount);

                if (discountAmount > 0) {
                    double discountPercentage = (discountAmount / originalItemAmount) * 100;
                    existingItem.setDiscountAmountPercentage(discountPercentage);
                    itemAmount -= discountAmount;
                }

                existingItem.setAmount(itemAmount);
                totalAmount += itemAmount;

                if (i >= oldDescriptions.size()) {
                    oldDescriptions.add(existingItem);
                }
            }
        }

        // Invoice Level Discount
        double invoiceDiscountAmount = Optional.ofNullable(invoice.getDiscountAmount()).orElse(0.0);
        oldInvoice.setDiscountAmount(invoiceDiscountAmount);

        if (invoiceDiscountAmount > 0) {
            double discountPercentage = (invoiceDiscountAmount / totalAmount) * 100;
            oldInvoice.setDiscountAmountPercentage(discountPercentage);
            totalAmount -= invoiceDiscountAmount;
        }

        // GST Calculation
        double gstAmount = calculateGST(invoice, totalAmount);
        totalAmount += gstAmount;

        // 🛠 **Fix Received Amount Calculation**
        double updatedReceivedAmount = Optional.ofNullable(oldInvoice.getReceivedAmount()).orElse(0.0)
                + Optional.ofNullable(invoice.getReceivedAmount()).orElse(0.0);
        oldInvoice.setReceivedAmount(updatedReceivedAmount);

        // Update Pending Amount
        oldInvoice.setPendingAmount(totalAmount - updatedReceivedAmount);

        oldInvoice.setTotalAmount(totalAmount);

        invoiceRepository.save(oldInvoice);
        return true;
    }

    private double calculateGST(Invoice invoice, double totalAmount) {
        if (invoice.getIgst() != null && invoice.getIgst() > 0) {
            invoice.setCgst(0.0);
            invoice.setSgst(0.0);
            return (totalAmount * invoice.getIgst()) / 100;
        } else {
            invoice.setIgst(0.0);
            return ((totalAmount * Optional.ofNullable(invoice.getSgst()).orElse(0.0)) / 100)
                    + ((totalAmount * Optional.ofNullable(invoice.getCgst()).orElse(0.0)) / 100);
        }
    }

    private int convertToSquareFeet(String size) {
        Pattern pattern = Pattern.compile("(\\d+)");
        Matcher matcher = pattern.matcher(size);
        int value = matcher.find() ? Integer.parseInt(matcher.group(1)) : 0;

        if (size.toUpperCase().contains("MM")) {
            return (int) Math.round(value * 0.010764);
        } else if (size.toUpperCase().contains("INCH")) {
            return (int) Math.round(value * 0.083333);
        }
        return value;
    }

    @Override
    public Boolean deleteInvoice(Long id) {
        invoiceRepository.findById(id).orElseThrow(() -> new InvoiceNotFoundException("Invoice not found with id " + id));
        invoiceRepository.deleteById(id);
        return true;
    }

    @Override
    public Boolean deleteInvoices() {
        invoiceRepository.deleteAll();
        return true;
    }
}
