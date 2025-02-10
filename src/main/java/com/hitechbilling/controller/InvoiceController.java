package com.hitechbilling.controller;

import com.hitechbilling.entity.Invoice;
import com.hitechbilling.response.CustomResponse;
import com.hitechbilling.service.InvoiceService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin("*")
@RequestMapping("/invoice")
@RequiredArgsConstructor
@Tag(name = "Invoice APIs")
public class InvoiceController {

    private final InvoiceService invoiceService;

    @PostMapping("/create")
    public ResponseEntity<Invoice> createInvoice(@Valid @RequestBody Invoice invoice) {
        return ResponseEntity.ok(invoiceService.createInvoice(invoice));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Invoice> getInvoice(@PathVariable Long id) {
        return ResponseEntity.ok(invoiceService.getInvoice(id));
    }

    @GetMapping("/all")
    public ResponseEntity<List<Invoice>> getInvoices() {
        return ResponseEntity.ok(invoiceService.getInvoices());
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<CustomResponse> updateInvoice(@PathVariable Long id, @Valid @RequestBody Invoice invoice) {
        Boolean isUpdated = invoiceService.updateInvoice(invoice, id);

        if (Boolean.TRUE.equals(isUpdated)) {
            return ResponseEntity.ok(new CustomResponse(true, "Invoice updated successfully!"));
        } else {
            return ResponseEntity.ok(new CustomResponse(false, "Invoice not updated!"));
        }
    }


    @DeleteMapping("/delete/{id}")
    public ResponseEntity<CustomResponse> deleteInvoice(@PathVariable Long id) {
        Boolean isDeleted = invoiceService.deleteInvoice(id);
        if (isDeleted){
            return ResponseEntity.ok(new CustomResponse(true, "Invoice deleted successfully!"));
        } else {
            return ResponseEntity.ok(new CustomResponse(false, "Invoice not deleted!"));
        }
    }

    @DeleteMapping("/delete-invoices")
    public ResponseEntity<CustomResponse> deleteInvoices() {
        Boolean isDeleted = invoiceService.deleteInvoices();
        if (isDeleted){
            return ResponseEntity.ok(new CustomResponse(true, "Invoices deleted successfully!"));
        } else {
            return ResponseEntity.ok(new CustomResponse(false, "Invoices not deleted!"));
        }
    }
}
