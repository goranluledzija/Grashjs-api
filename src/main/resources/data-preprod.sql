INSERT INTO currency (id, name, code)
VALUES (1, 'Euro', '€'),
       (2, 'Dollar', '$')
ON CONFLICT
    (id)
DO UPDATE SET name = excluded.name;
