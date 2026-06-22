-- Cambios para optimización de Checkout (CRO): Eliminación de DNI/CE del Receptor
ALTER TABLE order_addresses
    DROP COLUMN contact_doc_type,
    DROP COLUMN contact_doc_number;
