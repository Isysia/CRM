-- Initial sample data for customers table

INSERT INTO customers (name, email, phone, status)
VALUES ('Alice Johnson', 'alice.johnson@example.com', '+15551234567', 'LEAD'),
       ('Bob Smith', 'bob.smith@example.com', '+15557654321', 'ACTIVE'),
       ('Carol Williams', 'carol.williams@example.com', '+15559876543', 'INACTIVE'),
       ('David Brown', 'david.brown@example.com', '+15553456789', 'CLOSED'),
       ('Eve Davis', 'eve.davis@example.com', '+15552345678', 'LEAD');

-- If you also have deals and tasks tables, you can add initial rows like this:

-- INSERT INTO deals (customer_id, amount, stage, created_at) VALUES
--   (1, 1000.00, 'PROPOSAL', CURRENT_TIMESTAMP),
--   (2, 2500.00, 'NEGOTIATION', CURRENT_TIMESTAMP),
--   (3,  500.00, 'CLOSED', CURRENT_TIMESTAMP);

-- INSERT INTO tasks (deal_id, description, due_date, status) VALUES
--   (1, 'Follow up call',       DATEADD('DAY', 3, CURRENT_DATE), 'OPEN'),
--   (2, 'Send revised quote',   DATEADD('DAY', 1, CURRENT_DATE), 'OPEN'),
--   (3, 'Archive completed deal', CURRENT_DATE,                    'DONE');

