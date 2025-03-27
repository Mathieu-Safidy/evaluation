ALTER TABLE trigger_lead ADD COLUMN depenses_id int not null ;
ALTER TABLE trigger_ticket ADD COLUMN depenses_id int not null ;

ALTER TABLE trigger_lead ADD CONSTRAINT depenses_key_1 FOREIGN KEY (depenses_id) REFERENCES crm.depenses(depenses_id);
ALTER TABLE trigger_ticket ADD CONSTRAINT depenses_key_2 FOREIGN KEY (depenses_id) REFERENCES crm.depenses  (depenses_id);

ALTER TABLE budget ADD COLUMN finished_at DATETIME not null;
ALTER TABLE budget ADD COLUMN libele DATETIME not null;
ALTER TABLE budget MODIFY COLUMN libele VARCHAR(50);
ALTER TABLE depenses ADD COLUMN libele VARCHAR(50) not null;

ALTER TABLE budget ADD COLUMN id_alerte INT ;
ALTER TABLE budget ADD CONSTRAINT budget_alerte_fk_1 FOREIGN KEY (id_alerte) REFERENCES budget_alerte(id_alerte);