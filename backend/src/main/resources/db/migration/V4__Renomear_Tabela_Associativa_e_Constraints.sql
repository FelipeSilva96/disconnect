-- Aqui é pra mudar as tabelas associativas por causa da mudança pra Modalidade
ALTER TABLE Categoria_evento RENAME TO Modalidade_evento;
ALTER TABLE Modalidade_evento RENAME COLUMN Id_categoria TO Id_modalidade;

-- Renomear as chaves primarias porque elas também mudaram 
ALTER TABLE Modalidade RENAME CONSTRAINT pk_categoria TO pk_modalidade;
ALTER TABLE Modalidade_evento RENAME CONSTRAINT pk_categoria_evento TO pk_modalidade_evento;

-- e as fks tambem

ALTER TABLE Modalidade_evento RENAME CONSTRAINT fk_categoria_evento TO fk_modalidade_evento;
ALTER TABLE Modalidade_evento RENAME CONSTRAINT fk_evento_categoria TO fk_evento_modalidade;