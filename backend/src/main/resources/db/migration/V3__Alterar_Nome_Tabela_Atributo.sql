ALTER TABLE Categoria RENAME TO Modalidade;

ALTER TABLE Modalidade
RENAME COLUMN Modalidade TO Categoria;

